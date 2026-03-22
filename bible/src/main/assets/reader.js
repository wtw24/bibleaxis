/*
 * Copyright (C) 2011 Scripture Software (http://scripturesoftware.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function getBaseVerseClass(element) {
    if (element && element.getAttribute && element.getAttribute('data-intro') === 'true') {
        return 'verse introVerse';
    }
    return 'verse';
}

function selectVerse(id) {
	var element = document.getElementById(id);
	if (element) {
		element.className = "selectedVerse";
	}
}

function deselectVerse(id) {
	var element = document.getElementById(id);
	if (element) {
		element.className = getBaseVerseClass(element);
	}
}

function getCurrentVerse() {
    var y = 15;
    var element;
    while (element == null && y < 100) {
        element = document.elementFromPoint(100, y);
        y = y + 5;
    }

    while (element != null && element.id.indexOf('verse') == -1) {
        element = element.parentElement;
    }

    if (element != null) {
        reader.setCurrentVerse(element.id);
    }
}

function handleClick(x, y) {
	var element = document.elementFromPoint(x, y);
	
	while (element != null && element.id.indexOf('verse') == -1) {
		var highlightId = element.getAttribute ? element.getAttribute('data-highlight-id') : null;
		if (highlightId) {
		    reader.onClickHighlight(highlightId);
		    return;
		}
		var hrefAttr = element.getAttribute ? element.getAttribute('href') : null;
		if (hrefAttr && /^s(?:[gh]\s*)?\d+$/i.test(hrefAttr)) {
			reader.onClickStrong(hrefAttr.replace(/\s+/g, '').toUpperCase());
			return;
		}
		if (element instanceof window.HTMLAnchorElement && (element.href.indexOf('#') != -1)) {
			return;
		} else if (element.nodeName == "IMG") {
		    reader.onClickImage(element.getAttribute("src"));
		    return;
		}
		element = element.parentElement;
	}
	if (element != null) {
		reader.onClickVerse(element.id);	
	}
}

function gotoVerse(number) {
    document.location.href='#verse_' + number;
}

function getVerseNode(node) {
    var current = node;
    while (current != null && current !== document.body) {
        if (current.id && current.id.indexOf('verse_') === 0) {
            return current;
        }
        current = current.parentNode;
    }
    return null;
}

function getSelectionPosition(container, offset) {
    var verseNode = getVerseNode(container.nodeType === Node.TEXT_NODE ? container.parentNode : container);
    if (verseNode == null) {
        return null;
    }

    var preRange = document.createRange();
    preRange.setStart(verseNode, 0);
    try {
        preRange.setEnd(container, offset);
    } catch (e) {
        return null;
    }

    return {
        verse: parseInt(verseNode.id.replace('verse_', ''), 10),
        offset: preRange.toString().length
    };
}

function getSelectionPayload() {
    var selection = window.getSelection();
    if (!selection || selection.rangeCount === 0 || selection.isCollapsed) {
        return null;
    }

    var range = selection.getRangeAt(0);
    var start = getSelectionPosition(range.startContainer, range.startOffset);
    var end = getSelectionPosition(range.endContainer, range.endOffset);

    if (start == null || end == null) {
        return null;
    }

    if (start.verse > end.verse || (start.verse === end.verse && start.offset > end.offset)) {
        var temp = start;
        start = end;
        end = temp;
    }

    var rect = range.getBoundingClientRect();

    return {
        startVerse: start.verse,
        startOffset: start.offset,
        endVerse: end.verse,
        endOffset: end.offset,
        quote: selection.toString(),
        rectTop: rect ? rect.top : 0,
        rectBottom: rect ? rect.bottom : 0,
        rectLeft: rect ? rect.left : 0,
        rectRight: rect ? rect.right : 0
    };
}

function getTextNodes(root) {
    var nodes = [];
    var walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null, false);
    var node = walker.nextNode();
    while (node) {
        nodes.push(node);
        node = walker.nextNode();
    }
    return nodes;
}

function getNodeOffset(root, charOffset) {
    var nodes = getTextNodes(root);
    if (nodes.length === 0) {
        return null;
    }

    var currentOffset = 0;
    for (var i = 0; i < nodes.length; i++) {
        var textNode = nodes[i];
        var nextOffset = currentOffset + textNode.nodeValue.length;
        if (charOffset <= nextOffset) {
            return {
                node: textNode,
                offset: Math.max(0, charOffset - currentOffset)
            };
        }
        currentOffset = nextOffset;
    }

    return {
        node: nodes[nodes.length - 1],
        offset: nodes[nodes.length - 1].nodeValue.length
    };
}

function getVerseTextStartOffset(verseNode) {
    if (!verseNode) {
        return 0;
    }

    var verseNumberNode = verseNode.querySelector('.verseNumber');
    if (!verseNumberNode) {
        return 0;
    }

    var range = document.createRange();
    range.setStart(verseNode, 0);
    range.setEndAfter(verseNumberNode);
    return range.toString().length;
}

function applyHighlightInVerse(verseNode, startOffset, endOffset, color, id) {
    if (endOffset <= startOffset) {
        return;
    }

    var verseText = verseNode.textContent || '';
    var verseTextStartOffset = getVerseTextStartOffset(verseNode);
    var safeStartOffset = Math.max(verseTextStartOffset, Math.min(startOffset, verseText.length));
    var safeEndOffset = Math.max(0, Math.min(endOffset, verseText.length));

    while (safeStartOffset < safeEndOffset && /\s/.test(verseText.charAt(safeStartOffset))) {
        safeStartOffset++;
    }
    while (safeEndOffset > safeStartOffset && /\s/.test(verseText.charAt(safeEndOffset - 1))) {
        safeEndOffset--;
    }

    if (safeEndOffset <= safeStartOffset) {
        return;
    }

    var startPosition = getNodeOffset(verseNode, safeStartOffset);
    var endPosition = getNodeOffset(verseNode, safeEndOffset);
    if (startPosition == null || endPosition == null) {
        return;
    }

    var range = document.createRange();
    range.setStart(startPosition.node, startPosition.offset);
    range.setEnd(endPosition.node, endPosition.offset);

    var wrapper = document.createElement('span');
    wrapper.className = 'textHighlight';
    wrapper.setAttribute('data-highlight-color', color);
    wrapper.style.backgroundColor = highlightsVisible ? color : 'transparent';
    wrapper.style.padding = highlightsVisible ? '0 1px 0 0' : '0';
    wrapper.style.borderRadius = highlightsVisible ? '3px' : '0';
    wrapper.style.pointerEvents = highlightsVisible ? 'auto' : 'none';
    wrapper.setAttribute('data-highlight-id', id);

    var extracted = range.extractContents();
    wrapper.appendChild(extracted);
    range.insertNode(wrapper);
}

var highlightsVisible = true;

function setHighlightsVisible(visible) {
    highlightsVisible = !!visible;
    var nodes = document.querySelectorAll('span[data-highlight-id]');
    for (var i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        var color = node.getAttribute('data-highlight-color') || '';
        node.style.backgroundColor = highlightsVisible ? color : 'transparent';
        node.style.padding = highlightsVisible ? '0 1px 0 0' : '0';
        node.style.borderRadius = highlightsVisible ? '3px' : '0';
        node.style.pointerEvents = highlightsVisible ? 'auto' : 'none';
    }
}

function applyHighlightRange(rangeData) {
    for (var verse = rangeData.startVerse; verse <= rangeData.endVerse; verse++) {
        var verseNode = document.getElementById('verse_' + verse);
        if (!verseNode) {
            continue;
        }

        var verseTextLength = verseNode.textContent.length;
        var startOffset = verse === rangeData.startVerse ? rangeData.startOffset : 0;
        var endOffset = verse === rangeData.endVerse ? rangeData.endOffset : verseTextLength;
        applyHighlightInVerse(verseNode, startOffset, endOffset, rangeData.color, rangeData.id);
    }
}

function applyHighlights(highlights) {
    clearHighlights();

    if (!highlights || !highlights.length) {
        return;
    }

    for (var i = 0; i < highlights.length; i++) {
        applyHighlightRange(highlights[i]);
    }
}

function clearHighlights() {
    var nodes = document.querySelectorAll('span[data-highlight-id]');
    for (var i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        var parent = node.parentNode;
        if (!parent) {
            continue;
        }

        while (node.firstChild) {
            parent.insertBefore(node.firstChild, node);
        }
        parent.removeChild(node);
    }
}

var selectionNotifyTimer = null;

function notifySelectionChanged() {
    if (selectionNotifyTimer) {
        clearTimeout(selectionNotifyTimer);
    }

    selectionNotifyTimer = setTimeout(function() {
        var payload = getSelectionPayload();
        try {
            reader.onSelectionChanged(payload ? JSON.stringify(payload) : '');
        } catch (e) {
            // ignore bridge errors
        }
    }, 180);
}

document.addEventListener('selectionchange', notifySelectionChanged);
document.addEventListener('mouseup', notifySelectionChanged);
document.addEventListener('touchend', notifySelectionChanged);

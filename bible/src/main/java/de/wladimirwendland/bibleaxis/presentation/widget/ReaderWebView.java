/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import de.wladimirwendland.bibleaxis.domain.entity.Chapter;
import de.wladimirwendland.bibleaxis.domain.entity.Highlight;
import de.wladimirwendland.bibleaxis.domain.entity.Verse;
import de.wladimirwendland.bibleaxis.domain.textFormatters.ITextFormatter;
import de.wladimirwendland.bibleaxis.domain.textFormatters.StripTagsTextFormatter;
import de.wladimirwendland.bibleaxis.entity.TextAppearance;
import de.wladimirwendland.bibleaxis.presentation.ui.reader.IReaderViewListener;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

@SuppressLint("SetJavaScriptEnabled")
public class ReaderWebView extends WebView
        implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    public static final String TAG = ReaderWebView.class.getSimpleName();
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern NUMBERED_VERSE_PATTERN = Pattern.compile("^\\s*\\d+\\s+.*", Pattern.DOTALL);
    private static final Pattern LEADING_BIBLE_NUMBER_PATTERN = Pattern.compile(
            "^\\s*((?:<[^>]+>\\s*)*)(?:<(?:span|font|b|sup)[^>]*>\\s*)*(\\d+)(?:\\s*</(?:span|font|b|sup)>\\s*)*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern LEADING_FONT_TAG_WITH_ATTRS_PATTERN = Pattern.compile("(?i)<font\\b[^>]*>");
    private static final Pattern LEADING_SPAN_TAG_WITH_ATTRS_PATTERN = Pattern.compile("(?i)<span\\b[^>]*>");
    private static final Pattern BIBLE_FONT_TAG_WITH_ATTRS_PATTERN = Pattern.compile("(?i)<font\\b[^>]*>");
    private static final Pattern BIBLE_SPAN_TAG_WITH_ATTRS_PATTERN = Pattern.compile("(?i)<span\\b[^>]*>");
    private static final Pattern BIBLE_PARAGRAPH_TAG_WITH_ATTRS_PATTERN = Pattern.compile("(?i)<p\\b[^>]*>");
    private static final Pattern PRESENTATION_ATTRS_PATTERN = Pattern.compile(
            "(?i)\\s+(?:style|color|face|size|class)\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s>]+)"
    );
    private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile("(?is)<script\\b[^>]*>.*?</script>");
    private static final Pattern UNSAFE_EMBED_TAG_PATTERN = Pattern.compile("(?is)</?(?:iframe|object|embed|meta|base)\\b[^>]*>");
    private static final Pattern EVENT_HANDLER_ATTR_PATTERN = Pattern.compile(
            "(?i)\\s+on[a-z0-9_-]+\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s>]+)"
    );
    private static final Pattern JAVASCRIPT_URL_ATTR_PATTERN = Pattern.compile(
            "(?i)\\s+(?:href|src)\\s*=\\s*(?:\"\\s*javascript:[^\"]*\"|'\\s*javascript:[^']*'|\\s*javascript:[^\\s>]+)"
    );

    public boolean mPageLoaded;
    private String baseUrl;
    private String content;
    private Mode currMode = Mode.Read;
    private int currVerse;
    private ITextFormatter formatter = new StripTagsTextFormatter();
    private boolean isBible;
    private JavaScriptInterface jsInterface;
    private GestureDetector mGestureScanner;
    private List<Highlight> highlights = Collections.emptyList();
    private boolean highlightsVisible = true;
    private int highlightApplySession = 0;
    private TreeSet<Integer> selectedVerse = new TreeSet<>();
    private final ReaderTaskHandler taskHandler;
    private TextAppearance textAppearance;
    private final ViewHandler viewHandler;
    private OnFindInPageListener onFindInPageListener;

    @SuppressLint("AddJavascriptInterface")
    public ReaderWebView(Context mContext, AttributeSet attributeSet) {
        super(mContext, attributeSet);

        viewHandler = new ViewHandler();
        taskHandler = new ReaderTaskHandler(this);
        if (!isInEditMode()) {
            hardenWebViewSettings();
            setWebViewClient(new webClient());
            setWebChromeClient(new ChromeClient());

            this.jsInterface = new JavaScriptInterface();
            addJavascriptInterface(this.jsInterface, "reader");
            setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
                if (onFindInPageListener != null) {
                    onFindInPageListener.onFindResult(activeMatchOrdinal, numberOfMatches, isDoneCounting);
                }
            });

            setVerticalScrollbarOverlay(true);

            mGestureScanner = new GestureDetector(mContext, this);
            mGestureScanner.setIsLongpressEnabled(true);
            mGestureScanner.setOnDoubleTapListener(this);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void hardenWebViewSettings() {
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(false);
            settings.setAllowUniversalAccessFromFileURLs(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            removeJavascriptInterface("searchBoxJavaBridge_");
            removeJavascriptInterface("accessibility");
            removeJavascriptInterface("accessibilityTraversal");
        }
    }

    public int getCurrVerse() {
        return currVerse;
    }

    public Mode getReaderMode() {
        return currMode;
    }

    public TreeSet<Integer> getSelectedVerses() {
        return this.selectedVerse;
    }

    public void setFormatter(@NonNull ITextFormatter formatter) {
        this.formatter = formatter;
    }

    public void setMode(Mode mode) {
        currMode = mode;
        if (currMode != Mode.Study) {
            clearSelectedVerse();
        }
        notifyListeners(IReaderViewListener.ChangeCode.onChangeReaderMode);
    }

    public void setOnReaderViewListener(IReaderViewListener listener) {
        viewHandler.setListener(listener);
    }

    public void setSelectedVerse(TreeSet<Integer> selectedVerse) {
        jsInterface.clearSelectedVerse();
        this.selectedVerse = selectedVerse;
        for (Integer verse : selectedVerse) {
            jsInterface.selectVerse(verse);
        }
    }

    public void setTextAppearance(TextAppearance textApearence) {
        this.textAppearance = textApearence;
        update();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mPageLoaded) {
            taskHandler.addSingleDelayedMessage();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureScanner.onTouchEvent(event) || (event != null && super.onTouchEvent(event));
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (currMode == Mode.Study) {
            float density = getContext().getResources().getDisplayMetrics().density;
            x = (int) (x / density);
            y = (int) (y / density);

            loadUrl("javascript:handleClick(" + x + ", " + y + ");");
            notifyListeners(IReaderViewListener.ChangeCode.onChangeSelection);
        } else if (currMode == Mode.Read) {
            int width = this.getWidth();
            int height = this.getHeight();

            if (((float) y / height) <= 0.33) {
                notifyListeners(IReaderViewListener.ChangeCode.onUpNavigation);
            } else if (((float) y / height) > 0.67) {
                notifyListeners(IReaderViewListener.ChangeCode.onDownNavigation);
            } else if (((float) x / width) <= 0.33) {
                notifyListeners(IReaderViewListener.ChangeCode.onLeftNavigation);
            } else if (((float) x / width) > 0.67) {
                notifyListeners(IReaderViewListener.ChangeCode.onRightNavigation);
            }
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float dx = e1.getX() - e2.getX();
        float dy = e1.getY() - e2.getY();
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx < 0) {
                notifyListeners(IReaderViewListener.ChangeCode.onLeftNavigation);
            } else {
                notifyListeners(IReaderViewListener.ChangeCode.onRightNavigation);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        notifyListeners(IReaderViewListener.ChangeCode.onLongPress);
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        setMode(currMode == Mode.Study ? Mode.Read : Mode.Study);
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return false;
    }

    public void clearSelectedVerse() {
        if (selectedVerse.size() == 0) {
            return;
        }
        jsInterface.clearSelectedVerse();
        if (currMode == Mode.Study) {
            notifyListeners(IReaderViewListener.ChangeCode.onChangeSelection);
        }
    }

    public void clearTextSelection() {
        evaluateJavascript("window.getSelection().removeAllRanges();", null);
    }

    public void setOnFindInPageListener(OnFindInPageListener listener) {
        this.onFindInPageListener = listener;
    }

    public void findInPage(String query) {
        if (query == null || query.length() == 0) {
            clearFindInPage();
            return;
        }
        findAllAsync(query);
    }

    public void findNextInPage(boolean forward) {
        findNext(forward);
    }

    public void clearFindInPage() {
        clearMatches();
    }

    public void setHighlightsVisible(boolean visible) {
        this.highlightsVisible = visible;
        evaluateJavascript("setHighlightsVisible(" + (visible ? "true" : "false") + ");", null);
    }

    public void gotoVerse(int verse) {
        jsInterface.gotoVerse(verse);
    }

    public void setContent(String baseUrl, Chapter chapter, int currVerse, Boolean isBible, List<Highlight> highlights) {
        this.baseUrl = baseUrl;
        this.isBible = Boolean.TRUE.equals(isBible);
        this.content = getContent(chapter);
        this.currVerse = currVerse;
        this.highlights = highlights == null ? Collections.emptyList() : highlights;
        update();
    }

    public void update() {
        Log.d(TAG, "update");
        highlightApplySession++;
        mPageLoaded = false;
        taskHandler.removeMessages(ReaderTaskHandler.MSG_HANDLE_SCROLL);

        String modStyle = isBible ? "bible_style.css" : "book_style.css";
        @SuppressWarnings("StringBufferReplaceableByString")
        StringBuilder html = new StringBuilder()
                .append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\r\n")
                .append("<html>\r\n")
                .append("<head>\r\n")
                .append("<meta http-equiv=Content-Type content=\"text/html; charset=UTF-8\">\r\n")
                .append("<script language=\"JavaScript\" src=\"file:///android_asset/reader.js\" type=\"text/javascript\"></script>\r\n")
                .append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/")
                .append(modStyle)
                .append("\">\r\n")
                .append(getStyle())
                .append("</head>\r\n")
                .append("<body>\r\n")
                .append(content == null ? "" : content)
                .append("</body>\r\n")
                .append("</html>");

        loadDataWithBaseURL("file://" + baseUrl, html.toString(), "text/html", "UTF-8", "about:config");
        jsInterface.clearSelectedVerse();
    }

    private String getContent(Chapter chapter) {
        if (chapter == null) {
            return "";
        }

        ArrayList<Verse> verses = chapter.getVerseList();
        StringBuilder chapterHTML = new StringBuilder();
        for (int verse = 1; verse <= verses.size(); verse++) {
            String verseText = formatter.format(verses.get(verse - 1).getText());
            verseText = sanitizeUntrustedMarkup(verseText);
            if (isBible) {
                verseText = sanitizeBibleVerseMarkup(verseText);
            }
            boolean introVerse = isBible && isIntroVerse(verseText);
            if (isBible && !introVerse) {
                verseText = normalizeBibleVerseNumber(verseText);
            }
            chapterHTML.append("<div id=\"verse_").append(verse).append("\" class=\"verse")
                    .append(introVerse ? " introVerse" : "")
                    .append("\" data-intro=\"")
                    .append(introVerse ? "true" : "false")
                    .append("\">")
                    .append(verseText.replaceAll("<(/)*div(.*?)>", "<$1p$2>"))
                    .append("</div>")
                    .append("\r\n");
        }

        return chapterHTML.toString();
    }

    private String getStyle() {
        String textColor;
        String backColor;
        String selTextColor;
        String selTextBack;
        String introTextColor;

        getSettings().setStandardFontFamily(textAppearance.getTypeface());

        if (textAppearance.isNightMode()) {
            textColor = "#EEEEEE";
            backColor = "#000000";
            selTextColor = "#EEEEEE";
            selTextBack = "#562000";
            introTextColor = "#2E6FB7";
        } else {
            backColor = textAppearance.getBackground();
            textColor = textAppearance.getTextColor();
            selTextColor = textAppearance.getSelectedTextColor();
            selTextBack = textAppearance.getSelectedBackgroung();
            introTextColor = "#2E6FB7";
        }
        String textSize = textAppearance.getTextSize();
        int lineSpacing = textAppearance.getLineSpacing();

        return "<style type=\"text/css\">\r\n" +
                "body {\r\n" +
                "padding: 5px 5px 50px;\r\n" +
                "text-align: " + textAppearance.getTextAlign() + ";\r\n" +
                "color: " + textColor + ";\r\n" +
                "font-size: " + textSize + "pt;\r\n" +
                "line-height: " + lineSpacing + "%;\r\n" +
                "background: " + backColor + ";\r\n" +
                "}\r\n" +
                ".verse {\r\n" +
                "background: " + backColor + ";\r\n" +
                "}\r\n" +
                ".verseNumber {\r\n" +
                "color: #2E6FB7;\r\n" +
                "font-weight: 400;\r\n" +
                "font-size: 0.62em;\r\n" +
                "vertical-align: super;\r\n" +
                "line-height: 1;\r\n" +
                "margin-right: 0.25em;\r\n" +
                "}\r\n" +
                ".verse.introVerse {\r\n" +
                "color: " + introTextColor + ";\r\n" +
                "}\r\n" +
                ".verse.introVerse i, .verse.introVerse em, .verse.introVerse b, .verse.introVerse strong {\r\n" +
                "color: inherit;\r\n" +
                "}\r\n" +
                ".selectedVerse {\r\n" +
                "color: " + selTextColor + ";\r\n" +
                "background: " + selTextBack + ";\r\n" +
                "}\r\n" +
                ".selectedVerse .verseNumber {\r\n" +
                "color: #2E6FB7;\r\n" +
                "}\r\n" +
                ".textHighlight {\r\n" +
                "border-radius: 3px;\r\n" +
                "padding: 0 1px;\r\n" +
                "}\r\n" +
                "img {\r\n" +
                "max-width: 100%;\r\n" +
                "}\r\n" +
                "</style>\r\n";
    }

    private boolean isIntroVerse(String verseText) {
        if (verseText == null) {
            return false;
        }

        String plainText = HTML_TAG_PATTERN.matcher(verseText).replaceAll(" ")
                .replace("&nbsp;", " ")
                .trim();
        if (plainText.isEmpty()) {
            return false;
        }
        return !NUMBERED_VERSE_PATTERN.matcher(plainText).matches();
    }

    private String normalizeBibleVerseNumber(String verseText) {
        if (verseText == null) {
            return "";
        }

        Matcher matcher = LEADING_BIBLE_NUMBER_PATTERN.matcher(verseText);
        if (!matcher.find()) {
            return verseText;
        }

        String prefix = matcher.group(1) == null ? "" : matcher.group(1);
        String number = matcher.group(2);
        String tail = verseText.substring(matcher.end()).replaceFirst("^\\s+", "");
        prefix = normalizeBibleVersePrefix(prefix);

        return prefix + "<span class=\"verseNumber\">" + number + "</span> " + tail;
    }

    private String normalizeBibleVersePrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return "";
        }

        String normalized = LEADING_FONT_TAG_WITH_ATTRS_PATTERN.matcher(prefix).replaceAll("<font>");
        return LEADING_SPAN_TAG_WITH_ATTRS_PATTERN.matcher(normalized).replaceAll("<span>");
    }

    private String sanitizeBibleVerseMarkup(String verseText) {
        if (verseText == null || verseText.isEmpty()) {
            return "";
        }

        String normalized = BIBLE_FONT_TAG_WITH_ATTRS_PATTERN.matcher(verseText).replaceAll("<font>");
        normalized = BIBLE_SPAN_TAG_WITH_ATTRS_PATTERN.matcher(normalized).replaceAll("<span>");
        normalized = BIBLE_PARAGRAPH_TAG_WITH_ATTRS_PATTERN.matcher(normalized).replaceAll("<p>");
        return PRESENTATION_ATTRS_PATTERN.matcher(normalized).replaceAll("");
    }

    static String sanitizeUntrustedMarkup(String verseText) {
        if (verseText == null || verseText.isEmpty()) {
            return "";
        }

        String sanitized = SCRIPT_TAG_PATTERN.matcher(verseText).replaceAll("");
        sanitized = UNSAFE_EMBED_TAG_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = EVENT_HANDLER_ATTR_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = JAVASCRIPT_URL_ATTR_PATTERN.matcher(sanitized).replaceAll("");
        return sanitized;
    }

    static boolean isAllowedResourceUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return false;
        }

        int schemeSeparator = rawUrl.indexOf(':');
        if (schemeSeparator <= 0) {
            return true;
        }

        String normalizedScheme = rawUrl.substring(0, schemeSeparator).toLowerCase(Locale.US);
        return "file".equals(normalizedScheme)
                || "data".equals(normalizedScheme)
                || "about".equals(normalizedScheme)
                || "content".equals(normalizedScheme)
                || "android.resource".equals(normalizedScheme);
    }

    private WebResourceResponse blockedResourceResponse(String reason) {
        Log.w(TAG, "Blocked web resource load: " + reason);
        WebResourceResponse response = new WebResourceResponse(
                "text/plain",
                "UTF-8",
                new java.io.ByteArrayInputStream(new byte[0])
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            response.setStatusCodeAndReasonPhrase(403, "Blocked by ReaderWebView policy");
        }
        return response;
    }

    private void notifyListeners(IReaderViewListener.ChangeCode code) {
        Message msg = Message.obtain(viewHandler, ViewHandler.MSG_OTHER, code);
        viewHandler.sendMessage(msg);
    }

    private void onScrollComplete() {
        Log.d(TAG, "onScrollComplete");
        loadUrl("javascript: getCurrentVerse();");
    }

    private static final class ChromeClient extends WebChromeClient {

        public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
            if (result != null) {
                result.confirm();
            }
            return true;
        }
    }

    private static class ReaderTaskHandler extends Handler {

        private static final int MSG_HANDLE_SCROLL = 1;

        private final WeakReference<ReaderWebView> reader;

        ReaderTaskHandler(ReaderWebView readerWebView) {
            super(Looper.getMainLooper());
            this.reader = new WeakReference<>(readerWebView);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            ReaderWebView readerWebView = reader.get();
            if (readerWebView == null) {
                return;
            }

            if (msg.what == MSG_HANDLE_SCROLL) {
                readerWebView.onScrollComplete();
            }
            super.handleMessage(msg);
        }

        void addSingleDelayedMessage() {
            removeMessages(ReaderTaskHandler.MSG_HANDLE_SCROLL);
            sendEmptyMessageDelayed(ReaderTaskHandler.MSG_HANDLE_SCROLL, 100);
        }
    }

    private final class webClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return true;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (!isAllowedResourceUrl(url)) {
                return blockedResourceResponse(url);
            }
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (request != null && request.getUrl() != null) {
                String url = request.getUrl().toString();
                if (!isAllowedResourceUrl(url)) {
                    return blockedResourceResponse(url);
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            Log.d(TAG, "onPageFinished");

            if (url != null && url.startsWith("about:")) {
                return;
            }

            if (!mPageLoaded) {
                scheduleApplyHighlightsWithRetry(highlightApplySession, 0);
                scheduleHighlightsRepaintBurst(highlightApplySession);
                if (currVerse == 1) {
                    loadUrl("javascript: window.scrollTo(0, 0);");
                } else {
                    gotoVerse(currVerse);
                }
            }
            mPageLoaded = true;
        }
    }

    private void scheduleApplyHighlightsWithRetry(int session, int attempt) {
        if (session != highlightApplySession) {
            return;
        }

        if (highlights.isEmpty()) {
            return;
        }

        String payload = buildHighlightsPayload();
        evaluateJavascript(
                "(function(){"
                        + "var ready=(typeof applyHighlights==='function') && !!document.querySelector('div[id^=\\\"verse_\\\"]');"
                        + "if(!ready){return 'NR';}"
                        + "applyHighlights(" + payload + ");"
                        + "if(typeof setHighlightsVisible==='function'){setHighlightsVisible(" + (highlightsVisible ? "true" : "false") + ");}"
                        + "return String(document.querySelectorAll('span[data-highlight-id]').length);"
                        + "})();",
                value -> {
                    if (session != highlightApplySession) {
                        return;
                    }

                    int appliedCount = 0;
                    try {
                        if (value != null) {
                            String normalized = value.replace("\"", "").trim();
                            if (!"NR".equals(normalized) && normalized.length() > 0) {
                                appliedCount = Integer.parseInt(normalized);
                            }
                        }
                    } catch (Exception ignored) {
                        // ignore parse errors, will retry
                    }

                    if (appliedCount > 0) {
                        return;
                    }

                    if (attempt >= 12) {
                        return;
                    }

                    postDelayed(() -> scheduleApplyHighlightsWithRetry(session, attempt + 1), 150);
                }
        );
    }

    private void scheduleHighlightsRepaintBurst(int session) {
        if (highlights.isEmpty()) {
            return;
        }

        postDelayed(() -> {
            if (session == highlightApplySession) {
                applyHighlights();
            }
        }, 300);

        postDelayed(() -> {
            if (session == highlightApplySession) {
                applyHighlights();
            }
        }, 900);

        postDelayed(() -> {
            if (session == highlightApplySession) {
                applyHighlights();
            }
        }, 1600);
    }

    private void applyHighlights() {
        evaluateJavascript(
                "applyHighlights(" + buildHighlightsPayload() + ");"
                        + "if(typeof setHighlightsVisible==='function'){setHighlightsVisible(" + (highlightsVisible ? "true" : "false") + ");}",
                null
        );
    }

    private String buildHighlightsPayload() {
        if (highlights.isEmpty()) {
            return "[]";
        }

        JSONArray array = new JSONArray();
        for (Highlight highlight : highlights) {
            JSONObject item = new JSONObject();
            try {
                item.put("id", highlight.id);
                item.put("startVerse", highlight.startVerse);
                item.put("startOffset", highlight.startOffset);
                item.put("endVerse", highlight.endVerse);
                item.put("endOffset", highlight.endOffset);
                item.put("color", highlight.color);
                array.put(item);
            } catch (Exception ex) {
                Log.e(TAG, "Failed build highlight payload", ex);
            }
        }
        return array.toString();
    }

    private final class JavaScriptInterface {

        JavaScriptInterface() {
            clearSelectedVerse();
        }

        @JavascriptInterface
        public void setCurrentVerse(String id) {
            Log.d(TAG, "setCurrentVerse " + id);
            if (id.matches("verse_\\d+?")) {
                try {
                    currVerse = Integer.parseInt(id.split("_")[1]);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }

        @JavascriptInterface
        public void onClickImage(String path) {
            Message msg = new Message();
            msg.what = ViewHandler.MSG_ON_CLICK_IMAGE;
            msg.obj = path;
            viewHandler.sendMessage(msg);
        }

        @JavascriptInterface
        public void onClickHighlight(String highlightId) {
            Message msg = Message.obtain(viewHandler, ViewHandler.MSG_ON_CLICK_HIGHLIGHT, highlightId);
            viewHandler.sendMessage(msg);
        }

        @JavascriptInterface
        public void onClickVerse(String id) {
            if (currMode != Mode.Study || !id.contains("verse")) {
                return;
            }

            try {
                Integer verse = Integer.parseInt(id.split("_")[1]);
                if (selectedVerse.contains(verse)) {
                    selectedVerse.remove(verse);
                    deselectVerse(verse);
                } else {
                    selectedVerse.add(verse);
                    selectVerse(verse);
                }
                final Message msg = Message.obtain(viewHandler, ViewHandler.MSG_OTHER, IReaderViewListener.ChangeCode.onChangeSelection);
                viewHandler.sendMessage(msg);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void onSelectionChanged(String payload) {
            if (payload == null || payload.length() == 0) {
                return;
            }
            Message msg = Message.obtain(viewHandler, ViewHandler.MSG_ON_TEXT_SELECTION, payload);
            viewHandler.sendMessage(msg);
        }

        private void clearSelectedVerse() {
            for (Integer verse : selectedVerse) {
                deselectVerse(verse);
            }
            selectedVerse.clear();
        }

        private void deselectVerse(final Integer verse) {
            viewHandler.post(() -> loadUrl("javascript: deselectVerse('verse_" + verse + "');"));
        }

        private void gotoVerse(final int verse) {
            Log.d(TAG, "gotoVerse " + verse);
            viewHandler.post(() -> loadUrl("javascript: gotoVerse(" + verse + ");"));
        }

        private void selectVerse(final int verse) {
            viewHandler.post(() -> loadUrl("javascript: selectVerse('verse_" + verse + "');"));
        }
    }

    public interface OnFindInPageListener {
        void onFindResult(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting);
    }
}

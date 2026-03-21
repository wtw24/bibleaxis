package de.wladimirwendland.bibleaxis.presentation.widget;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReaderWebViewSecurityTest {

    @Test
    public void sanitizeUntrustedMarkup_removesScriptBlocksAndEventHandlers() {
        String raw = "<p onclick=\"alert(1)\">Text<script>alert(1)</script></p>";

        String sanitized = ReaderWebView.sanitizeUntrustedMarkup(raw);

        assertFalse(sanitized.contains("<script"));
        assertFalse(sanitized.contains("onclick="));
        assertTrue(sanitized.contains("Text"));
    }

    @Test
    public void sanitizeUntrustedMarkup_removesJavascriptUrls() {
        String raw = "<a href=\"javascript:alert('x')\">open</a><img src='javascript:foo()'>";

        String sanitized = ReaderWebView.sanitizeUntrustedMarkup(raw);

        assertFalse(sanitized.toLowerCase().contains("javascript:"));
        assertTrue(sanitized.contains("open"));
    }

    @Test
    public void sanitizeUntrustedMarkup_removesUnsafeEmbedAndMetaTags() {
        String raw = "<iframe src=\"x\"></iframe><object></object><embed src=\"x\"><meta charset=\"utf-8\"><base href=\"https://example.com\"><p>safe</p>";

        String sanitized = ReaderWebView.sanitizeUntrustedMarkup(raw);

        assertFalse(sanitized.toLowerCase().contains("<iframe"));
        assertFalse(sanitized.toLowerCase().contains("<object"));
        assertFalse(sanitized.toLowerCase().contains("<embed"));
        assertFalse(sanitized.toLowerCase().contains("<meta"));
        assertFalse(sanitized.toLowerCase().contains("<base"));
        assertTrue(sanitized.contains("safe"));
    }

    @Test
    public void isAllowedResourceUrl_allowsLocalSchemes() {
        assertTrue(ReaderWebView.isAllowedResourceUrl("file:///android_asset/reader.js"));
        assertTrue(ReaderWebView.isAllowedResourceUrl("about:blank"));
        assertTrue(ReaderWebView.isAllowedResourceUrl("data:text/plain;base64,AA=="));
        assertTrue(ReaderWebView.isAllowedResourceUrl("content://com.example.provider/item"));
    }

    @Test
    public void isAllowedResourceUrl_blocksRemoteSchemes() {
        assertFalse(ReaderWebView.isAllowedResourceUrl("https://example.com/a.js"));
        assertFalse(ReaderWebView.isAllowedResourceUrl("http://example.com/img.png"));
        assertFalse(ReaderWebView.isAllowedResourceUrl("javascript:alert(1)"));
    }

    @Test
    public void isAllowedResourceUrl_handlesRelativeAndCaseInsensitiveSchemes() {
        assertTrue(ReaderWebView.isAllowedResourceUrl("chapter/1.html"));
        assertTrue(ReaderWebView.isAllowedResourceUrl("FILE:///android_asset/reader.css"));
        assertFalse(ReaderWebView.isAllowedResourceUrl(""));
    }
}

// Copyright 2014 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.distiller.webdocument;

import org.chromium.distiller.DomDistillerJsTestCase;
import org.chromium.distiller.DomWalker;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

import java.util.List;

public class DomConverterTest extends DomDistillerJsTestCase {
    private void runTest(String innerHtml, String expectedHtml) throws Throwable {
        runTest(innerHtml, expectedHtml, false);
    }

    private void runTest(String innerHtml, String expectedHtml, boolean mobileArticle)
            throws Throwable {
        Element container = Document.get().createDivElement();
        mBody.appendChild(container);
        container.setInnerHTML(innerHtml);
        FakeWebDocumentBuilder builder = new FakeWebDocumentBuilder();
        DomConverter filteringDomVisitor = new DomConverter(builder);
        filteringDomVisitor.setHasArticleElement(mobileArticle);
        filteringDomVisitor.setIsMobileFriendly(mobileArticle);
        new DomWalker(filteringDomVisitor).walk(container);
        String expectedDocument = "<div>" + expectedHtml + "</div>";
        assertEquals(expectedDocument, builder.getDocumentString().toLowerCase());
    }

    public void testVisibleText() throws Throwable {
        String html = "visible text";
        runTest(html, html);
    }

    public void testVisibleElement() throws Throwable {
        String html = "<div>visible element</div>";
        runTest(html, html);
    }

    public void testDisplayNone() throws Throwable {
        runTest("<div style=\"display:none\">diplay none</div>", "");
    }

    public void testVisibilityHidden() throws Throwable {
        runTest("<div style=\"visibility:hidden\">visibility hidden</div>", "");
    }

    public void testInvisibleInVisible() throws Throwable {
        String html = "<div>visible parent" +
                          "<div style=\"display:none\">invisible child</div>" +
                      "</div>";
        runTest(html, "<div>visible parent</div>");
    }

    public void testVisibleInInvisible() throws Throwable {
        String html = "<div style=\"visibility:hidden\">invisible parent" +
                "<div>visible child</div>" +
                "</div>";
        runTest(html, "");
    }

    public void testVisibleInInvisible2() throws Throwable {
        String html = "<div style=\"display:none\">invisible parent" +
                "<div>visible child</div>" +
                "</div>";
        runTest(html, "");
    }

    public void testVisibleInVisible() throws Throwable {
        String html = "<div>visible parent" +
                          "<div>visible child</div>" +
                      "</div>";
        runTest(html, html);
    }

    public void testInvisibleInInvisible() throws Throwable {
        String html = "<div style=\"visibility:hidden\">invisible parent" +
                          "<div style=\"display:none\">invisible child</div>" +
                      "</div>";
        runTest(html, "");
    }

    public void testDifferentChildrenInVisible() throws Throwable {
        String html = "<div>visible parent" +
                          "<div style=\"display:none\">invisible child0</div>" +
                          "<div>visible child1" +
                              "<div style=\"visibility:hidden\">invisible grandchild</div>" +
                          "</div>" +
                          "<div style=\"visibility:hidden\">invisible child2</div>" +
                      "</div>";
        runTest(html, "<div>visible parent<div>visible child1</div></div>");
    }

    public void testDifferentChildrenInInvisible() throws Throwable {
        String html = "<div style=\"visibility:hidden\">invisible parent" +
                          "<div style=\"display:none\">invisible child0</div>" +
                          "<div>visible child1" +
                              "<div style=\"display:none\">invisible grandchild</div>" +
                          "</div>" +
                          "<div style=\"visibility:hidden\">invisible child2</div>" +
                      "</div>";
        runTest(html, "");
    }

    public void testKeepHidden() throws Throwable {
        String html = "<div class=\"hidden\" style=\"display: none\">visible element</div>";
        runTest(html, html, true);
    }

    public void testKeepHiddenNested() throws Throwable {
        // "visibility: hidden" is used. See crbug.com/599121
        String html = "<div class=\"hidden\" style=\"visibility: hidden\">" +
                "<div>visible element</div></div>";
        runTest(html, html, true);
    }

    public void testKeepContinue() throws Throwable {
        String html = "<div class=\"continue\" style=\"display: none\">visible element</div>";
        runTest(html, html, true);
    }

    public void testKeepContinueNested() throws Throwable {
        // "display: none" is used. See crbug.com/687071
        String html = "<div class=\"continue\" style=\"display: none\">" +
                "<div>visible element</div></div>";
        runTest(html, html, true);
    }

    public void testWikipediaFoldedSections() throws Throwable {
        String html = "<div aria-expanded=\"false\" style=\"display: none\">folded section</div>";
        runTest(html, html, true);
    }

    public void testDataTable() throws Throwable {
        String html = "<table align=\"left\" role=\"grid\">" + // role=grid make this a data table.
                          "<tbody align=\"left\">" +
                              "<tr>" +
                                  "<td>row1col1</td>" +
                                  "<td>row1col2</td>" +
                              "</tr>" +
                          "</tbody>" +
                      "</table>";
        runTest(html, "<datatable/>");
    }

    public void testNonDataTable() throws Throwable {
        String html = "<table align=\"left\">" +
                          "<tbody align=\"left\">" +
                              "<tr>" +
                                  "<td>row1col1</td>" +
                                  "<td>row1col2</td>" +
                              "</tr>" +
                          "</tbody>" +
                      "</table>";
        runTest(html, html);
    }

    public void testIgnorableElements() throws Throwable {
        runTest("<head></head>", "");
        runTest("<style></style>", "");
        runTest("<script></script>", "");
        runTest("<link></link>", "");
        runTest("<noscript></noscript>", "");
        runTest("<iframe></iframe>", "");
        runTest("<svg></svg>", "");

        runTest("<option></option>", "");
        runTest("<object></object>", "");
        runTest("<embed></embed>", "");
        runTest("<applet></applet>", "");
    }

    public void testSvgTagNameCase() {
        Element container = Document.get().createDivElement();
        container.setInnerHTML("<SVG></SVG>");
        assertEquals("svg", container.getFirstChildElement().getTagName());
    }

    public void testElementOrder() {
        Element container = Document.get().createDivElement();
        container.setInnerHTML("Text content <img src=\"http://example.com/1.jpg\"> more content");

        WebDocumentBuilder builder = new WebDocumentBuilder();
        DomConverter converter = new DomConverter(builder);
        new DomWalker(converter).walk(container);

        WebDocument doc = builder.toWebDocument();
        List<WebElement> elements = doc.getElements();

        assertEquals(3, elements.size());
        assertTrue(elements.get(0) instanceof WebText);
        assertTrue(elements.get(1) instanceof WebImage);
        assertTrue(elements.get(2) instanceof WebText);
    }

    public void testLineBreak() throws Throwable {
        String html = "text<br>split<br/>with<br/>lines";
        runTest(html, "text\nsplit\nwith\nlines");
    }

    public void testList() throws Throwable {
        Element container = Document.get().createDivElement();
        container.setInnerHTML("<ol><li>some text1</li><li>some text2</li></ol>");

        WebDocumentBuilder builder = new WebDocumentBuilder();
        DomConverter converter = new DomConverter(builder);
        new DomWalker(converter).walk(container);

        WebDocument doc = builder.toWebDocument();
        List<WebElement> elements = doc.getElements();

        assertEquals(8, elements.size());
        assertTrue(elements.get(0) instanceof WebTag);
        assertTrue(((WebTag) elements.get(0)).isStartTag());

        assertTrue(elements.get(1) instanceof WebTag);
        assertTrue(((WebTag) elements.get(1)).isStartTag());

        assertTrue(elements.get(2) instanceof WebText);

        assertTrue(elements.get(3) instanceof WebTag);
        assertFalse(((WebTag) elements.get(3)).isStartTag());

        assertTrue(elements.get(4) instanceof WebTag);
        assertTrue(((WebTag) elements.get(4)).isStartTag());

        assertTrue(elements.get(5) instanceof WebText);

        assertTrue(elements.get(6) instanceof WebTag);
        assertFalse(((WebTag) elements.get(6)).isStartTag());

        assertTrue(elements.get(7) instanceof WebTag);
        assertFalse(((WebTag) elements.get(7)).isStartTag());
    }

    public void testSocialElements() throws Throwable {
        runTest("<div></div>", "<div></div>");
        runTest("<div data-component=\"share\"></div>", "");
        runTest("<div class=\"socialArea\"></div>", "");
        runTest("<li></li>", "<li></li>");
        runTest("<li class=\"sharing\"></li>", "");
    }

    public void testWikiEditLinks() throws Throwable {
        String notEdit = "<a href=\"index.php?action=edit&redlink=1\"></a>";
        runTest(notEdit, notEdit);
        runTest("<a href=\"index.php?action=edit&section=3\" class=\"mw-ui-icon\"></a>", "");
    }

    public void testWikiEditSection() throws Throwable {
        String notEdit = "<span class=\"mw-headline\"></span>";
        runTest(notEdit, notEdit);
        runTest("<span class=\"mw-editsection\"></span>", "");
    }
}

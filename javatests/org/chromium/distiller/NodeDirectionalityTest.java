// Copyright 2014 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.distiller;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;

public class NodeDirectionalityTest extends DomDistillerJsTestCase {

    private static final String CONTENT_TEXT = "Lorem Ipsum Lorem Ipsum Lorem Ipsum.";
    private static final String TITLE_TEXT = "I am the document title";

    public void testDirAttributeLtrAddedToTree() {
        Element div = TestUtil.createDiv(0);
        div.appendChild(TestUtil.createSpan(CONTENT_TEXT));
        div.appendChild(TestUtil.createSpan(CONTENT_TEXT));
        mBody.appendChild(div);

        NodeTree tree = new NodeTree(div);
        tree.addChild(div.getChild(0));
        tree.addChild(div.getChild(1));

        Node cloned = tree.cloneSubtreeRetainDirection();

        for (int i = 0; i < cloned.getChildCount(); i++) {
            Node n = cloned.getChild(i);
            assertEquals("ltr",Element.as(n).getAttribute("dir"));
        }
    }

    public void testDirAttributeRtlAddedToTree() {
        Element div = TestUtil.createDiv(0);
        div.getStyle().setProperty("direction","rtl");
        div.appendChild(TestUtil.createSpan(CONTENT_TEXT));
        div.appendChild(TestUtil.createSpan(CONTENT_TEXT));
        mBody.appendChild(div);

        NodeTree tree = new NodeTree(div);
        tree.addChild(div.getChild(0));
        tree.addChild(div.getChild(1));

        Node cloned = tree.cloneSubtreeRetainDirection();

        for (int i = 0; i < cloned.getChildCount(); i++) {
            Node n = cloned.getChild(i);
            assertEquals("rtl",Element.as(n).getAttribute("dir"));
        }
    }

    public void testMixedDirAttributeAddedToTree() {
        Element div = TestUtil.createDiv(0);
        div.getStyle().setProperty("direction","ltr");

        Element child1 = TestUtil.createDiv(1);
        child1.getStyle().setProperty("direction","rtl");

        child1.appendChild(TestUtil.createSpan(CONTENT_TEXT));

        div.appendChild(child1);

        mBody.appendChild(div);

        // construct node tree
        NodeTree tree = new NodeTree(div);
        tree.addChild(child1);
        tree.getChildren().get(0).addChild(child1.getChild(0));

        Node cloned = tree.cloneSubtreeRetainDirection();

        // make sure span element got the "dir" attribute correctly - based on parent
        assertEquals("rtl",Element.as(cloned.getChild(0).getChild(0)).getDir());
    }
}

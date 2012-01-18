/**
 * Copyright (c) 2011, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mnode.newsagent.reader

import groovy.lang.ExpandoMetaClass.SubClassDefiningClosure;

import java.awt.BorderLayout

import javax.swing.JScrollPane;
import javax.swing.JSplitPane
import javax.swing.text.html.StyleSheet

import org.mnode.ousia.HTMLEditorKitExt
import org.mnode.ousia.HyperlinkBrowser
import org.mnode.ousia.OusiaBuilder
import org.mnode.ousia.HyperlinkBrowser.HyperlinkFeedback
import org.mnode.ousia.layer.StatusLayerUI

def ousia = new OusiaBuilder()

ousia.edt {
	frame(title: rs('Newsagent Reader'), show: true, locationRelativeTo: null, trackingEnabled: true, size: [600, 400]) {
		borderLayout()
		splitPane(orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 200, continuousLayout: true, oneTouchExpandable: true, dividerSize: 10) {
			splitPane(constraints: 'left', dividerSize: 7) {
				scrollPane(horizontalScrollBarPolicy: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
					treeTable(constraints: 'left', treeTableModel: new SubscriptionTreeTableModel(null))
				}
				scrollPane(horizontalScrollBarPolicy: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
					table(constraints: 'right')
				}
			}
			panel(constraints: 'right') {
				borderLayout()
				label(constraints: BorderLayout.NORTH, border: emptyBorder(5), id: 'contentTitle')
				panel(id: 'feedItemView') {
					borderLayout()
					def statusLayer = new StatusLayerUI()
					layer(statusLayer) {
						scrollPane {
							def styleSheet = new StyleSheet()
							styleSheet.addRule('body {background-color:#ffffff; color:#444b56; font-family:verdana,sans-serif; margin:8px; }')
							defaultEditorKit = new HTMLEditorKitExt(styleSheet: styleSheet)
							editorPane(id: 'feedItemContent', editorKit: defaultEditorKit, editable: false, contentType: 'text/html', opaque: true, border: null)
							feedItemContent.addHyperlinkListener(new HyperlinkBrowser(feedback: [
									show: { uri -> statusLayer.showStatusMessage uri.toString() },
									hide: { statusLayer.hideStatusMessage() }
								] as HyperlinkFeedback))
						}
					}
				}
			}
		}
	}
}

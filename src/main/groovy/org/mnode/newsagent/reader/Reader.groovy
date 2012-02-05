/**
 * Copyright (c) 2012, Ben Fortuna
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

import java.awt.BorderLayout

import javax.jcr.NamespaceException
import javax.jcr.SimpleCredentials
import javax.naming.InitialContext
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel
import javax.swing.text.html.StyleSheet

import org.apache.jackrabbit.core.jndi.RegistryHelper
import org.mnode.newsagent.FeedReader
import org.mnode.newsagent.FeedReaderImpl
import org.mnode.newsagent.FeedResolverImpl
import org.mnode.newsagent.jcr.JcrFeedCallback
import org.mnode.ousia.DialogExceptionHandler
import org.mnode.ousia.HTMLEditorKitExt
import org.mnode.ousia.HyperlinkBrowser
import org.mnode.ousia.OusiaBuilder
import org.mnode.ousia.HyperlinkBrowser.HyperlinkFeedback
import org.mnode.ousia.layer.StatusLayerUI

try {
	new Socket('localhost', 1338)
	println 'Already running'
	System.exit(0)
}
catch (Exception e) {
}

new File(System.getProperty("user.home"), ".newsagent").mkdir()
def configFile = new File(System.getProperty("user.home"), ".newsagent/config.xml")
configFile.text = Reader.getResourceAsStream("/config.xml").text

def ousia = new OusiaBuilder()

Thread.start {
	ServerSocket server = [1338]
	while(true) {
		try {
			server.accept {}
		}
		finally {
			ousia.doLater {
				frame.visible = true
			}
		}
	}
}

File repositoryLocation = [System.getProperty("user.home"), ".newsagent/data"]

def context = new InitialContext()
RegistryHelper.registerRepository(context, 'newsagent', configFile.absolutePath, repositoryLocation.absolutePath, false)
def repository = context.lookup('newsagent')

def session = repository.login(new SimpleCredentials('readonly', ''.toCharArray()))
Runtime.getRuntime().addShutdownHook({
	RegistryHelper.unregisterRepository(context, 'newsagent')
})

try {
	session.workspace.namespaceRegistry.registerNamespace('mn', 'http://mnode.org/namespace')
}
catch (NamespaceException e) {
	println e.message
}
JcrFeedCallback callback = [node:session.rootNode << 'mn:subscriptions']

FeedReader reader = new FeedReaderImpl()
reader.read(new FeedResolverImpl().resolve("slashdot.org")[0], callback)

ousia.edt {
	imageIcon(id: 'logo64', '/logo64.png')
	imageIcon(id: 'logo48', '/logo48.png')
	imageIcon(id: 'logo32', '/logo32.png')
	imageIcon(id: 'logo16', '/logo16.png')
	
	frame(title: rs('Newsagent Reader'), show: true, defaultCloseOperation: JFrame.EXIT_ON_CLOSE, locationRelativeTo: null, trackingEnabled: true, size: [600, 400],
		iconImages: [logo64.image, logo48.image, logo32.image, logo16.image]) {
		
		borderLayout()
		splitPane(orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 200, continuousLayout: true, oneTouchExpandable: true, dividerSize: 10) {
			splitPane(constraints: 'left', dividerSize: 7) {
				scrollPane(horizontalScrollBarPolicy: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
					treeTable(id: 'subscriptionTree', constraints: 'left', treeTableModel: new SubscriptionTreeTableModel(session.rootNode['mn:subscriptions']))
	                subscriptionTree.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
	                subscriptionTree.selectionModel.valueChanged = {
						def selectedPath = subscriptionTree.getPathForRow(subscriptionTree.selectedRow)
	                    if (selectedPath) {
	                        edt {
	                            entryTable.model = new FeedEntryTableModel(selectedPath.lastPathComponent)
	                        }
	                    }
	                    else {
	                        edt {
	                            entryTable.model = new DefaultTableModel()
	                        }
	                    }
					}
					subscriptionTree.packAll()
				}
				scrollPane(horizontalScrollBarPolicy: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
					table(id: 'entryTable', constraints: 'right')
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
		
	Thread.defaultUncaughtExceptionHandler = new DialogExceptionHandler()
}

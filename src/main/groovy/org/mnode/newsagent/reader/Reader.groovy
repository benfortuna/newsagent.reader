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
import java.awt.Color
import java.awt.Desktop
import java.awt.Font
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

import javax.jcr.NamespaceException
import javax.jcr.SimpleCredentials
import javax.naming.InitialContext
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.ListSelectionModel
import javax.swing.text.html.StyleSheet

import org.apache.jackrabbit.core.jndi.RegistryHelper
import org.jdesktop.swingx.JXTable
import org.mnode.juicer.query.QueryBuilder
import org.mnode.newsagent.FeedReader
import org.mnode.newsagent.FeedReaderImpl
import org.mnode.newsagent.FeedResolverImpl
import org.mnode.newsagent.OpmlImporterImpl
import org.mnode.newsagent.jcr.JcrFeedCallback
import org.mnode.newsagent.jcr.JcrOpmlCallback
import org.mnode.newsagent.util.HtmlDecoder
import org.mnode.ousia.DateTableCellRenderer
import org.mnode.ousia.DialogExceptionHandler
import org.mnode.ousia.HTMLEditorKitExt
import org.mnode.ousia.HyperlinkBrowser
import org.mnode.ousia.OusiaBuilder
import org.mnode.ousia.HyperlinkBrowser.HyperlinkFeedback
import org.mnode.ousia.flamingo.BreadcrumbContextCallback
import org.mnode.ousia.glazedlists.DateExpansionModel
import org.mnode.ousia.layer.StatusLayerUI
import org.pushingpixels.substance.api.fonts.SubstanceFontUtilities

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.TreeList.Format
import ca.odell.glazedlists.gui.TableFormat
import ca.odell.glazedlists.swing.EventTableModel
import ca.odell.glazedlists.swing.TreeTableSupport

try {
	new Socket('localhost', 1337)
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
	ServerSocket server = [1337]
	while(true) {
		try {
			server.accept {}
		}
		finally {
			ousia.doLater {
				newsagentFrame.visible = true
			}
		}
	}
}

//File repositoryLocation = [System.getProperty("user.home"), ".newsagent/data"]
File repositoryLocation = ['target/repository']

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
JcrFeedCallback callback = [node:session.rootNode << 'mn:subscriptions', downloadEnclosures:false]
//
FeedReader reader = new FeedReaderImpl()
//reader.read(new FeedResolverImpl().resolve("slashdot.org")[0], callback)
OpmlImporterImpl importer = []
//importer.importOpml(new FileInputStream('src/test/resources/google-reader-subscriptions.xml'), new JcrOpmlCallback(node: session.rootNode))

def updateFeed
updateFeed = { feedNode ->
  if (feedNode['mn:link']) {
    reader.read new URL(feedNode['mn:link'].string), callback
  }
  else {
    feedNode.nodes.each {
	  updateFeed it
	}
  }
}

FeedResolverImpl feedResolver = []

def subscriptionQuery = new QueryBuilder(session.workspace.queryManager).with {
	query(
		source: selector(nodeType: 'nt:unstructured', name: 'subscriptions'),
		constraint: and(
			constraint1: descendantNode(selectorName: 'subscriptions', path: '/mn:subscriptions'),
			constraint2: propertyExistence(selectorName: 'subscriptions', propertyName: 'mn:status'))
	)
}

ousia.edt {
	lookAndFeel('substance-mariner').fontPolicy = SubstanceFontUtilities.getScaledFontPolicy(1.2)
	
	imageIcon(id: 'logo64', '/logo64.png')
	imageIcon(id: 'logo48', '/logo48.png')
	imageIcon(id: 'logo32', '/logo32.png')
	imageIcon(id: 'logo16', '/logo16.png')
	
	actions {
		action id: 'addSubscriptionAction', name: rs('Add Subscription..'), closure: {
			def subscriptionText = addSubscriptionField.text
			addSubscriptionField.text = null
			doOutside {
				//Desktop.desktop.browse(URI.create('http://basetools.org/coucou'))
				def feedUrls = feedResolver.resolve(subscriptionText)
				if (feedUrls) {
					reader.read feedUrls[0], callback
				}
				
				doLater {
					subscriptionTable.model = new SubscriptionTableModel(subscriptionQuery.execute().nodes.toList())
				}
			}
		}
	}
	
	frame(id: 'newsagentFrame', title: rs('Newsagent Reader'), show: true, defaultCloseOperation: JFrame.EXIT_ON_CLOSE, locationRelativeTo: null, trackingEnabled: true, size: [600, 400],
		iconImages: [logo64.image, logo48.image, logo32.image, logo16.image]) {
		
		borderLayout()
		
		panel(constraints: BorderLayout.NORTH) {
			borderLayout()
			breadcrumbBar(id: 'breadcrumb', new BreadcrumbContextCallback(rootContext: new RootContext(session.rootNode)), throwsExceptions: false)
			textField(id: 'addSubscriptionField', columns: 14, prompt: addSubscriptionAction.getValue('Name'), promptFontStyle: Font.ITALIC, promptForeground: Color.LIGHT_GRAY,
						keyPressed: {e-> if (e.keyCode == KeyEvent.VK_ESCAPE) e.source.text = null}, constraints: BorderLayout.EAST) {
						
				addSubscriptionField.addActionListener addSubscriptionAction
			}
		}
		
		splitPane(orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 200, continuousLayout: true, oneTouchExpandable: true, dividerSize: 10) {
			splitPane(constraints: 'left', dividerSize: 7) {
				scrollPane(horizontalScrollBarPolicy: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
					/*
					treeTable(id: 'subscriptionTree', constraints: 'left', treeTableModel: new SubscriptionTreeTableModel(session.rootNode['mn:subscriptions']))
	                subscriptionTree.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
	                subscriptionTree.selectionModel.valueChanged = {
						def selectedPath = subscriptionTree.getPathForRow(subscriptionTree.selectedRow)
	                    if (selectedPath?.lastPathComponent?.hasProperty('mn:link')) {
	                        edt {
//	                            entryTable.model = new FeedEntryTableModel(selectedPath.lastPathComponent)
								entries.withWriteLock {
									clear()
									selectedPath.lastPathComponent.nodes.each {
										add it
									}
								}
	                        }
	                    }
	                    else {
	                        edt {
//	                            entryTable.model = new DefaultTableModel()
								entries.withWriteLock {
									clear()
								}
	                        }
	                    }
					}
					subscriptionTree.packAll()
					*/
					def subscriptionNodes = subscriptionQuery.execute().nodes.toList()
					table(new JXTable(), showHorizontalLines: false, autoCreateRowSorter: true, id: 'subscriptionTable', constraints: 'left', model: new SubscriptionTableModel(subscriptionNodes))
	                subscriptionTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
					subscriptionTable.setDefaultRenderer(String, new SubscriptionTableCellRenderer(subscriptionNodes))

	                subscriptionTable.selectionModel.valueChanged = { e ->
						if (!e.valueIsAdjusting) {
							if (subscriptionTable.selectedRow >= 0) {
								int subscriptionIndex = subscriptionTable.convertRowIndexToModel(subscriptionTable.selectedRow)
								def subscription = subscriptionNodes[subscriptionIndex]
								edt {
	//	                            entryTable.model = new FeedEntryTableModel(subscription)
									entries.withWriteLock {
										clear()
										subscription.nodes.each {
											add it
										}
									}
									newsagentFrame.title = "${subscription['mn:title'].string} - ${rs('Newsagent Reader')}"
								}
	                        }
							else {
								edt {
	//	                            entryTable.model = new DefaultTableModel()
									entries.withWriteLock {
										clear()
									}
									newsagentFrame.title = rs('Newsagent Reader')
								}
							}
						}
					}
				}
				scrollPane(horizontalScrollBarPolicy: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
					table(id: 'entryTable', constraints: 'right') {
						
						def dateGroup = { date ->
							def today = Calendar.instance
							today.clearTime()
							def yesterday = Calendar.instance
							yesterday.add Calendar.DAY_OF_YEAR, -1
							yesterday.clearTime()
							if (date.time < yesterday.time) {
								return 'Older Items'
							}
							else if (date.time < today.time) {
								return 'Yesterday'
							}
							else {
								return 'Today'
							}
						}
						
						// XXX: global..
						entries = new BasicEventList()
						treeList(filterList(sortedList(entries, comparator: { b, a -> a['mn:date'].date.time <=> b['mn:date'].date.time } as Comparator<?>)),
							 expansionModel: new DateExpansionModel(), format: [
						        allowsChildren: {element -> true},
						        getComparator: {depth -> },
						        getPath: {path, element ->
									path << dateGroup(element['mn:date'].date)
									path << element
								 }
						    ] as Format<?>, id: 'entryTree')
						
						entryTable.model = new EventTableModel<?>(entryTree,
							[
								getColumnCount: {2},
								getColumnName: {column -> switch(column) {
										case 0: return 'Title'
										case 1: return 'Published Date'
										default: return null
									}
								},
								getColumnValue: {object, column -> switch(column) {
									case 0: if (object instanceof String) {
										return object
									} else {
										return HtmlDecoder.decode(object['mn:title'].string).replaceAll(/<(.|\n)*?>/, '')
									}
									case 1: if (!(object instanceof String)) {
										return object['mn:date'].date.time
									}
								}}
							] as TableFormat)

						DefaultNodeTableCellRenderer defaultRenderer = [entryTree, ['Today', 'Yesterday', 'Older Items']]
						defaultRenderer.background = Color.WHITE
						entryTable.setDefaultRenderer(String, defaultRenderer)
						
						DateTableCellRenderer dateRenderer = [defaultRenderer]
						dateRenderer.background = Color.WHITE
			
						// XXX: global..
						ttsupport = TreeTableSupport.install(entryTable, entryTree, 0)
						
						ttsupport.delegateRenderer = defaultRenderer
						entryTable.columnModel.getColumn(1).cellRenderer = defaultRenderer
						entryTable.columnModel.getColumn(1).cellRenderer = dateRenderer
						
						entryTable.selectionModel.valueChanged = { e ->
							if (!e.valueIsAdjusting) {
								def entry
								if (entryTable.selectedRow >= 0) {
									entry = entryTree[entryTable.convertRowIndexToModel(entryTable.selectedRow)]
								}
								if (entry instanceof javax.jcr.Node) {
									doLater {
										contentTitle.text = "<html><strong>${entry['mn:title'].string}</strong><br/>${entry.parent['mn:title'].string} <em>${entry['mn:date'].date.time}</em></html>"
										feedItemContent.editorKit = defaultEditorKit
										feedItemContent.text = entry['mn:description'].string
										feedItemContent.caretPosition = 0
									}
								}
								else {
									contentTitle.text = null
									feedItemContent.text = null
								}
							}
						}
						
						entryTable.mouseClicked = { e ->
							if (e.button == MouseEvent.BUTTON1 && e.clickCount >= 2 && entryTable.selectedRow >= 0) {
								doLater {
									newsagentFrame.contentPane.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
								}
								def selectedItem = entryTree[entryTable.convertRowIndexToModel(entryTable.selectedRow)]
								// feed item..
								if (selectedItem.hasProperty('mn:link')) {
									println selectedItem['mn:link'].string
									doOutside {
										Desktop.desktop.browse(URI.create(selectedItem['mn:link'].string))
//										aggregator.markNodeRead selectedItem.node
										selectedItem.session.save {
											selectedItem['mn:seen'] = true
										}
										doLater {
											activityTable.model.fireTableRowsUpdated activityTable.selectedRow, activityTable.selectedRow
											newsagentFrame.contentPane.cursor = Cursor.defaultCursor
										}
									}
								}
							}
						}

					}
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
	
	doOutside {
		updateFeed session.rootNode['mn:subscriptions']
	}
}

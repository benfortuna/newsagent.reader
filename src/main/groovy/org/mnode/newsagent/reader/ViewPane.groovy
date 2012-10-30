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
import java.awt.Cursor
import java.awt.Desktop
import java.awt.Frame;
import java.awt.event.MouseEvent

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView

import javax.jcr.Session
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities

import org.jdesktop.swingx.JXPanel
import org.jdesktop.swingx.JXTable
import org.mnode.juicer.query.QueryBuilder
import org.mnode.newsagent.util.HtmlDecoder
import org.mnode.ousia.DateTableCellRenderer
import org.mnode.ousia.OusiaBuilder
import org.mnode.ousia.glazedlists.DateExpansionModel
import org.mnode.ousia.layer.StatusLayerUI

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.TreeList.Format
import ca.odell.glazedlists.gui.TableFormat
import ca.odell.glazedlists.swing.EventTableModel
import ca.odell.glazedlists.swing.TreeTableSupport

class ViewPane extends JXPanel {
	
	Session session
	
    def subscriptionNodes
    
    def ttsupport
    
    def entries
    
//    def parent
    def swing
    
	ViewPane(Session session, def actionContext, def swing = new OusiaBuilder()) {
//        this.parent = parent
        this.swing = swing
        this.session = session
//		layout = swing.cardLayout(new SlidingCardLayout(), id: 'slider')
        layout = swing.borderLayout()

        def subscriptionQuery = new QueryBuilder(session.workspace.queryManager).with {
            query(
                source: selector(nodeType: 'nt:unstructured', name: 'subscriptions'),
                constraint: and(
                    constraint1: descendantNode(selectorName: 'subscriptions', path: '/mn:subscriptions'),
                    constraint2: propertyExistence(selectorName: 'subscriptions', propertyName: 'mn:status'))
            )
        }
        println "All Subscriptions Query: [$subscriptionQuery.statement]"
        
        add swing.build {
            panel {
            borderLayout()
            splitPane(orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 200, continuousLayout: true, oneTouchExpandable: true, dividerSize: 10) {
                /*
    			splitPane(constraints: 'left', dividerSize: 7, continuousLayout: true, oneTouchExpandable: true) {
    				scrollPane(horizontalScrollBarPolicy: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
    					subscriptionNodes = subscriptionQuery.execute().nodes.toList()
    					table(new JXTable(), showHorizontalLines: false, autoCreateRowSorter: true, id: 'subscriptionTable', constraints: 'left', model: new SubscriptionTableModel(subscriptionNodes))
    	                subscriptionTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
    					subscriptionTable.setDefaultRenderer(String, new SubscriptionTableCellRenderer(subscriptionNodes))
    
    	                subscriptionTable.selectionModel.valueChanged = { e ->
    						if (!e.valueIsAdjusting) {
                                def frame = SwingUtilities.getWindowAncestor(subscriptionTable)
    							if (subscriptionTable.selectedRow >= 0) {
    								int subscriptionIndex = subscriptionTable.convertRowIndexToModel(subscriptionTable.selectedRow)
    								def subscription = subscriptionNodes[subscriptionIndex]
                                    loadEntries(subscription, frame)
    	                        }
    							else {
    								edt {
    									entries.withWriteLock {
    										clear()
    									}
    									frame.title = rs('Newsagent Reader')
    								}
    							}
    						}
    					}
    				}
    				*/
    				scrollPane(horizontalScrollBarPolicy: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
    					table(id: 'entryTable', constraints: 'right', gridColor: Color.LIGHT_GRAY) {
    						
                            actionContext.entryTable = entryTable
                            
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
    										/*
    										feedItemContent.editorKit = defaultEditorKit
    										feedItemContent.text = entry['mn:description'].string
    										feedItemContent.caretPosition = 0
    										*/
    										Platform.runLater {
    											//view.engine.load(entry['mn:link'].string)
    											view.engine.loadContent(entry['mn:description'].string)
    										}
    									}
    								}
    								else {
    									contentTitle.text = null
    									//feedItemContent.text = null
    								}
    							}
    						}
    						
    						entryTable.mouseClicked = { e ->
    							if (e.button == MouseEvent.BUTTON1 && e.clickCount >= 2 && entryTable.selectedRow >= 0) {
                                    def frame = SwingUtilities.getWindowAncestor(e.source)
    								doLater {
    									frame.contentPane.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
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
    //											entryTable.model.fireTableRowsUpdated entryTable.selectedRow, entryTable.selectedRow
    											frame.contentPane.cursor = Cursor.defaultCursor
    										}
    									}
    								}
    							}
    						}
    
    					}
    				}
//    			}
    			panel(constraints: 'right') {
    				borderLayout()
    				label(constraints: BorderLayout.NORTH, border: emptyBorder(5), id: 'contentTitle')
    				panel(id: 'feedItemView') {
    					borderLayout()
    					def statusLayer = new StatusLayerUI()
    					layer(statusLayer) {
    						/*
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
    						*/
    						container(new JFXPanel(), id: 'jfxPanel') {
    							Platform.runLater {
    								view = new WebView()
    								jfxPanel.scene = new Scene(view)
    							}
    						}
    					}
    				}
    			}
            }
		}
        }
//        add swing.button(text: 'Click Me')
	}
	
    void loadEntries(javax.jcr.Node subscription, Frame frame) {
        swing.edt {
            entries.withWriteLock {
                clear()
                subscription.nodes.each {
                    add it
                }
            }
            frame.title = "${subscription['mn:title'].string} - ${rs('Newsagent Reader')}"
        }
    }
    
	void show(String viewId) {
		layout.show(this, viewId)
	}
}

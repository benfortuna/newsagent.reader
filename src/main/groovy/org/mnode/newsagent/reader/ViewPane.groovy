package org.mnode.newsagent.reader

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseEvent

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView

import javax.jcr.Session;
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.ListSelectionModel

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
    
	ViewPane(Session session, def swing = new OusiaBuilder()) {
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
        
//        add swing.build {
            add swing.splitPane(orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 200, continuousLayout: true, oneTouchExpandable: true, dividerSize: 10) {
    			splitPane(constraints: 'left', dividerSize: 7, continuousLayout: true, oneTouchExpandable: true) {
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
    					subscriptionNodes = subscriptionQuery.execute().nodes.toList()
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
    					table(id: 'entryTable', constraints: 'right', gridColor: Color.LIGHT_GRAY) {
    						
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
    //											entryTable.model.fireTableRowsUpdated entryTable.selectedRow, entryTable.selectedRow
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
//		}
	}
	
	void show(String viewId) {
		layout.show(this, viewId)
	}
}

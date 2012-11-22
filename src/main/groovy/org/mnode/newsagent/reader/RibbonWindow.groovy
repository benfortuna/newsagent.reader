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

import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionListener
import java.awt.event.KeyEvent

import javax.swing.JFileChooser
import javax.swing.JSplitPane;

import org.jdesktop.swingx.prompt.BuddySupport
import org.mnode.juicer.query.QueryBuilder
import org.mnode.newsagent.FeedCallback
import org.mnode.newsagent.FeedReader
import org.mnode.newsagent.FeedReaderImpl
import org.mnode.newsagent.FeedResolverImpl
import org.mnode.newsagent.OpmlCallback
import org.mnode.newsagent.OpmlImporterImpl
import org.mnode.newsagent.jcr.JcrFeedCallback
import org.mnode.newsagent.jcr.JcrOpmlCallback
import org.mnode.newsagent.reader.util.Filters
import org.mnode.newsagent.reader.util.GroupAndSort
import org.mnode.newsagent.util.FeedFetcherCacheImpl
import org.mnode.ousia.OusiaBuilder
import org.mnode.ousia.SlidingCardLayout
import org.mnode.ousia.flamingo.icons.NextSvgIcon
import org.mnode.ousia.flamingo.icons.PowerSvgIcon
import org.mnode.ousia.flamingo.icons.PreviousSvgIcon
import org.mnode.ousia.flamingo.icons.ReloadSvgIcon
import org.mnode.ousia.flamingo.icons.StarSvgIcon
import org.pushingpixels.flamingo.api.bcb.BreadcrumbPathListener
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind
import org.pushingpixels.flamingo.api.common.icon.EmptyResizableIcon
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority

@Slf4j
class RibbonWindow extends JRibbonFrame {

    def actionContext = [] as ObservableMap
    
	final GroupAndSort gas = []
	
    final OpmlImporterImpl importer = []
    
	final FeedResolverImpl feedResolver = []
	
	final FeedReader reader = new FeedReaderImpl(new FeedFetcherCacheImpl('org.mnode.newsagent.reader.feedCache'))
	
	final FeedCallback callback
    
    final OpmlCallback opmlCallback
	
	RibbonWindow(def session, def swing = new OusiaBuilder()) {
	
		callback = new JcrFeedCallback(node:session.rootNode << 'mn:subscriptions', downloadEnclosures:false)
        opmlCallback = new JcrOpmlCallback(node: session.rootNode)
		
        StarSvgIcon searchIcon = []
        StarSvgIcon feedIcon = []
        StarSvgIcon forwardIcon = []
        StarSvgIcon bookmarkIcon = []
        StarSvgIcon okIcon = []
        StarSvgIcon okAllIcon = []
        
		
		def updateFeed
		updateFeed = { feedNode ->
		  if (feedNode['mn:source']) {
			reader.read new URL(feedNode['mn:source'].string), callback
		  }
		  else {
			feedNode.nodes.each {
			  updateFeed it
			}
		  }
		}
		
		swing.build {
            fileChooser(id: 'chooser')
            
			actions {
				action id: 'newAction', name: rs('New Item'), accelerator: shortcut('N'), closure: {
//					System.exit(0)
				}
				action id: 'exitAction', name: rs('Exit'), accelerator: shortcut('Q'), closure: {
					System.exit(0)
				}
				action id: 'aboutAction', name: rs('About'), accelerator: 'F1', closure: {
//					System.exit(0)
                    if (aboutView.showing) {
                        slider.previous(sliderPane)
                    }
                    else {
                        slider.show(sliderPane, 'AboutView')
                    }
//                    sliderPane.show('AboutView')
				}
				action id: 'preferencesAction', name: rs('Preferences'), closure: {
                    contentPane1.show('preferences')
				}
                action id: 'refreshAction', name: rs('Refresh'), closure: {
                }
                
                action id: 'quickSearchAction', name: rs('Search Items'), closure: {
                    if (quickSearchField.text) {
                        def searchQuery = new QueryBuilder(session.workspace.queryManager, session.valueFactory).with {
                            query(
                                source: selector(nodeType: 'nt:unstructured', name: 'items'),
                                constraint: and(
                                    constraint1: descendantNode(selectorName: 'items', path: navigationPane.currentContext.node.path),
                                    constraint2: fullTextSearch(selectorName: 'items', propertyName: 'mn:description', searchTerms: quickSearchField.text)
                                )
                            )
                        }
                        SearchContext pr = [searchQuery, quickSearchField.text]
                        navigationPane.addBreadcrumbContext pr
                    }
                }
                action id: 'markAsReadAction', name: rs('Mark As Read'), closure: {
                    actionContext.markAsRead()
                }
                
                action id: 'markAllReadAction', name: rs('Mark All Read'), closure: {
                    actionContext.markAllRead()
                }
        
                action id: 'deleteAction', name: rs('Delete'), SmallIcon: svgIcon('Delete'), closure: {
                    actionContext.delete()
                }
                action id: 'importFeedsAction', name: rs('Feeds'), closure: {
                    if (chooser.showOpenDialog() == JFileChooser.APPROVE_OPTION) {
                        
                        log.info "Importing subscriptions from: $chooser.selectedFile"
                        Thread.start {
                            try {
                                importer.importOpml(chooser.selectedFile, opmlCallback)
                                updateFeed session.rootNode['mn:subscriptions']
                            } catch (def e) {
                                log.error 'Error importing opml', e
                            }
                        }
                    }
                }
                action id: 'exportFeedsAction', name: rs('Feeds'), closure: {
                    if (chooser.showSaveDialog() == JFileChooser.APPROVE_OPTION) {
                        doOutside {
                            FileWriter writer = [chooser.selectedFile]
                            MarkupBuilder opmlBuilder = [writer]
                            opmlBuilder.opml(version: '1.0') {
                                body {
                                    /*
                                    for (feedNode in session.rootNode.getNode('Feeds').nodes) {
                                        if (!feedNode.hasNode('query')) {
                                            outline(title: "${feedNode.getProperty('title').string}",
                                                xmlUrl: "${feedNode.getProperty('url').string}")
                                        }
                                    }
                                    */
                                }
                            }
                        }
                    }
                }
                action id: 'addFeedAction', name: rs('Add Subscription'), SmallIcon: feedIcon, closure: {
					def subscriptionText = addFeedField.text
					addFeedField.text = null
					doOutside {
						try {
							def feedUrls = feedResolver.resolve(subscriptionText)
							reader.read feedUrls[0], callback
						} catch (def e) {
							reader.read subscriptionText, callback
						} finally {
//							subscriptionNodes = subscriptionQuery.execute().nodes.toList()
//							doLater {
//								subscriptionTable.model = new SubscriptionTableModel(subscriptionNodes)
//							}
						}
					}
                }
                
                action id: 'bookmarkFeedAction', name: rs('Bookmark'), closure: {
                }
			}
		}

		ribbon.applicationMenu = swing.build {
//			def newIcon = ImageWrapperResizableIcon.getIcon(Main.getResource('/add.png'), [16, 16] as Dimension)
			def newIcon = ImageWrapperResizableIcon.getIcon(RibbonWindow.getResource('/add.png'), [16, 16] as Dimension)
			def exitIcon = ImageWrapperResizableIcon.getIcon(RibbonWindow.getResource('/exit.png'), [16, 16] as Dimension)
			def blankIcon = new EmptyResizableIcon(16)
			
			ribbonApplicationMenu(id: 'appMenu') {
				ribbonApplicationMenuEntryPrimary(id: 'newMenu', icon: newIcon, text: rs('New'), kind: CommandButtonKind.POPUP_ONLY)
//				newMenu.addSecondaryMenuGroup 'Create a new item', newAction
                
				appMenu.addMenuSeparator()
				
				ribbonApplicationMenuEntryPrimary(id: 'saveAsMenu', icon: blankIcon, text: rs('Save As'), kind: CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION)
				appMenu.addMenuSeparator()
				
                ribbonApplicationMenuEntrySecondary(id: 'importFeeds', icon: feedIcon, text: rs('Feed Subscriptions'), kind: CommandButtonKind.ACTION_ONLY, actionPerformed: importFeedsAction)
                ribbonApplicationMenuEntrySecondary(id: 'exportFeeds', icon: feedIcon, text: rs('Feed Subscriptions'), kind: CommandButtonKind.ACTION_ONLY, actionPerformed: exportFeedsAction)
    
				ribbonApplicationMenuEntryPrimary(id: 'importMenu', icon: blankIcon, text: rs('Import'), kind: CommandButtonKind.POPUP_ONLY)
                importMenu.addSecondaryMenuGroup 'Import external data', importFeeds
				ribbonApplicationMenuEntryPrimary(id: 'exportMenu', icon: blankIcon, text: rs('Export'), kind: CommandButtonKind.POPUP_ONLY)
                exportMenu.addSecondaryMenuGroup 'Export data', exportFeeds
				appMenu.addMenuSeparator()
				
				ribbonApplicationMenuEntryPrimary(icon: new PowerSvgIcon(), text: rs('Exit'), kind: CommandButtonKind.ACTION_ONLY, actionPerformed: exitAction)
				
				ribbonApplicationMenuEntryFooter(text: rs('Preferences'), actionPerformed: preferencesAction)
			}
		}
		
//		def helpIcon = ImageWrapperResizableIcon.getIcon(Main.getResource('/add.png'), [16, 16] as Dimension)
		StarSvgIcon helpIcon = []
		ribbon.configureHelp helpIcon, swing.aboutAction
        
        def taskIcon = ImageWrapperResizableIcon.getIcon(RibbonWindow.getResource('/task.png'), [16, 16] as Dimension)
        def previousIcon = ImageWrapperResizableIcon.getIcon(RibbonWindow.getResource('/task.png'), [16, 16] as Dimension)
        def nextIcon = ImageWrapperResizableIcon.getIcon(RibbonWindow.getResource('/task.png'), [16, 16] as Dimension)
        def refreshIcon = ImageWrapperResizableIcon.getIcon(RibbonWindow.getResource('/task.png'), [16, 16] as Dimension)
        def cancelLoadIcon = ImageWrapperResizableIcon.getIcon(RibbonWindow.getResource('/task.png'), [16, 16] as Dimension)
		StarSvgIcon clearIcon = [] 
        
		ribbon.addTask swing.build {
			ribbonTask('Home', bands: [
                ribbonBand(rs('Navigate'), icon: taskIcon, id: 'navigationBand', resizePolicies: ['mirror']) {
                    ribbonComponent(
                        component: commandButton(new PreviousSvgIcon(), text: rs('Previous'), id: 'previousButton', actionPerformed: {actionContext.previousItem()} as ActionListener) {
                                bind(source: actionContext, sourceProperty: 'previousItem', target: previousButton, targetProperty: 'enabled', converter: {it != null})
                            },
                        priority: RibbonElementPriority.TOP
                    )
                    ribbonComponent(
                        component: commandButton(new NextSvgIcon(), text: rs('Next'), id: 'nextButton', actionPerformed: {actionContext.nextItem()} as ActionListener) {
                                bind(source: actionContext, sourceProperty: 'nextItem', target: nextButton, targetProperty: 'enabled', converter: {it != null})
                            },
                        priority: RibbonElementPriority.TOP
                    )
                },
            
                ribbonBand(rs('Load'), icon: taskIcon, id: 'loadBand', resizePolicies: ['mirror']) {
                    ribbonComponent(
                        component: commandButton(new ReloadSvgIcon(), action: refreshAction),
                        priority: RibbonElementPriority.TOP
                    )
                    ribbonComponent(
                        component: commandButton(cancelLoadIcon, text: rs('Cancel')),
                        priority: RibbonElementPriority.TOP
                    )
                },
        
				ribbonBand('Quick Search', id: 'quickSearchBand', resizePolicies: ['mirror']) {
					ribbonComponent([
						component: textField(id: 'quickSearchField', columns: 14, enabled: false, prompt: 'Search..', promptFontStyle: Font.ITALIC, promptForeground: Color.LIGHT_GRAY,
							keyPressed: {e-> if (e.keyCode == KeyEvent.VK_ESCAPE) e.source.text = null}) {
							
	                        quickSearchField.addActionListener quickSearchAction
	                        quickSearchField.addBuddy commandButton(searchIcon, enabled: false, actionPerformed: quickSearchAction, id: 'quickSearchButton'), BuddySupport.Position.RIGHT
						},
						rowSpan: 1
					])
		
					ribbonComponent([
						component: checkBox(text: 'Include Archived'),
						rowSpan: 1
					])
					ribbonComponent([
						component: checkBox(text: 'Include Deleted'),
						rowSpan: 1
					])
				}
			])
		}
		
		ribbon.addTask swing.build {
			ribbonTask('View', bands: [
				ribbonBand(rs('Group By'), icon: taskIcon, id: 'groupByBand', resizePolicies: ['mirror']) {
					ribbonComponent([
						component: list(items: [rs('Date'), rs('Source')] as Object[], valueChanged: { e->
							contentPane1.groupEntries(e.source.selectedValue)
						}),
						rowSpan: 2
					])
				},
			
				ribbonBand(rs('Sort By'), icon: taskIcon, id: 'sortBand', resizePolicies: ['mirror']) {
					ribbonComponent([
						component: list(items: gas.sortComparators.keySet() as Object[], valueChanged: { e->
							contentPane1.sortEntries(e.source.selectedValue)
						}),
						rowSpan: 2
					])
		//			commandButton(rs('Sort Order'), commandButtonKind: CommandButtonKind.POPUP_ONLY, popupOrientationKind: CommandButtonPopupOrientationKind.SIDEWARD, popupCallback: {
		//				commandPopupMenu() {
		//					commandToggleMenuButton(rs('Ascending'))
		//					commandToggleMenuButton(rs('Descending'))
		//				}
		//			} as PopupPanelCallback)
				},

				ribbonBand(rs('Filter'), icon: taskIcon, id: 'filterBand', resizePolicies: ['mirror']) {
					ribbonComponent(
						component: textField(columns: 14, prompt: rs('Type To Filter..'), promptFontStyle: Font.ITALIC, promptForeground: Color.LIGHT_GRAY, id: 'filterTextField', keyPressed: {e-> if (e.keyCode == KeyEvent.VK_ESCAPE) e.source.text = null}),
						rowSpan: 1
					) {
						filterTextField.addBuddy commandButton(clearIcon, actionPerformed: {filterTextField.text = null} as ActionListener), BuddySupport.Position.RIGHT
					}
					ribbonComponent(
						component: checkBox(text: rs('Unread Items'), id: 'unreadFilterCheckbox'),
						rowSpan: 1
					)
					ribbonComponent(
						component: checkBox(text: rs('Important Items'), id: 'importantFilterCheckbox'),
						rowSpan: 1
					)
				},
		
				ribbonBand('Show/Hide', id: 'showHideBand', resizePolicies: ['mirror']) {
                    ribbonComponent(
                        component: commandToggleButton(id: 'toggleTableHeader', rs('Table Header'),
                             actionPerformed: { e->
                                 if (e.source.actionModel.selected) {
                                     actionContext.entryTable.tableHeader.visible = true
                                     actionContext.entryTable.tableHeader.preferredSize = null
                                 }
                                 else {
                                     actionContext.entryTable.tableHeader.visible = false
                                     actionContext.entryTable.tableHeader.preferredSize = [-1, 0]
                                 }
                             } as ActionListener) {toggleTableHeader.actionModel.selected = true}, 
                        priority: RibbonElementPriority.TOP
                    )
				},
				
				ribbonBand('Layout', id: 'layoutBand', resizePolicies: ['mirror']) {
					ribbonComponent([
						component: comboBox(items: [rs('Horizontal'), rs('Vertical')] as Object[], editable: false, itemStateChanged: { e->
							if (e.source.selectedItem == 'Horizontal') {
								contentPane1.setOrientation(JSplitPane.HORIZONTAL_SPLIT)
							}
							else {
								contentPane1.setOrientation(JSplitPane.VERTICAL_SPLIT)
							}
						}),
						rowSpan: 1
/*
						component: commandToggleButton(id: 'toggleOrientation', rs('Orientation'),
							 actionPerformed: { e->
								 if (e.source.actionModel.selected) {
									 contentPane1.setOrientation(JSplitPane.HORIZONTAL_SPLIT)
								 }
								 else {
									 contentPane1.setOrientation(JSplitPane.VERTICAL_SPLIT)
								 }
							 } as ActionListener) {toggleTableHeader.actionModel.selected = true},
						priority: RibbonElementPriority.TOP
 */
					])

				},
			])
		}
		
		ribbon.addTask swing.build {
			ribbonTask('Tools', bands: [
				ribbonBand('Advanced', id: 'advancedToolsBand', resizePolicies: ['mirror']),
			])
		}
		
		ribbon.addTask swing.build {
			ribbonTask('Action', bands: [
//				ribbonBand('Action1', id: 'action1Band', resizePolicies: ['mirror']),
                
                ribbonBand(rs('Subscribe'), id: 'feedSubscriptionBand', resizePolicies: ['mirror']) {
                    ribbonComponent(
                        component: textField(id: 'addFeedField', columns: 10, prompt: 'Add subscription..', promptFontStyle: Font.ITALIC, promptForeground: Color.LIGHT_GRAY,
                            keyPressed: {e-> if (e.keyCode == KeyEvent.VK_ESCAPE) e.source.text = null}),
						rowSpan: 1
                    ) {
                        addFeedField.addActionListener addFeedAction
                        addFeedField.addBuddy commandButton(searchIcon, enabled: false, actionPerformed: addFeedAction, id: 'addFeedButton'), BuddySupport.Position.RIGHT
                    }
                },
            
                ribbonBand(rs('Update'), icon: forwardIcon, id: 'updateBand', resizePolicies: ['mirror']) {
                    ribbonComponent([
                        component: commandToggleButton(bookmarkIcon, id: 'bookmarkFeedButton', enabled: false, action: bookmarkFeedAction),
                        priority: RibbonElementPriority.TOP
                    ])
                    ribbonComponent([
                        component: commandButton(okIcon, action: markAsReadAction),
                        priority: RibbonElementPriority.MEDIUM
                    ])
                    ribbonComponent([
                        component: commandButton(okAllIcon, action: markAllReadAction),
                        priority: RibbonElementPriority.MEDIUM
                    ])
                    ribbonComponent([
                        component: commandButton(deleteAction),
                        priority: RibbonElementPriority.MEDIUM
                    ])
                },
    
                ribbonBand(rs('Share'), icon: forwardIcon, id: 'shareBand', resizePolicies: ['mirror']) {
                    ribbonComponent([
                        component: commandButton(rs('Post To Buzz'), actionPerformed: {
                            contentPane1.shareSelectedEntry('http://www.google.com/buzz/post?url=%s')
                        } as ActionListener),
                        priority: RibbonElementPriority.TOP
                    ])
                    ribbonComponent([
                        component: commandButton(rs('Twitter'), actionPerformed: {
                            contentPane1.shareSelectedEntry('http://twitter.com/share?url=%s')
                        } as ActionListener),
                        priority: RibbonElementPriority.MEDIUM
                    ])
                    ribbonComponent([
                        component: commandButton(rs('Facebook'), actionPerformed: {
                            contentPane1.shareSelectedEntry('http://www.facebook.com/sharer.php?u=%s')
                        } as ActionListener),
                        priority: RibbonElementPriority.MEDIUM
                    ])
                    ribbonComponent(
                        component: commandButton(rs('Reddit'), actionPerformed: {
                            /*
                            def selectedItem = activityTree[activityTable.convertRowIndexToModel(activityTable.selectedRow)]
                            // feed item..
                            if (selectedItem.node.hasProperty('link')) {
                                Desktop.desktop.browse(URI.create("http://reddit.com/submit?url=${selectedItem.node.getProperty('link').value.string}&title=${URLEncoder.encode(selectedItem.node.getProperty('title').value.string, 'UTF-8')}"))
                            }
                            */
                            contentPane1.shareSelectedEntry('http://reddit.com/submit?url=%s')
                        } as ActionListener),
                        priority: RibbonElementPriority.MEDIUM
                    )
                }
			])
		}
        
        ribbon.minimized = true
		
        
		add swing.panel(id: 'sliderPane') {
            cardLayout(new SlidingCardLayout(), id: 'slider')
            
            panel(constraints: 'FeedView') {
    			borderLayout()
    			//panel(new BreadcrumbPane(), id: 'breadcrumb', constraints: BorderLayout.NORTH)
    			panel(new NavigationPane(session), id: 'navigationPane', constraints: BorderLayout.NORTH) {
    				navigationPane.addBreadcrumbListener({ e ->
    					edt {
                            filterTextField.text = null
                            unreadFilterCheckbox.selected = false
                            importantFilterCheckbox.selected = false
                            
                            // enable/disable ribbon tasks..
                            quickSearchField.text = null
                            quickSearchField.enabled = !e.source.items[-1].data.leaf
                            quickSearchButton.enabled = !e.source.items[-1].data.leaf
                            
                            doOutside {
                                try {
                                    doLater {
                                        contentPane.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
    //                                    breadcrumb.enabled = false
                                    }
                                    
            						if (e.source.items[-1].data.class == SubscriptionContext) {
                                        def subscription = e.source.items[-1].data.node
                                        title = "${subscription['mn:title'].string} - ${rs('Newsagent Reader')}"
            							contentPane1.loadEntries(subscription)
            						}
            						else if (e.source.items[-1].data.class == TagContext) {
            							def subscriptionNodes = []
            							e.source.items[-1].data.children.each {
            								subscriptionNodes << it.node
            							}
                                        def tag = e.source.items[-1].data.name
                                        title = "$tag - ${rs('Newsagent Reader')}"
            							contentPane1.loadEntries(tag, subscriptionNodes)
            						}
                                } finally {
                                    doLater {
    //                                    activityTable.scrollRectToVisible(activityTable.getCellRect(0, 0, true))
                                        contentPane.cursor = Cursor.defaultCursor
    //                                    breadcrumb.enabled = true
                                    }
                                }
                            }
    					}
    				} as BreadcrumbPathListener)
    			}
    			panel(new ViewPane(session, actionContext, gas, new Filters(swing)), id: 'contentPane1')
    		}
        
            panel(new AboutView(), id: 'aboutView', constraints: 'AboutView')
        }
	}
}

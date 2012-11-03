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
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.lang.invoke.MethodHandleImpl.BindCaller.T

import org.mnode.ousia.OusiaBuilder
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

class RibbonWindow extends JRibbonFrame {

    def actionContext = [] as ObservableMap
    
	RibbonWindow(def session, def swing = new OusiaBuilder()) {
	
		swing.build {
			actions {
				action id: 'newAction', name: rs('New Item'), accelerator: shortcut('N'), closure: {
//					System.exit(0)
				}
				action id: 'exitAction', name: rs('Exit'), accelerator: shortcut('Q'), closure: {
					System.exit(0)
				}
				action id: 'aboutAction', name: rs('About'), accelerator: 'F1', closure: {
//					System.exit(0)
//					slider.show(contentPane1, 'pane1')
                    contentPane1.show('about')
				}
				action id: 'preferencesAction', name: rs('Preferences'), closure: {
                    contentPane1.show('preferences')
				}
                action id: 'refreshAction', name: rs('Refresh'), closure: {
                }
			}
		}
		
		add swing.panel {
			borderLayout()
			//panel(new BreadcrumbPane(), id: 'breadcrumb', constraints: BorderLayout.NORTH)
			panel(new NavigationPane(session), id: 'navigationPane', constraints: BorderLayout.NORTH) {
				navigationPane.addBreadcrumbListener({ e ->
					edt {
						if (e.source.items[-1].data.class == SubscriptionContext) {
							contentPane1.loadEntries(e.source.items[-1].data.node, this)
						}
					}
				} as BreadcrumbPathListener)
			}
			panel(new ViewPane(session, actionContext), id: 'contentPane1')
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
				
				ribbonApplicationMenuEntryPrimary(id: 'importMenu', icon: blankIcon, text: rs('Import'), kind: CommandButtonKind.POPUP_ONLY)
				ribbonApplicationMenuEntryPrimary(id: 'exportMenu', icon: blankIcon, text: rs('Export'), kind: CommandButtonKind.POPUP_ONLY)
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
							
	//                        quickSearchField.addActionListener quickSearchAction
	//                        quickSearchField.addBuddy commandButton(searchIcon, enabled: false, actionPerformed: quickSearchAction, id: 'quickSearchButton'), BuddySupport.Position.RIGHT
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
						component: comboBox(items: [rs('Date'), rs('Source')] as Object[], editable: false, itemStateChanged: { e->
							contentPane1.groupEntries(e.source.selectedItem)
						}),
						rowSpan: 1
					])
				},
			
				ribbonBand(rs('Sort By'), icon: taskIcon, id: 'sortBand', resizePolicies: ['mirror']) {
					ribbonComponent([
						component: comboBox(items: contentPane1.sortComparators.keySet() as Object[], editable: false, itemStateChanged: { e->
							doLater {
								selectedSort = e.source.selectedItem
								sortedActivities.comparator = contentPane1.sortComparators[selectedSort]
							}
						}),
						rowSpan: 1
					])
		//			commandButton(rs('Sort Order'), commandButtonKind: CommandButtonKind.POPUP_ONLY, popupOrientationKind: CommandButtonPopupOrientationKind.SIDEWARD, popupCallback: {
		//				commandPopupMenu() {
		//					commandToggleMenuButton(rs('Ascending'))
		//					commandToggleMenuButton(rs('Descending'))
		//				}
		//			} as PopupPanelCallback)
				},
/*	
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
				},*/
		
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
			])
		}
		
		ribbon.addTask swing.build {
			ribbonTask('Tools', bands: [
				ribbonBand('Advanced', id: 'advancedToolsBand', resizePolicies: ['mirror']),
			])
		}
		
		ribbon.addTask swing.build {
			ribbonTask('Action', bands: [
				ribbonBand('Action1', id: 'action1Band', resizePolicies: ['mirror']),
			])
		}
        
        ribbon.minimized = true
	}
}

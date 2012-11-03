package org.mnode.newsagent.reader.util





import org.mnode.ousia.glazedlists.JCheckboxMatcherEditor

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.Filterator
import ca.odell.glazedlists.TextFilterator
import ca.odell.glazedlists.matchers.MatcherEditor
import ca.odell.glazedlists.swing.TextComponentMatcherEditor

class Filters {

	def filters
	
	Filters(def swing) {
		filters = new BasicEventList<MatcherEditor<?>>()
		filters << new TextComponentMatcherEditor(swing.filterTextField, { baseList, e ->
				baseList << e['mn:title']
		} as TextFilterator, true)
		
		filters << new JCheckboxMatcherEditor(swing.unreadFilterCheckbox, { baseList, e ->
				/*
				if (e['node'].hasProperty('seen')) {
						baseList << !e['node'].getProperty('seen').boolean
				}
				else if (e['node'].hasProperty('flags') && e['node'].flags.values.collect { it.string}.contains('seen')) {
						baseList << false
				}
				else {
						baseList << true
				}
				*/
				baseList << false//!e.seen()
		} as Filterator)
		
		filters << new JCheckboxMatcherEditor(swing.importantFilterCheckbox, { baseList, e ->
//                                                      baseList << (e['node'].flagged && e['node'].flagged.boolean)
				baseList << false// e.flagged()
		} as Filterator)

	}
}

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

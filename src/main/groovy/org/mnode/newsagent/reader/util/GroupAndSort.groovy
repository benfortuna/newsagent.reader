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

class GroupAndSort {
	
	def dateGroup = { date ->
		def today = Calendar.instance
		today.clearTime()
		def yesterday = Calendar.instance
		yesterday.add Calendar.DAY_OF_YEAR, -1
		yesterday.clearTime()
		if (date?.time < yesterday.time) {
			return 'Older Items'
		}
		else if (date?.time < today.time) {
			return 'Yesterday'
		}
		else {
			return 'Today'
		}
	}

	def dateGroupComparator = {a, b ->
		def groups = ['Today', 'Yesterday', 'Older Items']
		groups.indexOf(a) - groups.indexOf(b)
	} as Comparator
	
	def groupComparators = [
		'Date': {a, b -> dateGroupComparator.compare(dateGroup(a['mn:date']?.date), dateGroup(b['mn:date']?.date))} as Comparator,
		'Source': {a, b -> a.parent['mn:title']?.string <=> b.parent['mn:title']?.string} as Comparator
	]
	
	def selectedGroup = 'Date'
	def selectedSort = 'Date'
	
	def sortComparators = [
		'Date': {a, b ->
			int groupSort = groupComparators[selectedGroup].compare(a, b)
			(groupSort != 0) ? groupSort : b['mn:date']?.date <=> a['mn:date']?.date
		} as Comparator,
	
		'Title': {a, b ->
			int groupSort = groupComparators[selectedGroup].compare(a, b)
			groupSort = (groupSort != 0) ? groupSort : a['mn:title'].string <=> b['mn:title'].string
			(groupSort != 0) ? groupSort : b['mn:date']?.date <=> a['mn:date']?.date
		} as Comparator,
	
		'Source': {a, b ->
			int groupSort = groupComparators[selectedGroup].compare(a, b)
			groupSort = (groupSort != 0) ? groupSort : b.parent['mn:title']?.string <=> a.parent['mn:title']?.string
			(groupSort != 0) ? groupSort : b['mn:date']?.date <=> a['mn:date']?.date
		} as Comparator
	]

}

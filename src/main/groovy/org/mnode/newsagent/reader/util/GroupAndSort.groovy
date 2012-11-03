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
		'Date': {a, b -> dateGroupComparator.compare(dateGroup(a['mn:date'].date), dateGroup(b['mn:date'].date))} as Comparator,
		'Source': {a, b -> a.parent['mn:title'].string <=> b.parent['mn:title'].string} as Comparator
	]
	
	def selectedGroup = 'Date'
	def selectedSort = 'Date'
	
	def sortComparators = [
		'Date': {a, b ->
			int groupSort = groupComparators[selectedGroup].compare(a, b)
			(groupSort != 0) ? groupSort : b['mn:date'].date <=> a['mn:date'].date
		} as Comparator,
	
		'Title': {a, b ->
			int groupSort = groupComparators[selectedGroup].compare(a, b)
			groupSort = (groupSort != 0) ? groupSort : a['mn:title'].string <=> b['mn:title'].string
			(groupSort != 0) ? groupSort : b['mn:date'].date <=> a['mn:date'].date
		} as Comparator,
	
		'Source': {a, b ->
			int groupSort = groupComparators[selectedGroup].compare(a, b)
			groupSort = (groupSort != 0) ? groupSort : b.parent['mn:title'].string <=> a.parent['mn:title'].string
			(groupSort != 0) ? groupSort : b['mn:date'].date <=> a['mn:date'].date
		} as Comparator
	]

}

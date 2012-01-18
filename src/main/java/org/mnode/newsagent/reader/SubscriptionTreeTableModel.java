package org.mnode.newsagent.reader;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

public class SubscriptionTreeTableModel extends AbstractTreeTableModel {

	private static final String[] COLUMNS = {"Subscription", "Unread Count"};
	
	public SubscriptionTreeTableModel(Object root) {
		super(root);
	}

	public int getColumnCount() {
		return COLUMNS.length;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMNS[column];
	}
	
	public Object getValueAt(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getChild(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getChildCount(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getIndexOfChild(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

}

package org.mnode.newsagent.reader;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.swing.table.AbstractTableModel;

public class SubscriptionTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String[] COLUMNS = {"Subscription", "Unread Count"};

	private final List<Node> feedNodes;
	
	public SubscriptionTableModel(List<Node> feedNodes) {
		this.feedNodes = feedNodes;
	}
	
	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMNS[column];
	}

	@Override
	public int getRowCount() {
		return feedNodes.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		try {
			final Node feedNode = (Node) feedNodes.get(row);
			
			switch (column) {
				case 0: return (feedNode.hasProperty("mn:title")) ? feedNode.getProperty("mn:title").getString() : feedNode.getName();
				case 1: return feedNode.getNodes().getSize();
				default: return null;
			}
		}
		catch (RepositoryException e) {
			throw new ReaderException(e);
		}
	}

}

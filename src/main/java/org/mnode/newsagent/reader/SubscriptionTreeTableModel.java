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
package org.mnode.newsagent.reader;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

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
	
	public Object getValueAt(Object node, int column) {
		try {
			final Node feedNode = (Node) node;
			
			switch (column) {
				case 0: return (feedNode.hasProperty("mn:title")) ? feedNode.getProperty("mn:title").getString() : feedNode.getName();
				case 1: return ((Node) node).getNodes().getSize();
				default: return null;
			}
		}
		catch (RepositoryException e) {
			throw new ReaderException(e);
		}
	}

	public Object getChild(Object parent, int index) {
		try {
			final NodeIterator nodes = ((Node) parent).getNodes();
			nodes.skip(index);
			if (nodes.hasNext()) {
				return nodes.nextNode();
			}
			return null;
		}
		catch (RepositoryException e) {
			throw new ReaderException(e);
		}
	}

	public int getChildCount(Object node) {
		try {
			if (((Node) node).hasProperty("mn:title")) {
				return 0;
			}
			else {
				return (int) ((Node) node).getNodes().getSize();
			}
		}
		catch (RepositoryException e) {
			throw new ReaderException(e);
		}
	}

	public int getIndexOfChild(Object parent, Object child) {
		try {
			final NodeIterator nodes = ((Node) parent).getNodes();
			int index = 0;
			while (nodes.hasNext()) {
				final Node node = nodes.nextNode();
				if (node.isSame((Node) child)) {
					return index;
				}
				index++;
			}
			return 0;
		}
		catch (RepositoryException e) {
			throw new ReaderException(e);
		}
	}

}

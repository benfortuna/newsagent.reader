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

import groovy.util.logging.Slf4j;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

@Slf4j
public abstract class AbstractNodeTableModel extends AbstractTableModel implements EventListener {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -7446299389161781265L;

	private final Node node;
    
    private final String[] columnNames;
    
    private final Class<?>[] columnClasses;

    public AbstractNodeTableModel(Node node, String[] columns, int eventMask) {
        this(node, columns, null, eventMask);
    }

    public AbstractNodeTableModel(Node node, String[] columns, Class<?>[] classes, int eventMask) {
        this.node = node;
        this.columnNames = columns;
        this.columnClasses = classes;
        try {
            node.getSession().getWorkspace().getObservationManager().addEventListener(this,
                    eventMask, node.getPath(), true, null, null, false);
        }
        catch (RepositoryException e) {
            throw new ReaderException(e);
        }
    }
    
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnClasses != null && columnIndex < columnClasses.length) {
            return columnClasses[columnIndex];
        }
        return super.getColumnClass(columnIndex);
    }
    
    public int getRowCount() {
        try {
            return (int) node.getNodes().getSize();
        } catch (RepositoryException e) {
            throw new ReaderException(e);
        }
    }

    protected final Node getNodeAt(int index) throws RepositoryException {
        NodeIterator nodes = node.getNodes();
        nodes.skip(index);
        return nodes.nextNode();
    }

    protected final Property getPropertyAt(int index) throws RepositoryException {
        PropertyIterator properties = node.getProperties();
        properties.skip(index);
        return properties.nextProperty();
    }
    
    protected final int getIndex(Event event, boolean isProperty) {
        int index = -1;
        try {
            Node childNode = null;
            if (isProperty) {
                childNode = node.getSession().getProperty(event.getPath()).getParent();
            }
            else {
                childNode = node.getSession().getNode(event.getPath());
            }
            // only return index of direct child node..
            if (childNode.getParent().isSame(node)) {
                // XXX: need to iterate through all child nodes to find index.. currently will always return zero.
                index = childNode.getIndex() - 1;
            }
        }
        catch (RepositoryException e) {
            throw new ReaderException(e);
        }
        return index;
    }
    
    public void onEvent(final EventIterator events) {
        final List<Integer> addedIndicies = new ArrayList<Integer>();
        final List<Integer> removedIndicies = new ArrayList<Integer>();
        final List<Integer> changedIndicies = new ArrayList<Integer>();
        while (events.hasNext()) {
            Event e = events.nextEvent();
            if (e.getType() == Event.NODE_ADDED) {
                int index = getIndex(e, false);
                if (index >= 0) {
                    addedIndicies.add(index);
                }
            }
            else if (e.getType() == Event.NODE_REMOVED) {
                int index = getIndex(e, false);
                if (index >= 0) {
                    removedIndicies.add(index);
                }
            }
            else if (e.getType() == Event.PROPERTY_ADDED || e.getType() == Event.PROPERTY_CHANGED) {
                int index = getIndex(e, true);
                if (index >= 0) {
                    changedIndicies.add(index);
                }
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (!addedIndicies.isEmpty()) {
                    fireTableDataChanged();
                }
                if (!removedIndicies.isEmpty()) {
                    fireTableDataChanged();
                }
                if (!changedIndicies.isEmpty()) {
                    fireTableDataChanged();
                }
            }
        });
    }
}

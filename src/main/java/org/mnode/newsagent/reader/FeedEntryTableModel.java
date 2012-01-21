/**
 * This file is part of Coucou.
 *
 * Copyright (c) 2011, Ben Fortuna [fortuna@micronode.com]
 *
 * Coucou is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Coucou is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Coucou.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mnode.newsagent.reader;

import java.util.Date;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;

/**
 * @author Ben
 *
 */
public class FeedEntryTableModel extends AbstractNodeTableModel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6240183082831851408L;

	public FeedEntryTableModel(Node node) {
        super(node, new String[] {"Title", "Last Updated"}, new Class[] {String.class, Date.class},
                Event.NODE_ADDED | Event.NODE_REMOVED);
    }

    public Object getValueAt(int row, int column) {
        try {
            final Node node = getNodeAt(row);
            switch(column) {
                case 0:
                    return node.getProperty("mn:title").getString();
                case 1:
                    if (node.hasProperty("mn:date")) {
                        return node.getProperty("mn:date").getDate().getTime();
                    }
                    
                default: return null;
            }
        }
        catch (RepositoryException e) {
            throw new ReaderException(e);
        }
    }

}

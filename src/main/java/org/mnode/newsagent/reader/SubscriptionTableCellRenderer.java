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

import static org.mnode.juicer.JuicerUtils.hasPropertyValue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.ImageIcon;
import java.util.List;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ca.odell.glazedlists.TreeList;

/**
 * @author Ben
 *
 */
public class SubscriptionTableCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 6498301743998728522L;
    
    private final Font defaultFont;
    private final Font unreadFont;
    
    private final Color defaultForeground;
    private final Color nonItemForeground;
    private final Color flaggedBackground;
    private final Color defaultBackground;
    
//    private final Node parent;
    private final List<Node> items;
    
    public SubscriptionTableCellRenderer(List<Node> items) {
        defaultFont = getFont().deriveFont(Font.ITALIC);
        unreadFont = getFont().deriveFont(Font.BOLD);
        defaultForeground = Color.BLACK;
        nonItemForeground = Color.LIGHT_GRAY;
        flaggedBackground = new Color(255, 255, 0, 32);
        defaultBackground = Color.WHITE;
//        this.parent = parent;
        this.items = items;
        //this.groupNames = groupNames;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

		setForeground(defaultForeground);
    	setBackground(defaultBackground);
		setFont(defaultFont);
    	
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        try {
			Node node = (Node) items.get(table.convertRowIndexToModel(row));
			if (node.hasProperty("mn:icon")) {
				final Binary binary = node.getProperty("mn:icon").getBinary();
				final byte[] iconData = new byte[(int) binary.getSize()];
				binary.read(iconData, 0);
				ImageIcon icon = new ImageIcon(iconData);
				setIcon(icon);
			}
			else {
				setIcon(null);
			}
        }
        catch (Exception e) {
            setFont(defaultFont);
    		setForeground(defaultForeground);
        }
        return this;
    }
}

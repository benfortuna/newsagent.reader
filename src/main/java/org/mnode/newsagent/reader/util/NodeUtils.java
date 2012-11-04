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
package org.mnode.newsagent.reader.util;

import java.io.IOException;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.jdesktop.swingx.icon.EmptyIcon;
import org.mnode.newsagent.reader.ReaderException;

public final class NodeUtils {

	private static final EmptyIcon DEFAULT_ICON = new EmptyIcon(16, 16);
	
	private static final CacheManager CACHE_MANAGER = CacheManager.newInstance();
	
	/**
	 * Private constructor to enforce singleton.
	 */
	private NodeUtils() {
	}
	
	public static Icon getIcon(Node node) {
		Icon nodeIcon = null;
		Element iconElement = null;
		try {
			iconElement = getCache().get(node.getIdentifier());
		}
		catch (RepositoryException re) {
			throw new ReaderException(re);
		}
		if (iconElement != null) {
			nodeIcon = (Icon) iconElement.getValue();
		}
		else {
			try {
				if (node.hasProperty("mn:icon")) {
					final Binary icon = node.getProperty("mn:icon").getBinary();
					final byte[] imageData = new byte[(int) icon.getSize()];
					icon.read(imageData, 0);
					nodeIcon = new ImageIcon(imageData);
					getCache().put(new Element(node.getIdentifier(), nodeIcon));
				}
				else {
					nodeIcon = DEFAULT_ICON;
				}
			} catch (RepositoryException e) {
				throw new ReaderException(e);
			} catch (IOException e) {
				throw new ReaderException(e);
			}
		}
		return nodeIcon;
	}
	
    private static Ehcache getCache() {
        return CACHE_MANAGER.getEhcache("org.mnode.newsagent.reader.iconCache");
    }
}

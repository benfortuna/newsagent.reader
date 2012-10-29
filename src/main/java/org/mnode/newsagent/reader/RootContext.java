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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.swing.Icon;

import org.mnode.ousia.flamingo.BreadcrumbContext;

public class RootContext implements BreadcrumbContext {

	private final Node rootNode;
	
	public RootContext(Node node) {
		this.rootNode = node;
	}
	
	@Override
	public String getName() {
		return null;
	}

	@Override
	public List<? extends BreadcrumbContext> getChildren() {
	    try {
    		final List<BreadcrumbContext> children = new ArrayList<BreadcrumbContext>();
    		children.add(new AllSubscriptionsContext(rootNode.getSession()));
    		
    		try {
    			populateTags(rootNode.getNode("mn:tags"), children);
    		} catch (RepositoryException e) {
    			throw new ReaderException(e);
    		}
    		return children;
	    } catch (RepositoryException re) {
	        throw new ReaderException(re);
	    }
	}

	private void populateTags(Node node, List<BreadcrumbContext> tags) throws RepositoryException {
		final NodeIterator nodes = node.getNodes();
		while (nodes.hasNext()) {
			final Node childNode = nodes.nextNode();
			if (childNode.hasProperty("mn:label")) {
				tags.add(new TagContext(childNode));
			}
			else {
				populateTags(childNode, tags);
			}
		}
	}
	
	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

}

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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.mnode.ousia.flamingo.BreadcrumbContext;

public abstract class AbstractNodeContext implements BreadcrumbContext {

	private final Node node;
	
	public AbstractNodeContext(Node node) {
		this.node = node;
	}

	@Override
	public List<? extends BreadcrumbContext> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public final Icon getIcon() {
		try {
			if (node.hasProperty("mn:icon")) {
				final Binary icon = node.getProperty("mn:icon").getBinary();
				final byte[] imageData = new byte[(int) icon.getSize()];
				icon.read(imageData, 0);
				return new ImageIcon(imageData);
			}
		} catch (RepositoryException e) {
			throw new ReaderException(e);
		} catch (IOException e) {
			throw new ReaderException(e);
		}
		return null;
	}

	public Node getNode() {
		return node;
	}
}

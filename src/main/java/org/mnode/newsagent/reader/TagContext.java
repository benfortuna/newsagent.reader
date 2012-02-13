package org.mnode.newsagent.reader;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class TagContext extends AbstractNodeContext {
	
	public TagContext(Node node) {
		super(node);
	}
	
	@Override
	public String getName() {
		try {
			return getNode().getProperty("mn:label").getString();
		} catch (RepositoryException e) {
			throw new ReaderException(e);
		}
	}

}

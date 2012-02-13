package org.mnode.newsagent.reader;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class SubscriptionContext extends AbstractNodeContext {
	
	public SubscriptionContext(Node node) {
		super(node);
	}
	
	@Override
	public String getName() {
		try {
			return getNode().getProperty("mn:title").getString();
		} catch (RepositoryException e) {
			throw new ReaderException(e);
		}
	}

}
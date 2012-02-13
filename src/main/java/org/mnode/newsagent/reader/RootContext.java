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
		final List<BreadcrumbContext> children = new ArrayList<BreadcrumbContext>();
		children.add(new AllSubscriptionsContext());
		
		try {
			populateSubscriptions(rootNode, children);
		} catch (RepositoryException e) {
			throw new ReaderException(e);
		}
		return children;
	}

	private void populateSubscriptions(Node node, List<BreadcrumbContext> subscriptions) throws RepositoryException {
		final NodeIterator nodes = node.getNodes();
		while (nodes.hasNext()) {
			final Node childNode = nodes.nextNode();
			if (childNode.hasProperty("mn:title")) {
				subscriptions.add(new SubscriptionContext(childNode));
			}
			else {
				populateSubscriptions(childNode, subscriptions);
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

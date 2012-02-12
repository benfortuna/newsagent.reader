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

public class SubscriptionContext implements BreadcrumbContext {

	private final Node subscriptionNode;
	
	public SubscriptionContext(Node node) {
		this.subscriptionNode = node;
	}
	
	@Override
	public String getName() {
		try {
			return subscriptionNode.getProperty("mn:title").getString();
		} catch (RepositoryException e) {
			throw new ReaderException(e);
		}
	}

	@Override
	public List<BreadcrumbContext> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public Icon getIcon() {
		try {
			if (subscriptionNode.hasProperty("mn:icon")) {
				// TODO: read icon stream..
//				return subscriptionNode.getNode("mn:icon").getProperty("jcr:content").getBinary().getStream();
				final Binary icon = subscriptionNode.getProperty("mn:icon").getBinary();
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

}

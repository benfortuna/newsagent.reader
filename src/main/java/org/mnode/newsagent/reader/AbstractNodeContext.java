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

package org.mnode.newsagent.reader;

import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.mnode.ousia.flamingo.BreadcrumbContext;

public class AllSubscriptionsContext implements BreadcrumbContext {

	@Override
	public String getName() {
		return "All Subscriptions";
	}

	@Override
	public List<BreadcrumbContext> getChildren() {
		return Collections.emptyList();
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

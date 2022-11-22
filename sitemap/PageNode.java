package com.shanebow.web.SiteBuilder.sitemap;
/********************************************************************
* @(#)PageNode.java 1.00 20110124
* Copyright © 2011-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* PageNode: Web site page information record.
*
* @author Rick Salamone
* @version 1.00
* 20111024 rts initial demo
* 20160401 rts added no arg ctor for initial creation of blank tree
*******************************************************/
import com.shanebow.web.SiteBuilder.pages.Page;
import javax.swing.tree.DefaultMutableTreeNode;

final class PageNode
	extends DefaultMutableTreeNode
	{
	public PageNode() { super (Page.BLANK); }
	public PageNode( Page aPage ) { super (aPage); }
	public PageNode( Page aPage, PageNode aParent )
		{
		super (aPage);
		if ( aParent != null )
			aParent.add(this);
		}

	final Page page() { return (Page)getUserObject(); }
	}

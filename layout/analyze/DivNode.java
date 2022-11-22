package com.shanebow.web.SiteBuilder.layout;
/********************************************************************
* @(#)DivNode.java 1.00 20110124
* Copyright 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* DivNode: Web site page information record.
*
* @author Rick Salamone
* @version 1.00, 20111024 rts initial demo
*******************************************************/
import com.shanebow.web.SiteBuilder.pages.Page;
import javax.swing.tree.DefaultMutableTreeNode;

final class DivNode
	extends DefaultMutableTreeNode
	{
	public DivNode( Page aPage ) { super (aPage); }
	public DivNode( Page aPage, DivNode aParent )
		{
		super (aPage);
		if ( aParent != null )
			aParent.add(this);
		}

	final Page page() { return (Page)getUserObject(); }
	}

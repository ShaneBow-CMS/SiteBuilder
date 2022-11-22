package com.shanebow.web.SiteBuilder.sitemap;
/********************************************************************
* @(#)PageNodeList.java 1.00 20111025
* Copyright 2010-2011 by Richard T. Salamone, Jr. All rights reserved.
*
* PageNodeList:
*
* @author Rick Salamone
* 20111025 rts cleanup
*******************************************************/
import com.shanebow.web.SiteBuilder.pages.Page;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreePath;

public class PageNodeList
	implements Transferable, Serializable
	{
	private static final long serialVersionUID = 1270874212613332692L;
	/**
	* Data flavor that allows a PageNodeList to be extracted from a transferable
	* object
	*/
	public final static DataFlavor PageNodeArray_FLAVOR
		= new DataFlavor(TreePath[].class,	"PageNode list");
	public final static DataFlavor PageArray_FLAVOR
		= new DataFlavor(Page[].class,	"Page array");

	/**
	* List of flavors for retrieving this PageNodeList
	*/
	protected static DataFlavor[] FLAVORS =
		{
		PageNodeArray_FLAVOR,
		PageArray_FLAVOR,
		};
 
	/**
	* Nodes to transfer
	*/
	protected TreePath[] fNodes;
 
	/**
	* @param selection
	*/
	public PageNodeList(TreePath[] nodes)
		{
		fNodes = nodes;
		}

	public TreePath[] getNodes() { return fNodes; }

	@Override public Object getTransferData(DataFlavor aFlavor)
		throws UnsupportedFlavorException, IOException
		{
		if ( PageNodeArray_FLAVOR.equals(aFlavor))
			return fNodes;
		else if ( PageArray_FLAVOR.equals(aFlavor))
			{
			List<Page> list = new ArrayList<Page>();
			int i = 0;
			for ( TreePath treePath : fNodes )
				{
				PageNode node = (PageNode)treePath.getLastPathComponent();
				list.add(node.page());
				Enumeration children = node.children();
				while (children.hasMoreElements())
					list.add(((PageNode) children.nextElement()).page());
				}
			Page[] pages = list.toArray(new Page[0]);
			return pages;
			}
		else
			throw new UnsupportedFlavorException(aFlavor);
		}
 
	@Override public DataFlavor[] getTransferDataFlavors()
		{
		// TODO Auto-generated method stub
		return FLAVORS;
		}
 
	@Override public boolean isDataFlavorSupported(DataFlavor flavor)
		{
		for (DataFlavor supportedFlavor : getTransferDataFlavors())
			if (supportedFlavor.equals(flavor))
				return true;
		return false;
		}
	}

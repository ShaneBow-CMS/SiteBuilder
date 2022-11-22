package com.shanebow.web.SiteBuilder.sitemap;
/********************************************************************
* @(#)PageTreeTransferHandler.java 1.00 20111024
* Copyright (c) 2010-2011 by Richard T. Salamone, Jr. All rights reserved.
*
* PageTreeTransferHandler: Allows a manager to (re)assign contacts to the
* salesmen.
*
* @author Rick Salamone
* @version 1.00
* 20111024 rts created from apo mgr assign code
*******************************************************/
import com.shanebow.ui.SBDialog;
import com.shanebow.web.SiteBuilder.pages.Page;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.awt.datatransfer.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;

public final class PageTreeTransferHandler
	extends TransferHandler // PageTransferHandler // only uses sayError
	{
	private boolean sayError(String msg)
		{
		SBDialog.error("Sitemap DnD Error", "<HTML><FONT COLOR=RED><B>" + msg );
		return false;
		}

	@Override public boolean canImport(TransferHandler.TransferSupport info)
		{
		JTree.DropLocation dl = (JTree.DropLocation)info.getDropLocation();
		TreePath dropPath = dl.getPath();
		if ( dropPath == null )
			return false;
		if ( info.isDataFlavorSupported(PageNodeList.PageNodeArray_FLAVOR))
			{
			// @TODO: maybe should check to ensure new node not ancestor of parent
			//   via: if (!parent.isDescendent(new node))...
//			PageNode parent = (PageNode)dropPath.getLastPathComponent();
//			for ( transfer nodes )
//			if (!parent.isDescendent(transfer node))
return true;
			}
		return info.isDataFlavorSupported(Page.FLAVOR)
		    || info.isDataFlavorSupported(DataFlavor.stringFlavor);
		}

	@Override public boolean importData(TransferHandler.TransferSupport info)
		{
		if (!info.isDrop()) // ingnore paste
			return false;

		JTree tree = (JTree)info.getComponent(); // get drop target's model
		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();

		JTree.DropLocation dl = (JTree.DropLocation)info.getDropLocation();
		PageNode parent = (PageNode)dl.getPath().getLastPathComponent();
		int childIndex = dl.getChildIndex();
		if ( childIndex == -1 ) // dropped ON the parent node
			childIndex = parent.getChildCount(); // add = insert after other kids

		try // Do the import into the tree
			{
			Transferable t = info.getTransferable();
			if ( info.isDataFlavorSupported(PageNodeList.PageNodeArray_FLAVOR))
				{
				TreePath[] list = (TreePath[])t.getTransferData(PageNodeList.PageNodeArray_FLAVOR);
				for (TreePath tp : list)
					model.insertNodeInto((PageNode)tp.getLastPathComponent(), parent, childIndex++);
				}
			else if ( info.isDataFlavorSupported(Page.FLAVOR))
				{
				Page page = (Page)info.getTransferable()
				                              .getTransferData(Page.FLAVOR);
				model.insertNodeInto(new PageNode(page), parent, childIndex);
				}
			else if (info.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
				String data = (String)t.getTransferData(DataFlavor.stringFlavor);
				Page[] droppedPages = PageTransferHandler.parsePageStringData(data);
				if ( droppedPages == null )
					return sayError("Dropped pages == null??");
				for ( Page page : droppedPages )
					model.insertNodeInto(new PageNode(page), parent, childIndex++);
				}
			else return sayError("Tree doesn't accept a drop of this type.");
			}
		catch(Exception e) { return sayError("OOPS: " + e); }
		return true;
		}

	@Override public int getSourceActions(JComponent c) { return MOVE; }

	@Override protected Transferable createTransferable(JComponent c)
		{
System.out.println("" + c + " createTransferable");
		TreePath[] selection = ((JTree)c).getSelectionPaths();
		return (selection == null)? null : new PageNodeList(selection);
		}

	@Override protected void exportDone(JComponent c, Transferable t, int action)
		{
		if (action == MOVE) // need to remove items imported from the appropriate source
			{
			try
				{
				DefaultTreeModel model = (DefaultTreeModel)((JTree)c).getModel();
				TreePath[] list = (TreePath[]) t.getTransferData(PageNodeList.PageNodeArray_FLAVOR);
				for (TreePath tp : list)
					model.removeNodeFromParent((PageNode) tp.getLastPathComponent());
				}
			catch (Exception e) { sayError("" + c + " Error: " + e); }
			}
		}
	}

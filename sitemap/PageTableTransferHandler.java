package com.shanebow.web.SiteBuilder.sitemap;
/********************************************************************
* @(#)PageTableTransferHandler.java 1.00 20111024
* Copyright 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* PageTableTransferHandler: Extends TransferHandler to support
* PageTable as required by the SiteMap UI.
*
* @author Rick Salamone
* @version 1.00, 20111024 rts initial demo
*******************************************************/
import com.shanebow.web.SiteBuilder.pages.Page;
import com.shanebow.web.SiteBuilder.pages.PageTableModel;
import java.util.*;
import java.awt.Rectangle;
import java.awt.datatransfer.*;
import javax.swing.*;

class PageTableTransferHandler
	extends PageTransferHandler
	{
	private final int fSourceActions;
	public PageTableTransferHandler(int aSourceActions)
		{
		fSourceActions = aSourceActions;
		}

	@Override public boolean canImport(TransferSupport support)
		{
		return support.isDrop()
		    && (support.isDataFlavorSupported(Page.FLAVOR)
		     || support.isDataFlavorSupported(PageNodeList.PageArray_FLAVOR)
		     || support.isDataFlavorSupported(DataFlavor.stringFlavor));
		}

	@Override public boolean importData(TransferSupport support)
		{
		if (!canImport(support))
			return false;

		// Get the pages that are being dropped
		Page[] pages = getTransferPages(support);
		if ( pages == null )
			return false;

		// fetch the drop location
		int row = ((JTable.DropLocation)support.getDropLocation()).getRow();

		JTable table = (JTable)support.getComponent();
		PageTableModel tableModel = (PageTableModel)table.getModel();
		for ( Page page : pages )
			tableModel.add(row++, page);

		Rectangle rect = table.getCellRect(--row, 0, false);
		if (rect != null)
			table.scrollRectToVisible(rect);
		return true;
		}

	@Override public int getSourceActions(JComponent c) { return fSourceActions; }

	@Override protected Transferable createTransferable(JComponent c)
		{
		JTable table = (JTable)c;
		int[] indices = table.getSelectedRows();
		PageTableModel tableModel = (PageTableModel)table.getModel();
if ( indices.length == 1 )
return tableModel.get(indices[0]);

		// String data flavor is transferred as a '\n' separated
		// String of each page object's unique path()
		StringBuffer buff = new StringBuffer();
		for (int row : indices )
			{
			buff.append(tableModel.get(row).path());
			buff.append("\n");
			}
		buff.deleteCharAt(buff.length() - 1); // remove last newline
		return new StringSelection(buff.toString());
		}

	@Override protected void exportDone(JComponent c, Transferable t, int action)
		{
		if (action == MOVE)
			{
			JTable table = (JTable)c;
			PageTableModel tableModel = (PageTableModel)table.getModel();
			Page[] exportedPages = getTransferPages(t);
			for ( Page page : exportedPages )
				tableModel.remove(page);
			}
		} 
	}
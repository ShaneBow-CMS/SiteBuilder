package com.shanebow.web.SiteBuilder.sitemap;
/********************************************************************
* @(#)PageTransferHandler.java 1.00 20111024
* Copyright 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* PageTransferHandler: Extends TransferHandler for Page objects.
*
* @author Rick Salamone
* @version 1.00, 20111024 rts initial demo
*******************************************************/
import com.shanebow.web.SiteBuilder.pages.Page;
import com.shanebow.web.SiteBuilder.pages.SitePages;
import java.util.*;
import java.awt.datatransfer.*;
import javax.swing.*;

public class PageTransferHandler
	extends TransferHandler
	{
	protected final Page[] getTransferPages(TransferSupport info)
		{
		return getTransferPages(info.getTransferable());
		}

	protected final Page[] getTransferPages(Transferable t)
		{
		try // Convert the string that is being transferred into Page[]
			{
			if ( t.isDataFlavorSupported(Page.FLAVOR))
				{
				System.out.println("getTransferPages returning Page itself");
				return new Page[]{(Page)t.getTransferData(Page.FLAVOR)};
				}
			if ( t.isDataFlavorSupported(PageNodeList.PageArray_FLAVOR))
				{
				System.out.println("getTransferPages returning PageArray_FLAVOR");
				return (Page[])t.getTransferData(PageNodeList.PageArray_FLAVOR);
				}
			if ( t.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
				String data = (String)t.getTransferData(DataFlavor.stringFlavor);
				return parsePageStringData(data);
				}
			}
		catch (Exception e) {}
		return null;
		}

	public static Page[] parsePageStringData(String aTransferData)
		{
		String[] paths = aTransferData.split("\n");
		Page[] pages = new Page[paths.length];
		for ( int i = 0; i < paths.length; i++ )
			pages[i] = SitePages.getPage(paths[i]);
		return pages;
		}
	}
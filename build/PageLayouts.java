package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)PageLayouts.java 1.00 20121228
* Copyright © 2012-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* PageLayouts: Creates all the PageLayout objects and allows them to
* be retrieved by name.
*
* @author Rick Salamone
* @version 1.00
* 20121228 rts created
* 20140723 rts now extends Vector rather than enclosing an array
*******************************************************/
import java.io.*;
import java.util.Vector;

public final class PageLayouts
	extends Vector<PageLayout>
	{
	PageLayouts(final File aDir)
		throws Exception
		{
		super();
		File[] files = aDir.listFiles(new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".layout");
				}
			});
		for (File file : files)
			add(new PageLayout(file));
		trimToSize();
		}

	public PageLayout get(String aName)
		{
		for (PageLayout layout : this)
			if (layout.equals(aName)) return layout;
		return null;
		}
	}

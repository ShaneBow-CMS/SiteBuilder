package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)PageMenu.java 1.00 20140721
* Copyright © 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* PageMenu: An interface that is implemented by any objects that
* represent a menu on a web page that is implemented using <li>
* elements for the items.
* This class facilitates making one of the items active.
*
* @author Rick Salamone
* @version 1.00
* 20140721 rts created
*******************************************************/
import java.io.BufferedWriter;
import java.io.IOException;

public interface PageMenu
	{
	public boolean write(BufferedWriter out, String active)
		throws IOException;
	}

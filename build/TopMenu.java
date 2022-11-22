package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)TopMenu.java 1.00 20130917
* Copyright © 2011-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* TopMenu: Constructor reads in a file containing a menu marked up with
* tags as follows:
*   <li id="xxx">....</li>
* Note that there must be exactly one space between the li and the id,
* and they must match this pattern exactly.
* Then the write method will add class="active" to the <li> with id matching
* the active parameter.
*
* @author Rick Salamone
* @version 1.00
* 20130917 rts created
* 20131018 rts support to either fail or only log missing menu specifier
* 20140720 rts added static Map to support multiple menus for site
* 20160111 rts reads various char encodings
*******************************************************/
import com.shanebow.web.SiteBuilder.pages.Page;
import com.shanebow.util.SBMisc;
import java.io.*;
import java.util.*;

class TopMenu
	implements PageMenu
	{
	// Instance Data & Methods
	List<String>  fLines = new ArrayList<String>();
	TopMenu(File aFile)
		throws IOException
		{
		// Load the file into fLines
		BufferedReader stream = null;
		try { stream = SBMisc.utfReader(aFile); }
		catch(Exception e) { throw new IOException(e); }
		try
			{
			String line;
			while ((line = stream.readLine()) != null )
				fLines.add(line);
			}
		finally
			{
			try { stream.close(); }
			catch (Exception e) {}
			}
		}

	@Override public boolean write(BufferedWriter out, String active)
		throws IOException
		{
		boolean found = false;
		String pattern = "<li id=\"" + active + "\"";
		for (String line : fLines)
			{
			if (active != null && line.trim().startsWith(pattern))
				{
				line = line.replace("<li id", "<li class=\"active\" id");
				found = true;
				}
			out.write(line + '\n');
			}
		return found;
		}
	}

package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)SideMenu.java 1.00 20140913
* Copyright © 2014-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* SideMenu: Constructor reads in a file containing a menu marked up with
* tags as follows:
*   <li >....<a href="...">...</a></li>
* Then the write method will add class="active" to the <li> containing
* the href that matches the current page being built.
* Note that the href must satisfy the Page object's equal() method.
* Also, the opening <li> tag should be the first element on a line,
* and the closing </li> tag should be the last element on a line, but
* the entire li element can span one or more lines.
*
* @author Rick Salamone
* @version 1.00
* 20140913 rts created
* 20160111 rts reads various char encodings
*******************************************************/
import com.shanebow.web.SiteBuilder.pages.Page;
import com.shanebow.util.SBMisc;
import java.io.*;
import java.util.*;

class SideMenu
//	implements PageMenu
	{
	List<MenuItem>  fItems = new ArrayList<MenuItem>();

	SideMenu(File aFile)
		throws IOException
		{
		// Load the file into fItems
		BufferedReader stream = null;
		try { stream = SBMisc.utfReader(aFile); }
		catch(Exception e) { throw new IOException(e); }
		try
			{
			String line;
			while ((line = stream.readLine()) != null ) {
				MenuItem item = new MenuItem(line);
				fItems.add(item);
				if (line.contains("<li"))
					while (!line.contains("</li"))
						item.append(line = stream.readLine());
				}
			}
		finally
			{
			try { stream.close(); }
			catch (Exception e) {}
			}
		}

	public void write(BufferedWriter out, Page active)
		throws IOException
		{
		boolean found = false;
		String pattern = "<li id=\"" + active + "\"";
		for (MenuItem item : fItems)
			out.write(item.html(active) + '\n');
		}
	}

	class MenuItem {
		String html;
		String href;

		public MenuItem(String html) {
			this.html = html;
			grabHref(html);
			}

		public void append(String html) {
			this.html += '\n' + html;
			if (href == null)
				grabHref(html);
			}

		private void grabHref(String html) {
			int hrefAt = html.indexOf("href=\"");
			if (hrefAt >= 0) {
				int hrefEnd = html.indexOf("\"", hrefAt + 7);
				href = html.substring(hrefAt + 6, hrefEnd);
				}
			}

		/**
		* Checks if the current page matches the href. If so,
		* the containing <li> has the class "active" appended
		* to it.
		* @return the html for this menu item
		*/
		public String html(Page aCurrentPage) {
			return (aCurrentPage.equals(href))?
				html.replace("<li", "<li class=\"active\"") : html;
			}

		@Override public String toString() {
			return "MenuItem: " + html + "\n   href: " + href;
			}
		}


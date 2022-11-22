package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)ChromeHTMLWriter.java 1.00 20110718
* Copyright © 2011-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* ChromeHTMLWriter: Extends BufferedWriter specifically for creating HTML files.
*
* @author Rick Salamone
* @version 1.00
* 20111213 rts created
* 20130421 rts added a class (red-list) to the chrome sidebar list
* 20130425 rts added ctor with Page argument
* 20140627 rts fixed off by one in build ul and added top level href
* 20140722 rts extends TopMenu
*******************************************************/
import com.shanebow.web.SiteBuilder.pages.Page;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBProperties;
import java.io.*;
import java.util.*;

class ChromeMenu extends TopMenu
	implements PageMenu
	{
	private final List<DropMenu> fMenus = new ArrayList<DropMenu>();

	ChromeMenu(File aFile)
		throws IOException
		{
		super(aFile);

		// Parse fLines to build fMenus
		int lineNum = 0;
		for ( ; lineNum < fLines.size(); lineNum++ )
			{
			String line = fLines.get(lineNum);
			if ( line.indexOf("</div>") >= 0 ) //
				break;
			int dropTagAt = line.indexOf("rel=\"dropmenu");
			if ( dropTagAt < 0 ) // this line doesn't specify a drop menu
				continue;
			String href = null;
			int hrefAt = line.indexOf("href=\"");
			if (hrefAt > 0) {
				hrefAt += "href=\"".length();
				href = line.substring(hrefAt, line.indexOf("\"", hrefAt+1));
			//	System.out.println("chrome Top level href: " + href);
				}
	
			line = line.substring(dropTagAt + "rel=\"dropmenu".length());
			int id = line.charAt(0) - '0';
			int nameStart = line.indexOf(">") + 1;
			int nameEnd   = line.indexOf("<");
			String name   = line.substring(nameStart, nameEnd);
			int startLine = 0, endLine = 0;
			for ( int j = lineNum + 2; j < fLines.size(); j++ )
				if ( fLines.get(j).indexOf("id=\"dropmenu" + id + "\"") >= 0 )
					{ startLine = j+1; break; }
			if ( startLine == 0 )
				throw new IOException("ChromeMenu: "+lineNum+
				                      ", missing <div id=\"dropmenu" + id + "\" ...>");

			for ( int j = startLine; j < fLines.size(); j++ )
				if ( fLines.get(j).indexOf("</div>") >= 0 )
					{ endLine = j; break; }
			if ( endLine == 0 )
				throw new IOException("Line "+startLine
				       + ": missing </div> for <div id=\"dropmenu\"" + id + "\" ...>");
			fMenus.add(new DropMenu(id, name, href, startLine, endLine));
			}
		}

	@Override public boolean write(BufferedWriter out, String active)
		throws IOException
		{
		for (String line : fLines)
			out.write(line + '\n');
		return true;
		}

	String getDropDownAsUL(String aDropDownName)
		{
		DropMenu menu = find(aDropDownName);
		if (menu == null)
			return "<div class=\"chrome-side\"><h1>" + aDropDownName + "</h1>";

		StringBuilder it = new StringBuilder("<div class=\"chrome-side\"><h1>");
		String href = menu.href();
		String h1 = (href != null && !href.equals("#"))?
			"<a href=\"" + href + "\">" + aDropDownName + "</a>" : aDropDownName;

		it.append(h1).append("</h1>\n").append("<ul class=\"red-list\">\n");
		for ( int i = menu.startLine(); i < menu.endLine(); i++ )
			it.append(" <li>").append(fLines.get(i)).append("</li>\n");
		it.append("</ul></div>");
		return it.toString();
		}

	DropMenu find(String aDropDownName)
		{
		for ( DropMenu menu : fMenus )
			if ( menu.equals(aDropDownName))
				return menu;
		return null;
		}
	}

class DropMenu
	{
	private final int fId;   // eg dropmenu1
	private final String fName; // eg about us
	private final String fHref; // top level link
	private final int fStartLine;
	private final int fEndLine;

	public DropMenu( int aId, String aName, String aHref, int aStartLine, int aEndLine )
		{
		fId = aId;
		fName = aName;
		fHref = aHref;
		fStartLine = aStartLine;
		fEndLine = aEndLine;
		}

	public int startLine() { return fStartLine; }
	public int endLine()   { return fEndLine; }
	@Override public String toString() { return fName; }
	public String href() { return fHref; }
	public boolean equals(String aOtherName)
		{
		return fName.equalsIgnoreCase(aOtherName);
		}
	}

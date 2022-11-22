package com.shanebow.web.SiteBuilder.imp;
/********************************************************************
* @(#)ImportHtml.java 1.00 20111024
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* ImportHtml: Work horse for importing a site into SiteBuilder.
* Parses an ".html" file to produce an equivalent ".content" file for
* use by the SiteBuilder
*
* @author Rick Salamone
* @version 1.00, 20111214 rts created
*******************************************************/
import com.shanebow.util.SBMisc;
import com.shanebow.web.html.ParseHTML;
import com.shanebow.web.html.HTMLTag;
import java.io.*;
import java.util.*;

final class ImportHtml
	{
	private String fArgs = "!";
	private String fTitle;

	public ImportHtml()
		{
		
		}

	/**
	* Reads and parses the file passed in which is the site's sitemap.content
	* file ultimately used to create sitemap.html by the builder code.
	* @return PageNode root node of the JTree used to manipulate the tree
	*/
	public void process( File aFile )
		throws Exception
		{
		fArgs = "!";
		fTitle = null;
		FileInputStream fis = null;
		try
			{
			ParseHTML parser = new ParseHTML(fis = new FileInputStream(aFile));
			if ( parser.advanceTo("h1", 1))
				fArgs += parser.cdata();

			while (parser.advanceToTag())
				{
				HTMLTag tag = parser.getTag();
				String tagName = tag.getName().toLowerCase();
				if ( tagName.equals("h2"))
					fTitle = parser.cdata();
				else if ( tagName.equals("div"))
					{
					String tagClass = tag.getAttributeValue("class");
					if (!"banner".equals(tagClass))
						break;
					if ( parser.advanceTo("img", 1))
						{
						String src = tag.getAttributeValue("src");
						src = src.substring(src.lastIndexOf('/') + 1);
						if ( src.startsWith("header_"))
							src = src.substring("header_".length());
						fArgs += "," + src;
						}
					break;
					}
				}
			if ( !parser.advanceTo("div", 3))
				throw new IOException("Didn't find 3 div's");
			String outFile = aFile.getPath().replace(".html", ".content");
outFile = outFile.replaceAll("Lee_Byers_", "TapeWorm_");
outFile = outFile.replaceAll("lee_byers_", "TapeWorm_");
//outFile = outFile.replaceAll("_", "-");
			write(parser, new File(outFile));
			}
		finally { try { fis.close(); } catch (Exception ignore) {} }
		}

	void write(ParseHTML parser, File aFile)
		throws Exception
		{
		PrintWriter out = null;
		try
			{
			out = SBMisc.utfPrintWriter( aFile );
			out.println(fArgs);
			out.println("<h1>" + fTitle + "</h1>");
			int ch;
			int divCount = 0; // check paired div's: +1 for open div, -1 for close
			StringBuffer line = new StringBuffer();
			while ((ch = parser.read()) >= 0)
				{
				char c = (char)ch;
				if ( c == '\n' || c == '\r' )
					{
					if ( line.length() > 0)
						out.println(line.toString());
					line.setLength(0);
					}
				else if ( ch == 0 ) // a Tag
					{
					HTMLTag tag = parser.getTag();
					String tagName = tag.getName().toLowerCase();
					if ( tagName.equals("div")) ++divCount;
					else if ( tagName.equals("/div")
					     && (--divCount < 0)) // more closes than opens
						break; // hit closing of div's from above content in html
					line.append(tag.toString());
					}
				else
					{
					if ( Character.isWhitespace(c))
						{
						int len = line.length();
						if ( len == 0 ) continue;
						if ( len > 60 )
							{
							out.println(line.toString());
							line.setLength(0);
							}
						}
					line.append(c);
					}
				}
			}
		finally
			{
			try { out.close(); } catch (Exception ignore) {}
			}
		}
	}

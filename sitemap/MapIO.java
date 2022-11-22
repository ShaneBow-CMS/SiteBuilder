package com.shanebow.web.SiteBuilder.sitemap;
/********************************************************************
* @(#)MapIO.java 1.00 20111024
* Copyright © 2011-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* MapIO: Reads the sitemap.html file, creating a Page object
* for each list item anchor.
*
* @author Rick Salamone
* @version 1.00
* 20111024 rts created
* 20160111 rts reads various char encodings
*******************************************************/
import com.shanebow.util.SBDate;
import com.shanebow.util.SBMisc;
import com.shanebow.web.html.ParseHTML;
import com.shanebow.web.html.HTMLTag;
import com.shanebow.web.SiteBuilder.pages.Page;
import com.shanebow.web.SiteBuilder.pages.SitePages;
import java.io.*;
import java.util.*;
import javax.swing.tree.*;

final class MapIO
	{
	public MapIO() {}

	/**
	* Reads and parses the file passed in which is the site's sitemap.content
	* file ultimately used to create sitemap.html by the builder code.
	* @return PageNode root node of the JTree used to manipulate the tree
	*/
	PageNode readTree( File aFile )
		throws Exception
		{
		PageNode root = null;
		Stack<PageNode> parents = new Stack<PageNode>();
		int level = -1;
		FileInputStream fis = null;
		try
			{
			ParseHTML parser = new ParseHTML(fis = new FileInputStream(aFile));
			if ( parser.advanceTo("li", 1))
				root = parseNode(parser, null);

			PageNode prevNode = root;
			parents.push(root);
			while (parser.advanceToTag())
				{
				HTMLTag tag = parser.getTag();
				String tagName = tag.getName().toLowerCase();
				if ( tagName.equals("li"))
					prevNode = parseNode(parser, parents.peek());
				else if ( tagName.equals("ul"))
					parents.push(prevNode);
				else if ( tagName.equals("/ul"))
					parents.pop();
		//		else System.out.println("ignore tag: " + tagName);
				}
			return root;
			}
		finally { try { fis.close(); } catch (Exception ignore) {} }
		}

	/**
	* Reads and parses the file passed in which is the site's sitemap.xml
	* file. Each <url> tag is parsed and the associated Page object is
	* updated to reflect inclusion in the sitemap as well as optional settings.
	*/
	void readXml( File aFile )
		throws IOException
		{
		int errors = 0;
		StringBuilder errorMsg = new StringBuilder("<html><b>");
		FileInputStream fis = null;
		try
			{
			Page page = null;
			ParseHTML parser = new ParseHTML(fis = new FileInputStream(aFile));
			if ( !parser.advanceTo("urlset", 1))
				throw( new Exception("Missing <urlset>"));
			while (parser.advanceToTag())
				{
				HTMLTag tag = parser.getTag();
				String tagName = tag.getName().toLowerCase();
				if ( tagName.equals("loc"))
					{
					String path = parser.cdata();
					page = SitePages.getPage(path);
					if ( page == null )
						{
						appendError(++errors, errorMsg, "Page not found: " + path);
						parser.advanceTo("url", 1);
						}
					else page.setInXmlMap(true);
					}
				else if (tagName.equals("changefreq"))
					page.setChangeFreq(parser.cdata());
				else if (tagName.equals("priority"))
					page.setPriority(parser.cdata());
				}
			}
		catch (Exception e) { appendError(++errors, errorMsg, e.toString()); }
		finally { try { fis.close(); } catch (Exception ignore) {} }
		if ( errors > 0 )
			{
			if ( errors > MAX_ERROR_REPORT )
				errorMsg.append("<br>and ").append(errors - MAX_ERROR_REPORT).append(" more...");
			throw new IOException(errorMsg.toString());
			}
		}

	private static final int MAX_ERROR_REPORT = 5;
	private void appendError( int aNumber, StringBuilder aList, String aMsg)
		{
		if ( aNumber > MAX_ERROR_REPORT ) return;
		aList.append("<br>").append(aNumber).append(") ").append(aMsg);
		}

	/**
	* helper method for readTree(): does the grunt work of getting a Page
	* object from an anchor tag and creating a PageNode containing it.
	*/
	private final PageNode parseNode(ParseHTML parser, PageNode aParent)
		throws IOException
		{
		PageNode it = null;
		if ( parser.advanceTo( "a", 1 ))
			{
			HTMLTag tag = parser.getTag();
			String path = tag.getAttributeValue("href");
			String title = parser.cdata();
//			if ( fTitleSuffix != null )
//				title = title.substring(0, fTitleSuffix.length());
			if ( path != null )
				it = new PageNode(SitePages.getByHref(path, title), aParent);
			if (!parser.getTag().getName().equals("/a"))
				parser.advanceTo( "/a", 1 );
			}
		return it;
		}

	void write(PageNode aTreeRoot, File aFile)
		throws Exception
		{
		PrintWriter out = null;
		try
			{
			out = new PrintWriter( aFile );
			out.println("!naked");
			out.println("<h1>Site Map</h1>");
			out.print("<ul>");
			printDescendents(out, aTreeRoot);
			out.println("</ul>");
			}
		finally
			{
			try { out.close(); } catch (Exception ignore) {}
			SitePages.saved(aFile);
			}
		}

	private void printDescendents(PrintWriter out, PageNode node)
		{
		Page page = (Page)node.getUserObject();
		out.println("<li>" + page.anchor());
		if (node.getChildCount() > 0)
			{
			out.println("<ul>");
			Enumeration children = node.children();
			while (children.hasMoreElements())
				printDescendents(out, (PageNode)children.nextElement());
			out.println("</ul>");
			}
		out.println("</li>");
		}

	public void write(PrintWriter out, File aFile)
		throws Exception
		{
		BufferedReader stream = null;
		try
			{
			stream = SBMisc.utfReader(aFile); 
			String line;
			while ((line = stream.readLine()) != null )
				out.println(line);
			}
		finally
			{
			try { if (stream != null) stream.close(); }
			catch (Exception e) {}
			}
		}

	void writeXml(File aFile)
		throws Exception
		{
		PrintWriter out = null;
		List<Page> pageList = SitePages.list();
		try
			{
			out = new PrintWriter( aFile );
			writeXmlStart(out);
			for ( Page page : pageList )
				if ( page.isInXmlMap())
					writeXmlPageEntry(out, page);
			writeXmlEnd(out);
			}
		finally
			{
			try { out.close(); } catch (Exception ignore) {}
			SitePages.saved(aFile);
			}
		}

	private void writeXmlStart(PrintWriter out)
		throws IOException
		{
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<?xml-stylesheet type=\"text/xsl\" href=\"/sitemap.xsl\"?>");
		out.println("<urlset ");
		out.println("		xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" ");
		out.println("		xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		out.println("		xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 ");
		out.println("		http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\"");
		out.println(">");
		}

	private void writeXmlPageEntry(PrintWriter out, Page aPage)
		throws IOException
		{
		long lmod = aPage.updated().getTime();
		String changeFreq = aPage.changeFreq();
		String priority = aPage.priority();

		out.println("<url>");
		out.println("    <loc>" + aPage.url() + "</loc>");
		if ( lmod != 0 ) // "2011-10-16T07:01:29-04:00";
			out.println("    <lastmod>" + SBDate.yyyy_mm_dd(lmod/1000) + "</lastmod>" );
		if ( !changeFreq.isEmpty())
			out.println("    <changefreq>" + changeFreq + "</changefreq>" );
		if ( !priority.isEmpty())
			out.println("    <priority>" + priority + "</priority>" );
		out.println("</url>");
		}

	void writeXmlEnd(PrintWriter out)
		throws IOException
		{
		out.println("</urlset>");
		}
	}

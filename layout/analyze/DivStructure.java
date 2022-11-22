package com.shanebow.web.SiteBuilder.layout;
/********************************************************************
* @(#)DivStructure.java 1.00 20111024
* Copyright © 2011-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* DivStructure: Reads the sitemap.html file, creating a Page object
* for each list item anchor.
*
* @author Rick Salamone
* @version 1.00, 20111024 rts created
*******************************************************/
import com.shanebow.util.SBDate;
import com.shanebow.web.html.ParseHTML;
import com.shanebow.web.html.HTMLTag;
import com.shanebow.web.SiteBuilder.pages.Page;
import com.shanebow.web.SiteBuilder.pages.SitePages;
import java.io.*;
import java.util.*;
import javax.swing.tree.*;

final class DivStructure
	{
	public DivStructure() {}

	/**
	* Reads and parses the file passed in which is the site's sitemap.content
	* file ultimately used to create sitemap.html by the builder code.
	* @return DivNode root node of the JTree used to manipulate the tree
	*/
	DivNode readTree( File aFile )
		throws Exception
		{
		DivNode root = null;
		Stack<DivNode> parents = new Stack<DivNode>();
		int level = -1;
		FileInputStream fis = null;
		try
			{
			ParseHTML parser = new ParseHTML(fis = new FileInputStream(aFile));
			if ( parser.advanceTo("li", 1))
				root = parseNode(parser, null);

			DivNode prevNode = root;
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
	* helper method for readTree(): does the grunt work of getting a Page
	* object from an anchor tag and creating a DivNode containing it.
	*/
	private final DivNode parseNode(ParseHTML parser, DivNode aParent)
		throws IOException
		{
		DivNode it = null;
		if ( parser.advanceTo( "a", 1 ))
			{
			HTMLTag tag = parser.getTag();
			String path = tag.getAttributeValue("href");
			String title = parser.cdata();
//			if ( fTitleSuffix != null )
//				title = title.substring(0, fTitleSuffix.length());
			if ( path != null )
				it = new DivNode(SitePages.getByHref(path, title), aParent);
			if (!parser.getTag().getName().equals("/a"))
				parser.advanceTo( "/a", 1 );
			}
		return it;
		}

	}

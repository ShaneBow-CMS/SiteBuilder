package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)LayoutHTMLWriter.java 1.00 20121227
* Copyright © 2012-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* LayoutHTMLWriter:  Extends HTMLWriter to obtain page layout from
* a layout file.
*
* @author Rick Salamone
* @version 1.00
* 20121227 rts created
* 20131102 rts takes a layout as an argument
* 20130425 rts added ctor with Page argument
* 20130705 rts takes arguments for body tag
* 20130717 rts throws exception if content doesn't start with ! or title tag
* 20130717 rts supports lead tag imediately following title tag
* 20130807 rts moves scripts in content to just above the body closing tag
* 20130814 rts very basic support for reading external divs (no recurse, no script move)
* 20130827 rts ditto for external shared lib files
* 20130917 rts support simple top menu
* 20140529 rts throws exception on unrecognized layout
* 20140609 rts added postcontent() - formerly in close(), so exceptions not caught
* 20140911 rts split out processContent() so it can recursively include to any depth
* 20151022 rts target root not static fix
* 20160111 rts reads various char encodings
* 20160111 rts page can specify its language
* 20160905 rts layouts can have their own head file
*******************************************************/
import com.shanebow.util.SBDate;
import com.shanebow.util.SBMisc;
import com.shanebow.web.SiteBuilder.pages.Page;
import java.util.List;
import java.util.Vector;
import java.io.*;

public final class LayoutHTMLWriter
	extends HTMLWriter
	{
	PageLayout fLayout;

	public LayoutHTMLWriter(File aTargetRoot, Page aPage,
			File aContentFile, boolean aCreateDocTitle, PageLayouts aLayouts)
		throws IOException
		{
		super(aTargetRoot, aPage);
//		content(aContentFile, aCreateDocTitle, aLayouts); // 2nd param: createDocTitle

		flmod = aContentFile.lastModified();

		BufferedReader stream = null;
		try { stream = SBMisc.utfReader(aContentFile); }
		catch(Exception e) { throw new IOException(e); }

		String line = stream.readLine();
		if (line.charAt(0) == '!')
			{
			processArgs(line, aLayouts);
			line = stream.readLine();
			}
		else if (line.trim().charAt(0) != '<')
			throw new IOException("Content must begin with '!' or a title tag");
		if (fLayout == null) // not specified as arg
			fLayout = aLayouts.get("default");

// @TODO the layouts should load their own head
File layoutHead = new File(BuildCriteria.srcDir(), fLayout.toString() + ".head");
System.out.println("Layout head: " + layoutHead);
if (layoutHead.exists())
	process(layoutHead);
else
	writeHeadAssets();
writeHeadOrTail("head");
		String title = parseTitle(line.trim());

		// Parse lead if found
stream.mark(2048);
		fLead = "";
		line = stream.readLine();
//System.out.println("Read: '" + line + "'");
		if (line != null && line.startsWith("<lead>"))
			{
			line = line.substring(6);
			for (boolean done = false; line !=null && !done; )
				{
				if (done = line.endsWith("</lead>"))
					line = line.substring(0, line.length()-7);
				fLead += line;
				if (!done) line = stream.readLine();
				}
			}
		else stream.reset();
//System.out.println("Lead: '" + fLead + "'\n1st true line: '" + line + "'");
		startBody(title, aCreateDocTitle);
		processContent(aContentFile, stream);
		writePrevNext();

		postcontent();
		}

	private void processArgs(String line, PageLayouts aLayouts)
		throws IOException
		{
		String[] pieces = line.substring(1).split(",");
		for ( String arg : pieces )
			{
			if ( arg.isEmpty()) continue;
			else if ( arg.charAt(0) == '<') fPrev = arg.substring(1);
			else if ( arg.charAt(0) == '>') fNext = arg.substring(1);
			else if ( arg.charAt(0) == '$') fAdvert = arg.substring(1);
			else if ( arg.startsWith("layout:")) {
				if ((fLayout = aLayouts.get(arg.substring(7))) == null)
					throw new IOException("Unrecognized Layout: " + arg.substring(7));
				}
			else if ( arg.startsWith("lang"))   fLang = arg.substring(5);
			else if ( arg.startsWith("body:"))  fBodyArgs = " " + arg.substring(5);
			else if ( arg.startsWith("menu:"))  fMenu = arg.substring(5);
			else if ( arg.equalsIgnoreCase("naked")) fNaked = true;
			else if ( arg.endsWith(".jpg") || arg.endsWith(".gif")) fBanner = arg;
			else fMenu = arg; // throw new IOException("Unrecognized arg: " + arg);
//			else throw new IOException("Unrecognized arg: " + arg);
			}
		}

	/**
	* @param String aMasthead - this is the title that is diplayed in
	*        the masthead with the navagation menu - not necessarily the
	*        same as the <title> in the header
	* @param boolean aCreateDocTitle - if true, then the aMasthead is
	*        placed within a <title> tag in the header
	*/
	public void startBody(String aMasthead, boolean aCreateDocTitle)
		throws IOException
		{
		super.startBody(aCreateDocTitle? aMasthead : null);
		if ( fNaked )
			return;
		fLayout.writePrecontent(this, aMasthead);
		}

	public void postcontent()
		throws IOException
		{
		if ( !fNaked )
			fLayout.writePostcontent(this, flmod);
		}
	}

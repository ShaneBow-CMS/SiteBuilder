package com.shanebow.web.SiteBuilder.pages;
/********************************************************************
* @(#)SitePages.java 1.00 20111031
* Copyright © 2011-2017 by Richard T. Salamone, Jr. All rights reserved.
*
* SitePages: Maintains a list of the content that is used to generate
* the web site. 
*
* @version 1.00
* @author Rick Salamone
* 20111031 rts created
* 20121228 rts added layout files to the exclude set
* 20121231 rts accepts title tag on first non-args line
* 20140411 rts handles .php.content extension
* 20151227 rts handles .js.content extension & readTitle more robust
* 20160111 rts using utfReader
* 20170906 rts removed support for obsolete .phpcont extension
*******************************************************/
import com.shanebow.web.SiteBuilder.build.BuildCriteria;
import com.shanebow.util.SBMisc;
import java.awt.AWTEventMulticaster;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import com.shanebow.util.SBProperties;

public class SitePages
	{
	/**
	* The set of all the "content" pages that make up the site (*.html, *.js, *.css, and images)
	*/
	private static final Map<String, Page> PAGE_MAP = new HashMap<String, Page>();

	/**
	* List of the file extenstions that should be ignored when walking the content tree
	*/
	public static final String[] EXCLUDE_EXTS
		= { ".div", ".layout", ".seo", ".head", ".desc", ".keyword", "" };

	private static int fDirCount;
	static { refresh(); } // this line must follow other static variables!!

	static boolean exclude(String aExt)
		{
		for ( String excludedExt : EXCLUDE_EXTS )
			if ( excludedExt.equals(aExt))
				return true;
		return false;
		}

	private static String fHost; // = "http://thaidrills.com";
	/**
	* refresh() populates the PAGE_MAP by walking the site's source directory tree.
	*/
	static void refresh()
		{
fHost = "http://" + SBProperties.get(BuildCriteria.PKEY_DOMAIN).toLowerCase();
		File sourceRoot = SBProperties.getInstance().getFile(BuildCriteria.PKEY_DIR_SRC);
		int prefixLen = sourceRoot.getPath().length();
		PAGE_MAP.clear();
		java.util.Stack<File> dirStack = new java.util.Stack<File>();
		dirStack.push(sourceRoot);
		fDirCount = 0;
		while( !dirStack.empty())
			{
			File dir = dirStack.pop();
			++fDirCount;
			File[] files = dir.listFiles();
			if ( files == null )
				continue;

			for ( File file : files )
				{
				if ( file.isDirectory())
					dirStack.push(file);
				else // check ext and process accordingly
					_add(file, sourceRoot, prefixLen);
				}
			}
		// Ensure that the following pages are always available even for a virgin site
		getByHref("/", "Welcome");
		getByHref("/robots.txt", "robots.txt");
		getByHref("/sitemap.xml", "sitemap");
//		((HashMap)PAGE_MAP).trimToSize();
		}

	/**
	* workhorse for adding a page into the site pages universe. Used by
	* the refresh() method (at startup) which does not fire events as well
	* as the addContent() method which does fire an event.
	*/
	private static Page _add(File file, File sourceRoot, int prefixLen)
		{
		Page page;
		long lmod = file.lastModified();
		long lmodSEO = 0;
		String path = file.getPath().substring(prefixLen).replace('\\', '/');
		int dotAt = path.lastIndexOf('.');
		if (dotAt < 0) return null; // no extension
		String ext = path.substring(dotAt);
		if ( exclude(ext))
			return null;
		if ( ext.equals(".content"))
			{
			String noExt = path.substring(0, dotAt);
			if (noExt.endsWith(".php") // ".php.content" file
			||  noExt.endsWith(".js")) // ".js.content" file
				path = noExt;
			else // normal ".content" file
				path = noExt + ".html";
			try { lmodSEO = new File(sourceRoot, noExt+".seo").lastModified(); }
			catch (Exception noSEOFile) {}
			page = new Page(path, lmod, lmodSEO);

			if (noExt.endsWith(".js")) {// ".js.content" file
				//	log("SitePages._add(%s) set JS title to: '%s'", path, noExt);
				page.setTitle(noExt); // e.g. '/public_html/assets/apps/pdf/jspdf.js'
				}
			else
				readTitle(file, page); // fills in title
			}
		else page = new Page(path, lmod, lmodSEO);
		PAGE_MAP.put(path, page);
		return page;
		}

	public static final List<Page> missingFrom( Collection<Page> aCollection )
		{
		Set<Page> it = new HashSet<Page>(PAGE_MAP.values());
		it.removeAll(aCollection);
		return new ArrayList<Page>(it);
		}

	private static String key(String aHref)
		{
		String it = aHref.startsWith(fHost) ? aHref.substring(fHost.length()) : aHref;
		if ( it.length() == 1 )
			it = "/index.html";
		return it;
		}

	public static final Page getByHref(String aHref, String aTitle)
		{
		String key = key(aHref);
		Page page = PAGE_MAP.get(key);
		if (page == null)
			{
			if (aTitle != null) for (Page p : PAGE_MAP.values())
				if ( aTitle.equals(p.title()))    // maybe user moved file
					return p; // found it!         // try to find the title
			page = new Page(key, 0, 0);
			page.setTitle("??????");
			PAGE_MAP.put(key, page);
			}
		return page;
		}

	public static List<Page> list()
		{
		ArrayList<Page> it = new ArrayList<Page>(PAGE_MAP.values());
		it.trimToSize();
		Collections.sort(it);
		return it;
		}

	public static Page getPage(String path)
		{
		return PAGE_MAP.get(key(path));
		}

	private static void readTitle(File aFile, Page aPage)
		{
		BufferedReader stream = null;
		String title = "?";
		try
			{
			try { stream = SBMisc.utfReader(aFile); }
			catch(Exception e) { throw new IOException(e); }
			for ( int i = 0; !stream.ready(); ) // have to give writer thread time
				{
				if ( ++i >= 3 ) throw new Exception("stream not ready");
				try {Thread.sleep(100);}
				catch (InterruptedException e) {}
				}
			String line = stream.readLine();
			if (line.charAt(0) == '!')
				line = stream.readLine();
			String tag = line.trim().toLowerCase();
			if (tag.startsWith("<h1") || tag.startsWith("<title"))
				title = line.substring(line.indexOf(">")+1,line.lastIndexOf("</"));
			}
		catch (Exception e)
			{
			log("Exception SitePages.readTitle(%s): %s", aFile.getPath(), e.toString());
	//		e.printStackTrace();
			}
		finally
			{
			aPage.setTitle(title);
			try { if (stream != null) stream.close(); }
			catch (Exception e) {}
			}
		}

	private static void log (String fmt, Object... args)
		{
		System.out.println(String.format(fmt,args));
		}

	public static final String ADDED = "added";
	public static final String DELETED = "deleted";
	public static final String EDITED = "edited";
	public static final String PROPERTIES = "props";

	public static void saved(File aFile)
		{
log("contentSaved(%s)", aFile.toString());
		File sourceRoot = SBProperties.getInstance().getFile(BuildCriteria.PKEY_DIR_SRC);
		int prefixLen = sourceRoot.getPath().length();
		String path = aFile.getPath().substring(prefixLen).replace('\\', '/');
		int dotAt = path.lastIndexOf('.');
		if (dotAt < 0) return; // no extension
		String ext = path.substring(dotAt);
		if ( ext.equals(".content"))
			path = path.substring(0,dotAt) + ".html";
		else if ( ext.equals(".seo"))
			{
			Page page = PAGE_MAP.get(path.substring(0,dotAt)+".html");
			if ( page != null )
				{
				page.setLmodSEO(aFile.exists()? aFile.lastModified():0);
				fireActionEvent(page, PROPERTIES);
				}
			return;
			}
		else if ( exclude(ext)) return;

		Page page = getPage(path);
log("   page: " + page );
		if ( page != null ) // saved an existing page
			{
			page.setLmod(aFile.lastModified());
			if ( ext.equals(".content"))
				readTitle(aFile, page);
			fireActionEvent(page, EDITED);
			}
		else if (( page = _add(aFile, sourceRoot, prefixLen )) != null )
			fireActionEvent(page, ADDED);
		}

	static void inform(Page aPage, String aAction)
		{
		fireActionEvent( aPage, aAction );
		}

	/***************** Listener Support **************************/
	private static ActionListener fActionListener = null;

	public static synchronized void addActionListener(ActionListener l)
		 { fActionListener = AWTEventMulticaster.add(fActionListener, l); }
	public static synchronized void removeActionListener(ActionListener l)
		 { fActionListener = AWTEventMulticaster.remove(fActionListener, l); }
	private static void fireActionEvent(Page aPage, String aCommand)
		{
		ActionEvent e = new ActionEvent(aPage, ActionEvent.ACTION_PERFORMED, aCommand);
		if (fActionListener != null)
			fActionListener.actionPerformed(e);
		} 
	}

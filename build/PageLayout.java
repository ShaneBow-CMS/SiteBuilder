package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)PageLayout.java 1.00 20121227
* Copyright © 2012-2017 by Richard T. Salamone, Jr. All rights reserved.
*
* PageLayout: Reads and processes a .layout file into a buffer which
* is later written out to a specific html stream.
*
* @author Rick Salamone
* @version 1.00
* 20121227 rts created
* 20130425 rts keeps a sorted array of dynamic content
* 20130425 rts supports banner, chrome-side, and breadcrumbs
* 20130630 rts can use minus to cancel an include
* 20130717 rts added lead & toc types to dynamic content
* 20130827 rts supports importing from a shared library
* 20130917 rts added top-menu type to dynamic content
* 20131015 rts added sidebar type to dynamic content
* 20140529 rts made class final & added toString
* 20140722 rts menu processing handled by DynamicContent class
* 20140722 rts added lmod() and saves list of component files
* 20140806 rts added tron type to dynamic content
* 20140913 rts added sidemenu type to dynamic content
* 20160111 rts reads various char encodings
* 20160119 rts added getIncludeFile()
* 20170904 rts added variable - support for ~DOMAIN~ & ~SITE_NAME~
*******************************************************/
import com.shanebow.util.SBArray;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBMisc;
import java.io.*;
import java.util.*;

public final class PageLayout
	{
	public static final char INCLUDE_PREFIX= '~'; // 'ø';
	public static final File SKIP_INCLUDE = new File("a:/");
	static void clearMenus() {
		DynamicContent.clearMenus();
		}

	/**
	* just like "new File(dir, name)"
	*   - but throws a friendly exception if the file doesn't exist
	*/
	public static File existingFile(File dir, String name, File caller, int lineNo)
		throws FileNotFoundException
		{
		File it = new File(dir, name);
		if (!it.exists()) {
			if (name.length() > 31)
				name = name.substring(0,28) + "...";
			throw new FileNotFoundException(caller.getName() + " Line " + lineNo + ": include file does not exist: " + name);
			}
		return it;
		}

	/**
	* See if a line in the calling file contains an include directive
	*
	* @return
	*  - null: no include; process the line normally
	*  - SKIP_INCLUDE: line has a minus before the INCLUDE_PREFIX, so skip it
	*  - otherwise a File to include
	* @throws FileNotFoundException if the include File does not exist
	*/
	public static File getIncludeFile(File caller, int lineNo, String line)
		throws FileNotFoundException
		{
		String trimmed = line.trim();
		char first = trimmed.charAt(0);
		char second = trimmed.charAt(1);
		if (first != INCLUDE_PREFIX)
			return (first == '-' && second == INCLUDE_PREFIX)? SKIP_INCLUDE : null;

		return (second == INCLUDE_PREFIX)? existingFile(BuildCriteria.libDir(), trimmed.substring(2),caller,lineNo)
		                                 : existingFile(caller.getParentFile(), trimmed.substring(1),caller,lineNo);
		}

	private final StringBuilder fBuffer = new StringBuilder();
	private final String fName;
	private final SBArray<DynamicContent> fDynamic = new SBArray<DynamicContent>(5);
	private final SBArray<File> fDependencies = new SBArray<File>(5);
	private int fTitlePosition;
	private int fContentPosition;
	private int fDatePosition;
	private long flmod; // cumulative last modified date of dependencies

	public PageLayout(File aFile)
		throws Exception
		{
		String fileName = aFile.getName();
		int dotAt = fileName.lastIndexOf('.');
		fName = (dotAt > 0)? fileName.substring(0, dotAt) : "";
		read(aFile); // recursively read layout and component files
		fDynamic.dump(fName);
	//	fDependencies.dump(fName);
		_lmod();
		}

	private long _lmod()
		{
		long tmp;
		flmod = 0;
		for ( File dependency : fDependencies )
			{
			if (( tmp = dependency.lastModified()) > flmod )
				flmod = tmp;
			}
		return flmod;
		}

	public long lmod() { return flmod; }
	public boolean equals(String aName) { return fName.equalsIgnoreCase(aName); }
	public String toString() { return fName; }

	public void writePrecontent(HTMLWriter aWriter, String aTitle)
		throws IOException
		{
		int fromPos = 0;
		for (DynamicContent section : fDynamic)
			{
			int toPos = section.offset();
			aWriter.append(fBuffer, fromPos, toPos);
			fromPos = toPos;
			if (section.type() == 0)
				break;
			section.write(aWriter);
			}
		fContentPosition = fromPos;
		}

	public void writePostcontent(HTMLWriter aWriter, long almod)
		throws IOException
		{
		int fromPos = fContentPosition;
		for (DynamicContent section : fDynamic)
			{
			int toPos = section.offset();
			if (toPos <= fromPos)
				continue;
			aWriter.append(fBuffer, fromPos, toPos);
			fromPos = toPos;
			section.write(aWriter);
			}
		aWriter.append(fBuffer, fromPos, fBuffer.length());
		}

	private File libFile(String aName)
		throws IOException
		{
		File libDir = BuildCriteria.libDir();
		if (libDir == null)
			throw new IOException("Library directory not set");
		return new File(libDir, aName);
		}

	private void read(File aFile)
		throws Exception
		{
		fDependencies.add(aFile);
		if (aFile.getName().endsWith(".menu")) {
			fDynamic.insert(new DynamicContent(aFile, fBuffer.length()));
			return;
			}
		BufferedReader stream = null;
		try
			{
			try { stream = SBMisc.utfReader(aFile); }
			catch (Exception e) {
				SBLog.write("PageLayout cannot open file: " + aFile + "\n" + e);
 				throw new IOException(e);
				}
			String line;
			for (int lineNo = 1; (line = stream.readLine()) != null; ++lineNo )
				{
				String trimmed = line.trim();
				if (trimmed.length() < 2) {
					fBuffer.append(line).append('\n');
					continue;
					}

				int ampAt = trimmed.indexOf(INCLUDE_PREFIX);
				if (ampAt >= 0) // have an include file marker, or variable
					{
					// do variable substitutions, then recheck line
					line = SiteVars.process(line);
					trimmed = line.trim();
					ampAt = trimmed.indexOf(INCLUDE_PREFIX);
					}

				if (ampAt < 0) { // no special processing required
					fBuffer.append(line).append('\n');
					continue;
					}

				char first = trimmed.charAt(0);
				char second = trimmed.charAt(1);
				if (first != INCLUDE_PREFIX) {
					if (!(first == '-' && second == INCLUDE_PREFIX))
						fBuffer.append(line).append('\n');
					continue;
					}

				// We have a line that begins with the INCLUDE_PREFIX...
				if (second == INCLUDE_PREFIX) // it's a lib include
					read(existingFile(BuildCriteria.libDir(), trimmed.substring(2), aFile, lineNo));

				else // either dynamic or local include
					{
					trimmed = trimmed.substring(1); // chop off INCLUDE_PREFIX
					int type = DynamicContent.type(trimmed);
					if (type < 0) // it's an include
						read(existingFile(aFile.getParentFile(), trimmed, aFile, lineNo));
					else if (type == 8)
						{
						File legacyMenu = existingFile(aFile.getParentFile(), "menu.div", aFile, lineNo);
						fDependencies.add(legacyMenu);
						fDynamic.insert(new DynamicContent(legacyMenu, fBuffer.length()));
						}
					else if (type >= 0)
						fDynamic.insert(new DynamicContent(type, fBuffer.length()));
					}
				}
			}
		finally
			{
			try { if (stream != null) stream.close(); }
			catch (Exception e) {}
			}
		}
	}

class DynamicContent
	{
	private static final String[] TYPES =
		{
		"content",
		"masthead",
		"modified",
		"chrome-side",
		"banner",
		"breadcrumbs",
		"lead",
		"toc",
		"top-menu",
		"sidebar",
		"menu", // menu-file
		"tron",
		"sidemenu",
		};

	private static final Map<File, PageMenu> _menus = new HashMap<File, PageMenu>();

	static void clearMenus() {_menus.clear();}
	private static PageMenu fetch(File aFile)
		throws IOException
		{
		PageMenu it = _menus.get(aFile);
		if (it == null)
			_menus.put(aFile, it = aFile.getName().startsWith("chrome")?
				new ChromeMenu(aFile) : new TopMenu(aFile));
		return it;
		}

	static ChromeMenu findChrome() {
		for (PageMenu menu : _menus.values())
			if (menu instanceof ChromeMenu) return (ChromeMenu)menu;
		return null;
		}

	/**
	* as the layout/div files are read, whenever an INCLUDE_PREFIX is encountered
	* the text after it is compared to the above types via this method
	*/
	public static int type(String aTypeString)
		{
		for (int i=0; i < TYPES.length; i++)
			if (TYPES[i].equals(aTypeString)) return i;

		return -1;
		}

	private final int fType;
	private final int fOffset; // offset in the layout (not finished doc)
	private PageMenu fMenu;

	DynamicContent(File aMenuFile, int aOffset)
		throws IOException
		{
		this(10, aOffset, fetch(aMenuFile));
		}

	DynamicContent(int aType, int aOffset)
		{
		this(aType, aOffset, null);
		}

	DynamicContent(int aType, int aOffset, PageMenu aMenu)
		{
		fType = aType;
		fOffset = aOffset;
		fMenu = (aType == 3)? findChrome() : aMenu;
		}

	public int type() { return fType; }
	public int offset() { return fOffset; }
	@Override public int hashCode() { return fOffset; }
	@Override public String toString() { return TYPES[fType] + " @" + fOffset; }

	public void write(HTMLWriter aWriter)
		throws IOException
		{
		switch (fType)
			{
			case 1: aWriter.writeMastHead(); break;
			case 2: aWriter.writeModified(); break;
			case 3: aWriter.writeChromeSide((ChromeMenu)fMenu); break; // "chrome-side"
			case 4: aWriter.writeBanner();   break;
			case 5: aWriter.writeBreadCrumbs(); break;
			case 6: aWriter.writeLead(); break;
			case 7: aWriter.writeTOC(); break;
//			case 8: aWriter.writeTopMenu(); break;
			case 9: aWriter.writeSideBar(); break;
			case 10: aWriter.writeMenu(fMenu); break;
			case 11: aWriter.writeTron(); break;
			case 12: aWriter.writeSideMenu(); break;
			}
		}
	}

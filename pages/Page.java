package com.shanebow.web.SiteBuilder.pages;
/********************************************************************
* @(#)Page.java 1.00 20110124
* Copyright © 2011-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* Page: Web site page information record.
*
* @author Rick Salamone
* @version 1.00
* 20111024 rts created
* 20140710 rts added dir() to return pages containg folder
* 20140913 rts upgraded equals to handle String repesenting a path
* 20160401 rts added added static Page.BLANK used by SiteMap at startup
*******************************************************/
import com.shanebow.util.SBProperties;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

public final class Page
	implements Transferable, Serializable, Comparable<Page>
	{
	public static final String XX = ""; // Unassigned change freq

	public static final String[] CHANGE_FREQS
		= { XX, "always", "hourly", "daily", "weekly", "monthly", "yearly", "never" };
	public static final String[] PRIORITIES
		= { XX, "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0" };
	public static final DataFlavor FLAVOR = new DataFlavor(Page.class, "Page Info");

	public static final String[] FIELD_NAMES
		= {"Title", "Path", "Updated", "SEO", "Changes", "Priority", "sitemap.xml" };
	public static final Class[] FIELD_TYPES
		= {String.class, String.class, Date.class, Date.class, String.class, String.class, Boolean.class };
	public static final int TITLE=0;
	public static final int PATH=1; // unique "primary key"
	public static final int LMOD=2;
	public static final int LMOD_SEO=3;
	public static final int CHANGE_FREQ=4;
	public static final int PRIORITY=5;
	public static final int IN_XML_MAP=6;

	public static final Page BLANK = new Page("", 0, 0); // used by sitemap for init

	/**
	* Package private constuctor: SitePages is the only
	* class that constructs Page objects!!
	*/
	Page( String aPath, long almod, long almodSEO )
		{
		fPath = aPath;
		int lastSlashAt = aPath.lastIndexOf('/');
		fDir = (lastSlashAt < 0)? "" : aPath.substring(0,  lastSlashAt);
// System.out.println("Page: " + aPath + "\n      " + fDir);
		setLmod(almod);
		flmodSEO = almodSEO;
		fTitle = "";
		}

	private byte lookup(String aValue, String[] aList)
		{
		for (int i=0; i < aList.length; i++)
			if (aList[i].equals(aValue)) return (byte)i;
		return 0;
		}

private static String _host = ""; // "http://thaidrills.com";
	private final String fPath; // url path sans host (e.g. /pub/software/manual/installing-java.html)
	private final String fDir; // containing folder (e.g. /pub/software/manual) - no trailing slash
	private String fTitle; // actually the contents of the page's <h1> tag
	private long flmod; // content last modified date
	private long flmodSEO;  // seo data last modified date
	private byte fChangeFreqIndex;
	private byte fPriorityIndex;
	private boolean fInXmlMap;

	public String  anchor()     { return "<a href=\"" + _host + fPath + "\">" + fTitle +"</a>"; }
	public String  url()        { return _host + fPath; }
	public Date    updated()    { return new Date(flmod); }
	public String  path()       { return fPath; }
	public String  dir()        { return fDir; } // no trailing slash!
	public String  title()      { return fTitle; }
	public Date    seoUpdated() { return new Date(flmodSEO); }
	public Boolean isInXmlMap() { return Boolean.valueOf(fInXmlMap); }
	public String  changeFreq() { return CHANGE_FREQS[fChangeFreqIndex]; }
	public String  priority()   { return PRIORITIES[fPriorityIndex]; }

	@Override public int compareTo(Page other)
		{
		boolean thisIsInRoot = fPath.lastIndexOf('/') == 0;
		boolean thatIsInRoot = other.fPath.lastIndexOf('/') == 0;
		if ( thisIsInRoot && !thatIsInRoot ) return -1;
		if ( !thisIsInRoot && thatIsInRoot ) return 1;
		return fPath.compareTo(other.fPath);
		}

	@Override public boolean equals(Object aThat)
		{
		if ( aThat instanceof Page )
			return this.fPath.equals(((Page)aThat).fPath);
		else if (aThat instanceof String) {
			if (fPath.equals((String)aThat))
				return true;
			else if (((String)aThat).endsWith("/")) {
				return ((fPath.endsWith("index.html") || fPath.endsWith("index.php"))
				    &&   (fDir+'/').equals(aThat));
				}
			}
		return false;
		}

	/**
	* toString() value is displayed int the SiteMap tree
	*/
	@Override public String toString() { return title(); }

	void setTitle(String aTitle) { fTitle = aTitle; }
	public void setInXmlMap(boolean on) { fInXmlMap = on; }
	void setLmod(long almod) { flmod = almod; }
	void setLmodSEO(long almod) { flmodSEO = almod; }
	public void setChangeFreq(String aFreq) { fChangeFreqIndex = lookup(aFreq, CHANGE_FREQS); }
	public void setPriority(String aPriority) { fPriorityIndex = lookup(aPriority, PRIORITIES); }

	public final File sourceFile(String aExt)
		{
		String sourceRoot = SBProperties.get(com.shanebow.web.SiteBuilder.build.BuildCriteria.PKEY_DIR_SRC);
		int dotAt = fPath.lastIndexOf('.');
		String relPath = fPath.substring(0, dotAt) + aExt;
		return new File( sourceRoot, relPath );
		}

	public Object get(int field)
		{
		return (field==TITLE)? title()
		      :(field==PATH)? path()
		      :(field==LMOD)? updated()
		      :(field==LMOD_SEO)? seoUpdated()
		      :(field==CHANGE_FREQ)? changeFreq()
		      :(field==PRIORITY)? priority()
		      :(field==IN_XML_MAP)? isInXmlMap()
		      : null;
		}

	public String getString(int field) { return get(field).toString(); }

	/*************** Drag and Drop Support **************************/
	protected static final DataFlavor[] FLAVORS = { FLAVOR };

	@Override public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException
		{
		if ( !flavor.equals(FLAVOR))
			throw new UnsupportedFlavorException(flavor);
		return this;
		}

	@Override public DataFlavor[] getTransferDataFlavors() { return FLAVORS; }
	@Override public boolean isDataFlavorSupported(DataFlavor flavor)
		{
		return FLAVOR.equals(flavor);
		}
	}

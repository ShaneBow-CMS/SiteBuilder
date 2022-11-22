package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)ContentFileWriter.java 1.00 20110106
* Copyright © 2011-2017 by Richard T. Salamone, Jr. All rights reserved.
*
* ContentFileWriter: Determines if the HTML file needs to be (re)built from
* content and seo files. If so, it creates an HTMLWriter to do the work.
*
* @author Rick Salamone
* @version 1.00
* 20110808 rts created
* 20111016 rts determines if a file needs to be written based on lmod
* 20121228 rts loads PageLayouts to pass to pass to the HTMLWriter
* 20130425 rts eliminated chrome writer (but chrome menu remains)
* 20140411 rts handles .php.content extension
* 20140723 rts using the layout last modified date
* 20141016 rts lmod now checks all files with root matching the file being built
* 20160904 rts modified call to LayoutHTML for layout head files
* 20170906 rts removed support for obsolete .phpcont extension
*******************************************************/
import com.shanebow.web.SiteBuilder.pages.*;
import java.util.List;
import java.io.*;

public final class ContentFileWriter
	{
	private final long flmodLayout; // latest modification date of layout components
	private final PageLayouts fLayouts;
	private final String fSourcePrefix;
	private final String fTargetPrefix;
	private final File fTargetRoot;

	/**
	* Consructor takes the dir where .div files live for date calculations.
	*/
	ContentFileWriter(File sourceRoot, File targetRoot)
		throws IOException
		{
		long tmp, lmod = 0;
		try
			{
			fLayouts = new PageLayouts(sourceRoot);
			for (PageLayout layout : fLayouts)
				if ((tmp = layout.lmod()) > lmod)
					lmod = tmp;
			}
		catch(Exception e) { throw new IOException("Load layouts failed: " + e.getMessage()); }
		flmodLayout = lmod;
System.out.println("***** lmod = " + com.shanebow.util.SBDate.yyyymmdd__hhmmss(lmod/1000));
fSourcePrefix = sourceRoot.toString() + '/';
fTargetRoot = targetRoot;
fTargetPrefix = targetRoot.toString() + '/';
		}

static boolean verbose = false;
	BuildEvent _write(String aSiteDir, final String aFilename, File aSourceDir, String aSourceExt,
		                                          File aTargetDir, String aTargetExt )
		{
		Page page = SitePages.getPage(aSiteDir + '/' + aFilename + aTargetExt);
		// if (verbose)
		//	System.out.println("Site Path: '" + aSiteDir + "' file: " + aFilename + " page: " + page);

		File targetFile  = new File(aTargetDir, aFilename + aTargetExt);
		File seoFile     = new File(aSourceDir, aFilename + ".seo"); // title & meta
		File contentFile = new File(aSourceDir, aFilename + aSourceExt);

		BuildEvent event = new BuildEvent(targetFile); // the return value
		boolean haveSEO = seoFile.exists();
		if ( !haveSEO )
			event.addMessage("NO SEO");
		if ( targetFile.exists()) // see if the target needs to be rebuilt
			{
			/* Note that we need to prime pump with contentFile lmod
			* because .php.content files will not be found by listFiles
			*/
			long lmodPieces = contentFile.lastModified();
			File[] componentFiles = aSourceDir.listFiles(new FileFilter() {
				@Override public boolean accept(File aFile) {
					String name = aFile.getName();
					int dotAt = name.lastIndexOf('.');
					return (dotAt > 0 && aFilename.equals(name.substring(0,dotAt)));
					}
				});
			for (File f : componentFiles)
				if (f.lastModified() > lmodPieces)
					lmodPieces = f.lastModified();

			long lmodHtml = targetFile.lastModified();
			boolean needsUpdate = ( lmodPieces > lmodHtml ) || ( flmodLayout > lmodHtml );
			if ( !needsUpdate )
				return event.skipped();
			}

		LayoutHTMLWriter out = null;
		try
			{
			out = new LayoutHTMLWriter(fTargetRoot, page, contentFile, !haveSEO, fLayouts);
			return event.built();
			}
		catch (Exception e) {
			e.printStackTrace();
			return event.failed( "CFW", e);
			}
		finally { try { if (out != null) out.close(); } catch (Exception x) {} }
		}
	}

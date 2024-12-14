package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)BuildWorker.java	1.00 20110711
* Copyright © 2011-2017 by Richard T. Salamone, Jr. All rights reserved.
*
* BuildWorker: Extends SwingWorker to process files in the background
* of a swing application. Each file is passed off to the specified
* FileWriter for processing. 
*
* @author Rick Salamone
* @version 1.00
* 20110711 rts created
* 20110919 rts checks for new files on copy (not create from content yet)
* 20110928 rts css now has preprocessor for color variables
* 20110929 rts property to optionally build catalog/product pages
* 20111018 rts modified to walk arbitrary directory structure
* 20120705 rts moved loadChromeMenu call here and now report any exception
* 20120711 rts useChromeMenu is now read from properties
* 20130116 rts doInBackground publishes a BuildEvent when there is fatal exception
* 20130826 rts added php processing
* 20130917 rts support simple top menu
* 20130919 rts copies .sql files to target
* 20140411 rts handles .php.content extension
* 20140722 rts useChrome removed - handled by layout now
* 20150831 rts decoupled (unused) catalog code
* 20151022 rts added support for dir files
* 20151029 rts copies .tmpl files to target
* 20151211 rts copy all (newer) files from an include dir
* 20151227 rts handles .js.content extension
* 20160111 rts using utfReader
* 20160117 rts includeDir file can now specify files as well as directories
* 20160401 rts added build event for creating target directory
* 20160918 rts comments for dir files
* 20170906 rts removed support for obsolete .phpcont extension
*******************************************************/
import static com.shanebow.web.SiteBuilder.build.PageLayout.INCLUDE_PREFIX;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBMisc;
import com.shanebow.util.SBProperties;
import java.io.*;
import java.util.List;
import javax.swing.SwingWorker;

public class BuildWorker
	extends SwingWorker<Void, BuildEvent>
	{
	private long fStartTime;   // when processing began
	private int  fDirCount;
	private CSSProcessor noLayoutProcess; // for css & php does recursive includes

	public BuildWorker()
		{
		fStartTime = SBDate.timeNow();
		}

	@Override public Void doInBackground()
		{
		SBProperties props = SBProperties.getInstance();
		File sourceRoot = null;
		try
			{
			sourceRoot = props.getFile(BuildCriteria.PKEY_DIR_SRC);
			File targetRoot = props.getFile(BuildCriteria.PKEY_DIR_DEST);
			BuildEvent._RootPathLen = targetRoot.getPath().length();
SiteVars.load();

			noLayoutProcess = new CSSProcessor();
			PageLayout.clearMenus();
			walkin(sourceRoot, targetRoot, false);
			}
		catch (Throwable t)
			{
			if (sourceRoot == null) sourceRoot = new File(".");
			publish(new BuildEvent(sourceRoot, t));
//			t.printStackTrace();
			}
		// sleep to let swing worker 'catch up' before we fire off that we're done
		try { Thread.sleep(300); } catch(InterruptedException e) {}
		return (Void)null;
		}

	private void walkin(File sourceRoot, File targetRoot, boolean isIncludeDir)
		throws Exception
		{
 		log("Build.walkin(" + sourceRoot + ", " + targetRoot + ")");
if (isIncludeDir) System.out.println("Build.including(" + sourceRoot + ", " + targetRoot + ")");
//		FileCopier copyFile = new FileCopier();
//		CSSProcessor noLayoutProcess = new CSSProcessor();

		ContentFileWriter contentWriter = new ContentFileWriter(sourceRoot, targetRoot);

		int prefixLen = sourceRoot.getPath().length();
		java.util.Stack<File> dirStack = new java.util.Stack<File>();
		dirStack.push(sourceRoot);
		while( !dirStack.empty() && !isCancelled())
			{
			File dir = dirStack.pop();
			++fDirCount;
			File[] files = dir.listFiles();
			if ( files == null )
				continue;

			String subDir = dir.getPath().substring(prefixLen);
			String siteDir = subDir.replace('\\', '/');
			File fromDir = new File(sourceRoot, subDir);
			int fromDirLen = fromDir.getPath().length();
			File toDir = new File(targetRoot, subDir);
			if ( !toDir.isDirectory()) { // create target dir if necessary
				toDir.mkdir();
				System.out.println("## BuildWorker mkdir: " + toDir);
				publish(new BuildEvent(toDir, "mkdir"));
				}

			for ( File file : files ) {
				String filename = file.getName();
				if ( file.isDirectory()) {
					if (!filename.startsWith(".")) dirStack.push(file);
					else System.out.println("build skipped dot file: '" + filename + "'");
					}
				else { // check ext and process accordingly
					int dotAt = filename.lastIndexOf('.');
					if (dotAt < 0) continue; // no extension
					String ext = filename.substring(dotAt);
					if ( ext.equals(".content")) {
						if (filename.endsWith(".js.content")) { // for js targets just do includes
							publish ( noLayoutProcess.ifNewer( file, new File(toDir, filename.substring(0,dotAt))));
							}
						else { // for php & html targets apply a template
							String targetExt;
							if (filename.endsWith(".php.content")) {
								ext = ".php.content";
								targetExt = ".php";
								dotAt -= 4;
								}
							else targetExt = ".html";
							String name = filename.substring(0,dotAt);
							publish( contentWriter._write(siteDir, name, fromDir, ext, toDir, targetExt));
							}
						}
					else if ( isIncludeDir
					       || ext.equals(".js")
					       || ext.equals(".sql")
					       || ext.equals(".csv")
					       || ext.equals(".tmpl")
					       || ext.equals(".svg")
					       || ext.equals(".mp3")
					       || ext.equals(".xml")
					       || ext.equals(".xsl")
					       || ext.equals(".htaccess")
					       || filename.equals("robots.txt"))
						publish( copyIfNewer( file, new File(toDir, filename)));
					else if ( ext.equals(".php") || ext.equals(".css"))
						publish ( noLayoutProcess.ifNewer( file, new File(toDir, filename)));
					else if ( ext.equals(".dir"))
						includeDir(file, toDir);
					}
				}
			}
		}

	private void includeDir(File file, File toDir)
		{
		File source; // calc source dir - can be relative, absolute, or lib
		String toSub = file.getName(); // destination sub dir is file name sans ".dir"
		toSub = toSub.substring(0, toSub.length() - 4);
		File dest = new File(toDir, toSub);

		BufferedReader in = null;
		try {
			in = SBMisc.utfReader(file);
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();

				if (line.length() <= 0      // ignore blank lines,
				|| line.charAt(0) == '-'    // disabled includes,
				|| line.charAt(0) == '#' )  // and comments
					continue;

				if (line.charAt(0) == INCLUDE_PREFIX ) {
					if (line.charAt(1) == INCLUDE_PREFIX ) { // ie lib include
						File libDir = BuildCriteria.libDir();
						if (libDir == null)
							throw new IOException("Cannot import " + line + ": Library directory not set");
						source = new File(libDir, line = line.substring(2));
						}
					else source = new File(file.getParent(), line = line.substring(1));
					}
				else source = new File(line);
				if (!source.exists())
					throw new FileNotFoundException("Dir include source '" + source + "' does not exist");
				if (!source.isDirectory()) {
					int slashAt = line.lastIndexOf('/');
					if (slashAt >= 0) line = line.substring(slashAt);
		// noLayoutProcess.ifNewer( source, new File(dest, line))
					publish( copyIfNewer( source, new File(dest, line)));
					}
				else walkin(source, dest, true);
				}
			}
		catch (Exception e) { publish(new BuildEvent(file, e)); return; }
		finally { try { in.close(); } catch (Exception ignore) {} }
		}

	private BuildEvent copyIfNewer(File f1, File f2)
		{
		BuildEvent event = new BuildEvent(f2);
		if ( f2.exists()
		&&   f2.lastModified() >= f1.lastModified())
			return event.skipped();
		try
			{
			FileCopier.copy( f1, f2, false);
			return event.copied();
			}
		catch (Exception e) { return event.failed("copy if newer", e); }
		}

	private void log (String fmt, Object... args)
		{
		System.out.println(String.format(fmt,args));
		}

	public long  getElapsedSeconds() { return SBDate.timeNow() - fStartTime; }
	}

package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)HTMLWriter.java 1.00 20110718
* Copyright © 2011-2018 by Richard T. Salamone, Jr. All rights reserved.
*
* HTMLWriter: Extends BufferedWriter specifically for creating HTML files.
*
* @author Rick Salamone
* @version 1.00
* 20110718 rts created
* 20110808 rts added write(File) method
* 20111002 rts added processArgs for first content line starts with '!'
* 20111016 rts writes lmod date in footer
* 20111116 rts added support for member only content
* 20121231 rts accepts title tag on first non-args line
* 20130124 rts support for YUI css
* 20130130 rts fixed bug when no js files specified
* 20130130 rts support for yui-min.js and yui version
* 20130425 rts support for breadcrumbs and other PageLayout callbacks
* 20130705 rts takes arguments for body tag
* 20130717 rts added writeLead & bug fix parseTitle if whitespace
* 20130807 rts moves scripts in content to just above the body tag
* 20130815 rts allow absolute css file spec
* 20130917 rts support simple top menu
* 20131015 rts added writeSideBar()
* 20131020 rts added recursive write(File) method only for side bar now
* 20140627 rts chrome-side uses a class for styling and added top level link
* 20140710 rts writeSideBar() checks for sidebar.div in file's folder
* 20140722 rts removed static top menu loading & vars - now handled in layout
* 20140806 rts added tron type to dynamic content
* 20141016 rts moved processFile here from LayoutHTMLWriter, now includes work better
* 20150307 rts uses the configured dir for css files
* 20160111 rts reads various char encodings
* 20160119 rts simplified and fixed include processing with PageLayout.getIncludeFile()
* 20160124 rts head buffering just sets fOut, so head files now support recursive include
* 20160210 rts added writeTail to add contents of tail file at end of document
* 20160418 rts log problems with writeTail (i.e. included file not found) - see TODO
* 20160823 rts special processing if css dir set to assets
* 20160823 rts look for sidebar.div walks up dir tree
* 20160904 rts removed member only/session processing
* 20161211 rts added style substitutions within style tag (recurses)
* 20170904 rts calls processVariables()
*******************************************************/
import static com.shanebow.web.SiteBuilder.build.PageLayout.INCLUDE_PREFIX;
import static com.shanebow.web.SiteBuilder.build.PageLayout.SKIP_INCLUDE;
import com.shanebow.web.SiteBuilder.pages.*;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBMisc;
import com.shanebow.util.SBProperties;
import java.awt.Color;
import java.util.List;
import java.util.Vector;
import java.io.*;

public class HTMLWriter
	extends BufferedWriter
	{
	private static final String HEADER1 = "<!DOCTYPE html>";

	public static final String STYLESHEET = "stylesheet";

	// Are we within a <style> tag?
	protected boolean inStyleTag = false;

	// Style substitiutions used when inStyleTag is true
	private final String fP1;
	private final String fP2;
	private final String fP3;
	private final String fS1;
	private final String fS2;
	private final String fS3;

	/**
	* fOut is where writeln call goes. Starts off pointing to a StringBuilder
	* and once the head section is written, it points to "this" (BufferedWriter)
	*/
	private Appendable fOut;
	private final File fFile;
	protected final File fSourceRoot;
	protected String fBodyArgs = "";
	protected String fLead = "";
	protected String fLang = "en";
	protected String fCharset = "UTF-8";

	protected long flmod = 0; // last modified written to footer
	protected List<String> fScripts;

	protected String fNext; // href for "next" button, follows '>'
	protected String fPrev; // href for "prev" button, follows '<'
	protected String fAdvert; // href for advertisement, follows '$'
	protected boolean fNaked;
	protected String fMenu; // name of chrome dropdown to show in right pane
	protected String fBanner; // image to show in content pane
	protected Page fPage;

	public HTMLWriter(File aTargetRoot, Page aPage)
		throws IOException
		{
		this(new File(aTargetRoot, aPage.path()));
		fPage = aPage;
if (aPage ==null) System.out.println("File: '" + fFile + " page: " + fPage);
		writeHead(fPage.sourceFile(".seo"));
		}

	private HTMLWriter(File aFile)
		throws IOException
		{
		super(new OutputStreamWriter(new FileOutputStream(aFile),"UTF8"));
		fFile = aFile;
		fOut = new StringBuilder(); // buffer the header
		SBProperties props = SBProperties.getInstance();

		fP1= hex( props.getColor("cfg.style.p1", Color.BLUE));
		fP2= hex( props.getColor("cfg.style.p2", Color.RED));
		fP3= hex( props.getColor("cfg.style.p3", Color.GREEN));
		fS1= hex( props.getColor("cfg.style.s1", Color.DARK_GRAY));
		fS2= hex( props.getColor("cfg.style.s2", Color.GRAY));
		fS3= hex( props.getColor("cfg.style.s3", Color.LIGHT_GRAY));


		fSourceRoot = new File(props.getProperty("cfg.dir.source", "."));
		String iScale = props.getProperty("cfg.page.initial.scale", "1.0");
		if (!iScale.equals("0"))
			writeln(" <meta name=\"viewport\" content=\"width=device-width, initial-scale=" + iScale + "\" />");
		else SBLog.write("viewport initail scale not written");

		meta("author", "Rick Salamone");
		meta("GENERATOR", props.getProperty("app.name"));
		link("SHORTCUT ICON", "/favicon.ico?v="+SBDate.yyyymmdd());

//		writeHeadAssets();
		}

	private String hex(Color c)
		{
		return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
		}

	protected void writeHeadAssets()
		throws IOException
		{
		SBProperties props = SBProperties.getInstance();
		// Links to yui css and/or js
		String yui = YUIPreferences.headLinks();
		if (yui != null) writeln(yui);

		// Local css files
		String cssDir = props.getProperty(BuildCriteria.PKEY_DIR_CSS, "/css/");
		String jsDir = "/js/";
		if (cssDir.equals("/assets/")) {
			cssDir = "/assets/css/";
			jsDir = "/assets/js/";
			}
		String cssFiles = props.getProperty(BuildCriteria.PKEY_CSS_FILES, "");
		if ( !cssFiles.isEmpty())
			for ( String cssEntry : cssFiles.split(","))
				{
				String filespec = ((cssEntry.charAt(0) == '/')? "" : cssDir)
				                + cssEntry;
				if (!cssEntry.endsWith(".css")) filespec += ".css";
				css(filespec);
				}

		String jsFiles = props.getProperty(BuildCriteria.PKEY_JS_FILES, "");
		if ( !jsFiles.isEmpty())
			for ( String jsFile : jsFiles.split(","))
				script(jsDir + jsFile + ".js");
		}

	private void commitHead()
		throws IOException
		{
		SBProperties props = SBProperties.getInstance();
		// Write these lines straight to the top of the file
		this.append(HEADER1).append('\n');
		this.append(" <html lang=\"").append(fLang).append("\"><head>\n");
		this.append(" <meta charset=\"").append(fCharset).append("\" />\n");

		// now write the lines we have been buffering
		this.append((CharSequence)fOut);

		// clear the buffer, and begin writing straight to this(file) from now on
		((StringBuilder)fOut).setLength(0);
		fOut = this;
		}

	public void startBody(String aDocTitle)
		throws IOException
		{
		if ( aDocTitle != null )
			{
			String domain = SBProperties.get(BuildCriteria.PKEY_DOMAIN);
			writeln("  <title>" + aDocTitle + " &rsaquo; " + domain + "</title>");
			}
		writeln(" </head>");
		writeln(" <!--<body>Spoof body for bad isp's</body>-->");
		writeln(" <body" + fBodyArgs + ">");
		commitHead();
		}

	/**
	* Writes the tail then actually closes the file
	* @TODO BUG - Problems writing the tail are logged but NOT SHOWN BY BUILDER!
	* need to fix this!!!
	*/
	@Override public void close()
		throws IOException
		{
		try {
			writeTail();
			if (fScripts != null)
				for (String line : fScripts)
					writeln(line);
			writeln("</body></html>");
			}
		catch (IOException e) { throw e; }
		finally {
			try { super.close(); } catch (Exception ignore) {}
			}
		}

	protected String parseTitle(String trimmed)
		{
		String tag = trimmed.substring(0,6).toLowerCase();
		return (tag.startsWith("<h1") || tag.startsWith("<title"))?
			trimmed.substring(trimmed.indexOf(">")+1,trimmed.lastIndexOf("</"))
			: "?";
		}

	public void css(String aHref) throws IOException
		{
		link(STYLESHEET, aHref);
		}

	public void link(String aRel, String aHref) throws IOException
		{
		writeln("  <link rel=\"" + aRel + "\" href=\"" + aHref + "\" />" );
		}

	public void meta(String aName, String aContent) throws IOException
		{
		writeln("  <meta name=\"" + aName + "\" content=\"" + aContent + "\" >");
		}

	public void script(String aSrc) throws IOException
		{
		writeln("  <script src=\"" + aSrc + "\"></script>" );
		}

	public void writeln(String aLine)
		throws IOException
		{
		fOut.append(aLine).append('\n');
		}

	/**
	* recursively reads content files - if a line begins with INCLUDE_PREFIX, then
	* the remainder of the line is assumed to be an include file, and this method
	* calls itself to process the file.
	*
	* @param aFile - needed to determine directory of current file when including another
	* @param stream - the input we are reading from aFile
	*
	* caveat - cannot include files inside of <script> tag
	* @TODO - would be nice to support the standard and maybe even custom variables
	*/
	protected void processContent(File aFile, BufferedReader stream)
		throws IOException
		{
		String line;
		try
			{
			for (int lineNo = 1; (line = stream.readLine()) != null; lineNo++ )
				{
				if (line.endsWith("style>")) {
					inStyleTag = line.trim().equals("<style>");
					System.out.println("inStyleTag " + aFile.getName() + "  " + inStyleTag);
					writeln(line);
					continue;
					}

				int ampAt = line.indexOf(INCLUDE_PREFIX);
				if (line.startsWith("<script"))
					{
					if (fScripts == null)
						fScripts = new Vector<String>(25);
					while (line != null)
						{
						fScripts.add(line.replace('\t',' '));
						if (line.endsWith("</script>")) break; // done = true;
						line = stream.readLine();
						}
					}
				else if (ampAt >= 0) // have an include file marker, or variable
					{
					File includeFile = PageLayout.getIncludeFile(aFile, lineNo, line);
					if (includeFile == null) {
						line = SiteVars.process(line);
						writeln(line); // not an include line
						}
					else if (includeFile != SKIP_INCLUDE) process(includeFile);
					// else -- skip include file; eat the line
					}
				else if ( inStyleTag && line.indexOf('<') >= 0 )
					{
					line = line.replace("<P1>",fP1).replace("<P2>",fP2).replace("<P3>",fP3)
					           .replace("<S1>",fS1).replace("<S2>",fS2).replace("<S3>",fS3);
					writeln(line);
					}
				else writeln(line);
				}
			}
		finally
			{
			stream.close();
/*
			try { if (stream != null) stream.close(); }
			catch (Exception e) {}
*/
			}
		}

	protected void process(File aFile)
		throws IOException
		{
System.out.println("process: " + aFile);
		BufferedReader stream = null;
		try {
			stream = SBMisc.utfReader(aFile);
			processContent(aFile, stream);
			}
		catch (Exception e) {
			SBLog.write("HTMLWriter process: '" + aFile + "' Exception: " + e);
			throw new IOException(e);
			}
		}

	private void writeHead(File aFile)
		throws IOException
		{
		if (!aFile.exists())
			return;
		process(aFile);
		}

	protected void writePrevNext()
		throws IOException
		{
		if ( fPrev == null && fNext == null )
			return;
		write( "<div style=\"margin: 10px 5px 10px 5px;\"><hr>" );
		if (fPrev != null)
			write( "<div style=\"float: left\"><a href=\"" + fPrev + ".html\">"
			     + "<img src=\"/pix/prev.gif\" alt=\"Previous\"></a></div>" );

		if (fNext != null)
			write( "<div style=\"float: right\"><a href=\"" + fNext + ".html\">"
			     + "<img src=\"/pix/next.gif\" alt=\"Next\"></a></div>" );
		writeln( "</div>" );
		}

	protected void writeModified()
		throws IOException
		{
		write(SBDate.yyyy_mm_dd(flmod/1000));
		}

boolean fThrowMissingMenu = false;

	protected void writeMenu(PageMenu aMenu)
		throws IOException
		{
		if (!aMenu.write(this, fMenu)) // fMenu is actually the item in aMenu!
			{
			String msg = (fMenu != null)? fMenu + " not found in menu x"
			           : "Unspecified 'menu' parameter";
			if (fThrowMissingMenu)
/**
			if (fMenu != null // if menu specified, but not found always throw
			|| fThrowMissingMenu)
**/
				throw new IOException(msg);
			else
				SBLog.write(fFile.toString() + " " + msg);
			}
		}

	final void writeChromeSide(ChromeMenu aChromeMenu)
		throws IOException
		{
		if (fMenu != null)
			writeln(aChromeMenu.getDropDownAsUL(fMenu));
else System.out.println("null fMenu: " + fPage);
		}

	protected void writeBreadCrumbs()
		throws IOException
		{
		if (fPage == null) 
			throw new IOException("Missing Page Record");

		String path = fPage.path();
		String crumb = "";
		int i = 0;
		while (true) {
			i = path.indexOf('/', i) + 1;
			if (i <= 0) break;
			Page link = SitePages.getPage(path.substring(0,i) + "index.php");
			if (link == null )
				link = SitePages.getPage(path.substring(0,i) + "index.html");
			if (link == null ) crumb += "XXX" + " &rsaquo; ";
			else	if (link == fPage ) break;
			else crumb += link.anchor() + " &rsaquo; ";
			}
		crumb += fPage.title();
		writeln(crumb);
		}

	public final void writeMastHead()
		throws IOException
		{
		write(fPage.title());
		}

	public final void writeLead()
		throws IOException
		{
		if (fLead != null && !fLead.isEmpty())
			write(fLead);
		}

	public final void writeBanner()
		throws IOException
		{
		if ( fBanner != null )
			{
			writeln("<div class=\"banner\">");
			writeln("<img src=\"/pix/banner/" + fBanner + "\"");
			writeln("alt=\"TapeWorm automated trading\" width=\"470\" height=\"114\" />");
			writeln("</div>");
			}
		}

	public final void writeTOC()
		throws IOException
		{
		File tocFile = fPage.sourceFile(".toc");
		process(tocFile);
//		writeln("<p>TOC File: '" + tocFile + "'</p>");
		}

	public final void writeSideBar()
		throws IOException
		{
		File sideFile = fPage.sourceFile(".side");
		if (!sideFile.exists())
			sideFile = fPage.sourceFile(".toc");
		if (!sideFile.exists()) {
			File dir = new File(fSourceRoot+fPage.dir());
			do {
				sideFile = new File(dir, "sidebar.div");
				dir = dir.getParentFile();
				}
			while (!sideFile.exists() && (dir.compareTo(fSourceRoot) >= 0));
			}

		process(sideFile);
		}

	/**
	* Tries to find and write a tail file, searching in this order:
	* 1) sourceName + ".tail"
	* 2) sourceDir + "tail.div"
	* 3) sourceRoot + "tail.div"
	*/
	public final void writeHeadOrTail(String hot)
		throws IOException
		{
		File hotFile = fPage.sourceFile("." + hot);
		if (!hotFile.exists())
			hotFile = new File(fSourceRoot+fPage.dir(), hot + ".div");
		if (!hotFile.exists())
			hotFile = new File(fSourceRoot, hot + ".div");
		if (hotFile.exists())
			process(hotFile);
		}

	public final void writeTail()
		throws IOException
		{
		File tailFile = fPage.sourceFile(".tail");
		if (!tailFile.exists())
			tailFile = new File(fSourceRoot+fPage.dir(), "tail.div");
		if (!tailFile.exists())
			tailFile = new File(fSourceRoot, "tail.div");
		if (tailFile.exists())
			process(tailFile);
		}

	protected void writeSideMenu()
		throws IOException
		{
		if (fPage == null) 
			throw new IOException("Missing Page Record");

		SideMenu sideMenu = new SideMenu(new File(fSourceRoot+fPage.dir(), "sidebar.div"));
		sideMenu.write(this, fPage);
		}

	public final void writeTron()
		throws IOException
		{
		File incFile = fPage.sourceFile(".tron");
		if (!incFile.exists())
			incFile = new File(fSourceRoot+fPage.dir(), "tron.div");
		if (!incFile.exists())
			incFile = new File(fSourceRoot, "tron.div");
		if (incFile.exists())
			process(incFile);
		}
	}

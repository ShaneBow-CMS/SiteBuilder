package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)IncludeReader.java 1.00 20160904
* Copyright © 2011-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* IncludeReader: Extends BufferedWriter specifically for creating HTML files.
*
* @author Rick Salamone
* @version 1.00
* 20160904 rts created from pieces of HTMLWriter
*******************************************************/
import static com.shanebow.web.SiteBuilder.build.PageLayout.INCLUDE_PREFIX;
import static com.shanebow.web.SiteBuilder.build.PageLayout.SKIP_INCLUDE;
import com.shanebow.web.SiteBuilder.pages.*;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBMisc;
import com.shanebow.util.SBProperties;
import java.util.List;
import java.util.Vector;
import java.io.*;

public class IncludeReader
//	extends BufferedReader
	{
	/**
	* fOut is where writeln call go. Starts off pointing to a StringBuilder
	* and once the head section is written, it points to "this" (BufferedWriter)
	*/
	private Appendable fOut;
	protected List<String> fScripts;

	public IncludeReader()
		{
		fOut = new StringBuilder(); // buffer the header
//		SBProperties props = SBProperties.getInstance();
		SBLog.write("IncludeReader ctor");
		}

	public void writeln(String aLine)
		throws IOException
		{
		fOut.append(aLine).append('\n');
		}

	/**
	* recursively reads content files - if a line begins with INCLUDE_PREFIX, then the
	* remainder of the line is assumed to be an include file, and this
	* method calls itself to process the file.
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
				else if (ampAt >= 0) // have an include file marker
					{
					File includeFile = PageLayout.getIncludeFile(aFile, lineNo, line);
					if (includeFile == null) writeln(line); // not an include line
					else if (includeFile != SKIP_INCLUDE) process(includeFile);
					// else -- skip include file; eat the line
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

	public void process(File aFile)
		throws IOException
		{
System.out.println("process: " + aFile);
		BufferedReader stream = null;
		try {
			stream = SBMisc.utfReader(aFile);
			processContent(aFile, stream);
			}
		catch (Exception e) {
			SBLog.write("IncludeReader process: '" + aFile + "' Exception: " + e);
			throw new IOException(e);
			}
		}
	}

package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)CSSProcessor.java 1.00 20110928
* Copyright © 2011-2017 by Richard T. Salamone, Jr. All rights reserved.
*
* CSSProcessor: Copies a style sheet template to a true css, replacing
* color variables with valid css color entries - and allows importing
* other files (local and shared).
*
* @author Rick Salamone
* @version 1.00
* 20110928 rts created
* 20130827 rts added support to recursively import files
* 20160111 rts reads various char encodings/writes UTF8
* 20160113 rts rewrote include processing to simplify and bug fix
* 20160119 rts move getIncludeFile() to PageLayout
* 20170906 rts added variable substitutions
*******************************************************/
import static com.shanebow.web.SiteBuilder.build.PageLayout.INCLUDE_PREFIX;
import static com.shanebow.web.SiteBuilder.build.PageLayout.SKIP_INCLUDE;
import com.shanebow.util.SBMisc;
import com.shanebow.util.SBProperties;
import java.awt.Color;
import java.io.*;

public final class CSSProcessor
	{
	String propertyPrefix = "cfg.style.";

	private final String fP1;
	private final String fP2;
	private final String fP3;
	private final String fS1;
	private final String fS2;
	private final String fS3;

	public CSSProcessor()
		{
		super();
		SBProperties props = SBProperties.getInstance();
		fP1= hex( props.getColor(propKey("p1"), Color.BLUE));
		fP2= hex( props.getColor(propKey("p2"), Color.RED));
		fP3= hex( props.getColor(propKey("p3"), Color.GREEN));
		fS1= hex( props.getColor(propKey("s1"), Color.DARK_GRAY));
		fS2= hex( props.getColor(propKey("s2"), Color.GRAY));
		fS3= hex( props.getColor(propKey("s3"), Color.LIGHT_GRAY));
		}

	private String propKey(String suffix) { return propertyPrefix + suffix; }

	private String hex(Color c)
		{
		return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
		}

	public BuildEvent ifNewer(File f1, File f2)
		{
		BuildEvent event = new BuildEvent(f2);
		if ( f2.exists()
		&&   f2.lastModified() >= f1.lastModified())
			return event.skipped();

		try
			{
			process(f1, f2);
			return event.built();
			}
		catch (Exception e) {f2.delete(); return event.failed("", e); }
		}

	public void process(File f1, File f2)
		throws FileNotFoundException, IOException
		{
		BufferedWriter out= null;
		try
			{
			out = SBMisc.utfWriter(f2);
			write(out, f1);
			}
		finally { try { out.close(); } catch (Exception e) {} }
		}

	private void write(BufferedWriter out, File inFile)
		throws FileNotFoundException, IOException
		{
		int lineNo = 0;
		BufferedReader in = null;
		try
			{
			in = SBMisc.utfReader(inFile);
			if (in == null )
				throw new FileNotFoundException(inFile.toString() + " not found");

			String line;
			while ((line = in.readLine()) != null )
				{
				++lineNo;
				if ( line.indexOf('<') >= 0 )
					{
					line = line.replace("<P1>",fP1).replace("<S1>",fS1)
					           .replace("<P2>",fP2).replace("<S2>",fS2)
					           .replace("<P3>",fP3).replace("<S3>",fS3);
					}
				if ( line.indexOf(INCLUDE_PREFIX) >= 0 )
					{
					File includeFile = PageLayout.getIncludeFile(inFile, lineNo, line);
					if (includeFile == null)
						line = SiteVars.process(line);
					else {
						if (includeFile != SKIP_INCLUDE) write(out, includeFile);
						continue;
						} 
					}
				out.write(line);
				out.newLine();
				}
			}
		finally { try { in.close(); } catch (Exception e) {} }
		}
	}

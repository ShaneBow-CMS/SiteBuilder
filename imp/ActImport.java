package com.shanebow.web.SiteBuilder.imp;
/********************************************************************
* @(#)ActImport.java 1.00 20111214
* Copyright 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* ActImport: Action class to display the system properties
* in a dialog.
*
* @author Rick Salamone
* @version 1.00
* 20111214 rts created
*******************************************************/
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBMisc;
import java.awt.event.ActionEvent;
import java.io.File;

public final class ActImport
	extends SBAction
	{
	public static final String CMD_NAME="Import";

	public ActImport()
		{
		super( CMD_NAME, 'I', "Convert a web site to SiteBuilder content", null );
		}

	@Override public void actionPerformed(ActionEvent e)
		{
//		new DlgBuild(CMD_NAME).doStart();
		File dir = new File("c:/apps/src/com/TapeWorm/_content/pub/newsletter");
		String ext = ".html";
		ImportHtml converter = new ImportHtml();
		String[] files = SBMisc.fileList(dir, ext);
		int count = 0;
		int errors = 0;
		for ( String name : files )
			{
			File inFile = new File(dir, name + ext);
			try
				{
				converter.process(inFile);
				++count;
				log("Imported '" + inFile.toString() + "'");
				}
			catch (Exception ex)
				{
				++errors;
				log( "error importing " + inFile.getName() + ":\n " + ex.getMessage());
				}
			}
		String msg = "<html>Imported <b>" + count + "</b> html files";
		if ( errors > 0 )
			{
			msg += "<br><font color=RED> and enountered " + errors + " errors";
			SBDialog.error(LAF.getDialogTitle(CMD_NAME), msg);
			}
		else SBDialog.inform(LAF.getDialogTitle(CMD_NAME), msg);
		}

	private void log(String msg)
		{
		SBLog.write(msg);
		}
	}

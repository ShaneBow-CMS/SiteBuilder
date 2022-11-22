package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)ActBuild.java 1.00 20110103
* Copyright 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* ActBuild: Action class to display the system properties
* in a dialog.
*
* @author Rick Salamone
* @version 1.00, 20110103 rts created
*******************************************************/
import com.shanebow.ui.SBAction;
import java.awt.event.ActionEvent;

public final class ActBuild
	extends SBAction
	{
	public static final String CMD_NAME="Build Site";

	public ActBuild()
		{
		super( CMD_NAME, 'B', "Generate the html files for the site", null );
		}

	@Override public void actionPerformed(ActionEvent e)
		{
		new DlgBuild(CMD_NAME).doStart();
		}
	}

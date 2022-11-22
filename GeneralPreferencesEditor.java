package com.shanebow.web.SiteBuilder;
/********************************************************************
* @(#)GeneralPreferencesEditor.java 1.00 20110809
* Copyright © 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* GeneralPreferencesEditor: Allows user to configure the general
* settings for building a site.
*
* @author Rick Salamone
* @version 1.00
* 20110809 rts created
* 20111206 rts call populate() to reload props on display
*******************************************************/
import com.shanebow.web.SiteBuilder.build.BuildCriteria;
import com.shanebow.ui.PreferencesEditor;

public final class GeneralPreferencesEditor
	implements PreferencesEditor
	{
	private BuildCriteria fBuildCriteriaPanel;

	/**
	* Return a GUI component which allows the user to edit
	* this set of related preferences.
	*/  
	@Override public javax.swing.JComponent getComponent()
		{
		if (fBuildCriteriaPanel == null)
			fBuildCriteriaPanel = new BuildCriteria();
		fBuildCriteriaPanel.populate();
		return fBuildCriteriaPanel;
		}

	/**
	* The name of the tab in which this PreferencesEditor will be placed.
	*/
	@Override public String getTitle() { return "General"; }

	/**
	* The mnemonic to appear in the tab name.
	*/
	@Override public int getMnemonic() { return 'G'; }
  
	/**
	* Store the related preferences as they are currently displayed,
	* overwriting all corresponding settings.
	*/
	@Override public void savePreferences()
		{
		fBuildCriteriaPanel.getCriteria();
		}

	/**
	* Reset the related preferences to their default values, but only as 
	* presented in the GUI, without affecting stored preference values.
	*/
	@Override public void matchGuiToDefaultPreferences() {}
	}

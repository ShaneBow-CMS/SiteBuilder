package com.shanebow.web.SiteBuilder;
/********************************************************************
* @(#)SiteVarsPreferencesEditor.java 1.00 20170904
* Copyright © 2017 by Richard T. Salamone, Jr. All rights reserved.
*
* SiteVarsPreferencesEditor: Allows user to configure the general
* settings for building a site.
*
* @author Rick Salamone
* @version 1.00
* 20170904 rts created
*******************************************************/
import com.shanebow.web.SiteBuilder.build.SiteVars;
import com.shanebow.ui.PreferencesEditor;

public final class SiteVarsPreferencesEditor
	implements PreferencesEditor
	{
	private SiteVars fSiteVarsPanel;

	/**
	* Return a GUI component which allows the user to edit
	* this set of related preferences.
	*/  
	@Override public javax.swing.JComponent getComponent()
		{
		if (fSiteVarsPanel == null)
			fSiteVarsPanel = new SiteVars();
		fSiteVarsPanel.populate();
		return fSiteVarsPanel;
		}

	/**
	* The name of the tab in which this PreferencesEditor will be placed.
	*/
	@Override public String getTitle() { return "Vars"; }

	/**
	* The mnemonic to appear in the tab name.
	*/
	@Override public int getMnemonic() { return 'V'; }
  
	/**
	* Store the related preferences as they are currently displayed,
	* overwriting all corresponding settings.
	*/
	@Override public void savePreferences()
		{
		fSiteVarsPanel.save();
		}

	/**
	* Reset the related preferences to their default values, but only as 
	* presented in the GUI, without affecting stored preference values.
	*/
	@Override public void matchGuiToDefaultPreferences() {
		System.out.println("matchGuiToDefaultPreferences");
		}
	}

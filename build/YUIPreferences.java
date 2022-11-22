package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)YUIPreferences.java 1.00 20110928
* Copyright © 2013-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* YUIPreferences: Allows user to configure settings
* for using the YUI libraries in a site.
*
* @author Rick Salamone
* @version 1.00
* 20130124 rts created (as part of general preferences)
* 20130130 rts support for yui-min.js and yui version
* 20160124 rts put on own tab (from general prefs)
*******************************************************/
import com.shanebow.ui.PreferencesEditor;
import com.shanebow.ui.layout.LabeledPairPanel;
import com.shanebow.util.SBProperties;
import javax.swing.*;

public final class YUIPreferences
	extends JPanel
	implements PreferencesEditor
	{
	private static final String KEY_PREFIX = "cfg.css.yui.";

	public static final String PKEY_YUI_VERSION  = KEY_PREFIX+"version";
	public static final String PKEY_USE_YUI_MINJS= KEY_PREFIX+"minjs";
	public static final String PKEY_USE_YUI_RESET= KEY_PREFIX+"reset";
	public static final String PKEY_USE_YUI_BASE = KEY_PREFIX+"base";
	public static final String PKEY_USE_YUI_FONTS= KEY_PREFIX+"fonts";
	public static final String PKEY_USE_YUI_GRIDS= KEY_PREFIX+"grids";

	public static String headLinks() {
		SBProperties props = SBProperties.getInstance();
		StringBuilder it = new StringBuilder();
		String yuiVer = props.getProperty(PKEY_YUI_VERSION, "3.8.0");

		if (props.getBoolean(PKEY_USE_YUI_RESET, false))
			it.append(yuiVer).append("/build/cssreset/cssreset-min.css&");

		if (props.getBoolean(PKEY_USE_YUI_BASE, false))
			it.append(yuiVer).append("/build/cssbase/cssbase-min.css&");

		if (props.getBoolean(PKEY_USE_YUI_FONTS, false))
			it.append(yuiVer).append("/build/cssfonts/cssfonts-min.css&");

		if (props.getBoolean(PKEY_USE_YUI_GRIDS, false))
			it.append(yuiVer).append("/build/cssgrids/cssgrids-min.css&");

		int yuiLength = it.length();
		if (yuiLength > 0)
			{
			it.setLength(--yuiLength);
			it.insert(0, "<link rel=\"stylesheet\" href=\"http://yui.yahooapis.com/combo?")
			              .append("\" />");
			}
		if (props.getBoolean(PKEY_USE_YUI_MINJS, false))
			it.append("\n  <script src=\"http://yui.yahooapis.com/")
			              .append(yuiVer).append("/build/yui/yui-min.js\"></script>");

		return (0==it.length())? it.toString() : null;
		}

	private YUICriteria fYUICriteriaPanel;

	/**
	* Return a GUI component which allows the user to edit these preferences.
	*/  
	@Override public JComponent getComponent()
		{
		if (fYUICriteriaPanel == null)
			fYUICriteriaPanel = new YUICriteria();
		fYUICriteriaPanel.populate();
		return fYUICriteriaPanel;
		}

	/**
	* The name of the tab in which this editor will be placed.
	*/
	@Override public String getTitle() { return "YUI"; }

	/**
	* The mnemonic to appear in the tab name.
	*/
	@Override public int getMnemonic() { return 'Y'; }
  
	/**
	* Store the related preferences as they are currently displayed,
	* overwriting all corresponding settings.
	*/
	@Override public void savePreferences()
		{
		fYUICriteriaPanel.getCriteria();
		}

	/**
	* Reset the related preferences to their default values, but only as 
	* presented in the GUI, without affecting stored preference values.
	*/
	@Override public void matchGuiToDefaultPreferences() {}

	final class YUICriteria
		extends LabeledPairPanel
		{
		private final JTextField tfYuiVersion = new JTextField();
		private final JCheckBox  chkUseYuiMinJs = new JCheckBox();
		private final JCheckBox  chkUseYuiReset = new JCheckBox("Reset");
		private final JCheckBox  chkUseYuiBase = new JCheckBox("Base");
		private final JCheckBox  chkUseYuiFonts = new JCheckBox("Fonts");
		private final JCheckBox  chkUseYuiGrids = new JCheckBox("Grids");

		YUICriteria()
			{
			super();
			addRow( "version",       tfYuiVersion );
			addRow( "yui-min.js",    chkUseYuiMinJs );
			addRow( "css",           chkUseYuiReset, chkUseYuiBase );
			addRow( "",              chkUseYuiFonts, chkUseYuiGrids );
			}

		void populate()
			{
			SBProperties props = SBProperties.getInstance();
			tfYuiVersion.setText(props.getProperty(PKEY_YUI_VERSION, "3.8.0"));
			chkUseYuiMinJs.setSelected(props.getBoolean(PKEY_USE_YUI_MINJS, false));
			chkUseYuiReset.setSelected(props.getBoolean(PKEY_USE_YUI_RESET, false));
			chkUseYuiBase.setSelected(props.getBoolean(PKEY_USE_YUI_BASE, false));
			chkUseYuiFonts.setSelected(props.getBoolean(PKEY_USE_YUI_FONTS, false));
			chkUseYuiGrids.setSelected(props.getBoolean(PKEY_USE_YUI_GRIDS, false));
			}

		boolean getCriteria()
			{
			SBProperties props = SBProperties.getInstance();
			props.setProperty(PKEY_YUI_VERSION,  tfYuiVersion.getText());
			props.setProperty(PKEY_USE_YUI_MINJS,chkUseYuiMinJs.isSelected());
			props.setProperty(PKEY_USE_YUI_RESET,chkUseYuiReset.isSelected());
			props.setProperty(PKEY_USE_YUI_BASE,chkUseYuiBase.isSelected());
			props.setProperty(PKEY_USE_YUI_FONTS,chkUseYuiFonts.isSelected());
			props.setProperty(PKEY_USE_YUI_GRIDS,chkUseYuiGrids.isSelected());
			return true;
			}
		}
	}

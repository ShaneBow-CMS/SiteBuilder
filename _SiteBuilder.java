package com.shanebow.web.SiteBuilder;
/********************************************************************
* @(#)_SiteBuilder.java	1.00 20110707
* Copyright © 2011-2018 by Richard T. Salamone, Jr. All rights reserved.
*
* _SiteBuilder: The entry point and main container frame for the
* Web Site Builder application.
*
* @author Rick Salamone
* @version 1.00
* 20110707 rts created
* 20130422 rts moved build button to the left side
* 20130826 rts added apps tab
* 20140420 rts decoupled products/cats to wares tab
* 20150211 rts links check got ui and runs in background
* 20150307 rts support for configurable css dir
* 20150831 rts using a copy of SiteManager's improved site picker
* 20160126 rts changed templates view to source view
* 20160327 rts site picker ,odofoed to app;y public_html dir if used
* 20170903 rts no longer reload libs tab on site change - added theme dir
* 20180610 rts put site in title, and app dir is fixed
*******************************************************/
import com.shanebow.tools.Expose.ActProperties;
import com.shanebow.web.ftp.*;
import com.shanebow.web.SiteBuilder.build.ActBuild;
import com.shanebow.web.SiteBuilder.build.BuildCriteria;
import com.shanebow.web.SiteBuilder.build.YUIPreferences;
import com.shanebow.web.SiteBuilder.dbm.DBMTab;
import com.shanebow.web.SiteBuilder.editor.*;
import com.shanebow.web.SiteBuilder.links.*;
import com.shanebow.web.SiteBuilder.sitemap.*;
import com.shanebow.web.SiteBuilder.style.StylePreferencesEditor;
import com.shanebow.tools.uniedit.UniEditor;
import com.shanebow.ui.menu.*;
import com.shanebow.ui.LAF;
import com.shanebow.ui.Reloadable;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBFilePicker;
import com.shanebow.ui.SBTabbedPane;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public final class _SiteBuilder
	extends JFrame
	{
	private static long blowUp = 0; // SBDate.toTime("20101231  23:59");
	private static final String TAB_TARGET = "Target";
	private static final String TAB_ASSETS = "Assets";
	private static final String TAB_THEMES = "Themes";
	private static final String TAB_LIBS = "Libs";
	private static final String TAB_APPS = "Apps";
	private static final String TAB_DBM = "DBM";

	public static void main( String[] args )
		{
		SBProperties.load(_SiteBuilder.class);
		LAF.initLAF(blowUp, true);
		JFrame frame = new _SiteBuilder();
		}

	private final SBTabbedPane fTabs = new SBTabbedPane();
	private final ActBuild     fActBuild = new ActBuild();
	private final ActUpload    fActUpload = new ActUpload("cfg.dir.target");

	private final SitePicker fSitePicker = new SitePicker() {
		@Override protected boolean loadSite(String aCfgFile)
			{
			SBProperties props = SBProperties.getInstance();
			if ( !_exitOK()) return false;
			log("loading site: " + aCfgFile);
			props.changeCfg(aCfgFile);
			BuildCriteria.load();
			int numTabs = fTabs.getTabCount();
			for ( int i = 0; i < numTabs; i++ )
				{
				Component tab = fTabs.componentAt(i);
				String tabTitle = fTabs.getTitleAt(i);
				// don't reload these
				if (tabTitle.equals(TAB_LIBS)
				||  tabTitle.equals(TAB_THEMES))
					;

				// reload any UniEditor Tabs
				else if (tab instanceof UniEditor)
					((UniEditor)tab).setRoot(
					 	fTabs.getTitleAt(i).equals(TAB_ASSETS)? BuildCriteria.cssDir()
					 :	fTabs.getTitleAt(i).equals(TAB_TARGET)? BuildCriteria.dstDir()
					 :	fTabs.getTitleAt(i).equals(TAB_APPS)? BuildCriteria.appDir()
					 :	BuildCriteria.srcDir());

				// and reload these, e.g. SiteMap, TAB_DBM
				else if ( tab instanceof Reloadable )
					((Reloadable)tab).reload();

				// should have dealt with all tabs above
				else log("tab[%d]: %s IS NOT Reloadable", i, tab.getClass().getSimpleName());
				}
			setTitle(LAF.getDialogTitle(aCfgFile));
			return true;
			}
		};

	public _SiteBuilder()
		{
		super();
		SBProperties props = SBProperties.getInstance();
		setTitle(props.getProperty("app.name")
		        + " " + props.getProperty("app.version"));
		setBounds(props.getRectangle("usr.app.bounds", 25, 25, 850, 400));

		LAF.addPreferencesEditor(new GeneralPreferencesEditor());
		LAF.addPreferencesEditor(new StylePreferencesEditor());
		LAF.addPreferencesEditor(new FTPPreferencesEditor(true));
		LAF.addPreferencesEditor(new YUIPreferences());
		LAF.addPreferencesEditor(new SiteVarsPreferencesEditor());
		buildMenus();
		buildContent();
		setVisible(true);
		}

	private boolean _exitOK()
		{
		int numTabs = fTabs.getTabCount();
		for ( int i = 0; i < numTabs; i++ )
			{
			Component tab = fTabs.componentAt(i);
			if (( tab instanceof Reloadable )
			&&  !((Reloadable)tab).exitOK())
				return false;
			}
		return true;
		}

	private void log ( String fmt, Object... args )
		{
		String msg = String.format(fmt,args);
		SBLog.write( getClass().getSimpleName(), msg );
		System.out.println(msg);
		}

	protected void buildContent()
		{
		JPanel cp = new JPanel(new BorderLayout());
		cp.add( mainPanel(), BorderLayout.CENTER );
		cp.add( btnPanel(), BorderLayout.SOUTH );
		cp.setBorder(LAF.getStandardBorder());
		setContentPane(cp);
		}

	private JComponent mainPanel()
		{
		UniEditor ue;
		fTabs.addTab("Source",    new UniEditor("", null, true), "Site source files" );
//		fTabs.addTab("Site Map",  new SiteMap(), "Organize Site Map" );
		fTabs.addTab(TAB_APPS,    new UniEditor("", null, true), "Web Apps" );
		fTabs.addTab(TAB_ASSETS,  new UniEditor("", null, true), "Site Look & Feel" );

		fTabs.addTab(TAB_THEMES,  ue = new UniEditor(BuildCriteria.themeDir().toString(), null, true), "Site Themes" );
		ue.setForceHtml(false);
		fTabs.addTab(TAB_LIBS,    ue = new UniEditor(BuildCriteria.libDir().toString(), null, true), "Module Libraries" );
		ue.setForceHtml(false);

		fTabs.addTab(TAB_TARGET,  ue = new UniEditor("", null, true), "Local web site" );
		ue.setForceHtml(false);
//		fTabs.addTab("Wares",     new WaresPanel(), "What's sold here" );
//		fTabs.addTab(TAB_DBM,     new DBMTab(), "Database Manager" );
		return fTabs;
		}

	private JPanel btnPanel()
		{
		JPanel p = new JPanel();
		JButton btnBuild = fActBuild.makeButton();
		Dimension edgeSpacer = new Dimension(5, 0);
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		p.add( new JLabel("Site: "));
		p.add( fSitePicker );
		p.add(Box.createRigidArea(edgeSpacer));
		p.add( btnBuild );
		p.add( new JLabel("<html>"));
		p.add(Box.createRigidArea(edgeSpacer));
		p.add(Box.createHorizontalGlue());
		p.add(new ActLinksCheck().makeButton());
		p.add(Box.createRigidArea(edgeSpacer));
		p.add( fActUpload.makeButton());
		getRootPane().setDefaultButton(btnBuild);
		p.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		return p;
		}

	protected void buildMenus()
		{
		SBMenuBar menuBar = new SBMenuBar();

		menuBar.addMenu( "File",
			fActBuild, null,
			fActUpload, null,
new com.shanebow.web.SiteBuilder.imp.ActImport(), null,
			new SBViewLogAction(this), null,
			LAF.setExitAction(new com.shanebow.ui.SBExitAction(this)
				{
				@Override public boolean exitOK() { return _exitOK(); }
				@Override public void doApplicationCleanup()
					{
//					DBStatement.disconnect();
					}
				}));
		menuBar.addEditMenu();
		menuBar.addMenu("Settings",
			LAF.getPreferencesAction(), null,
			new ActProperties(this), null,
			menuBar.getThemeMenu());
		menuBar.addMenu("Thai",
			new com.thaidrills.ref.ActConsonantsCheat("Consonant Table", 'C'),
			new com.thaidrills.ref.ActVowelsCheat("Vowel Table", 'V'),
			new com.thaidrills.ref.ActToneCheat("Tone Rules", 'T')
			);
		menuBar.addMenu("Help",
			new SBActHelp(), null,
			new SBAboutAction(this));
		setJMenuBar(menuBar);
		}
	}

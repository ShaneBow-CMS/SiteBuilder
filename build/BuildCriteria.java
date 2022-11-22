package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)BuildCriteria.java 1.00 20110106
* Copyright © 2011-2017 by Richard T. Salamone, Jr. All rights reserved.
*
* BuildCriteria:
*
* @author Rick Salamone
* @version 1.00
* 20110106 rts created
* 20110108 rts supports file patterns & case insensitive
* 20111206 rts split out populate to handle multiple sites
* 20120711 rts added check box for using chrome (dropdown) menus
* 20130124 rts support for YUI css
* 20130917 rts support simple top menu
* 20140723 rts add lib dir (had been hard coded for a long time)
* 20150307 rts add css dir (had been hard coded for a long time)
* 20160124 rts decoupled YUI settings
* 20160327 rts added key for public_html dir
* 20160401 rts bug fixes public_html dir
* 20170107 rts made _appDir configurable
* 20170903 rts made _libDir a usr rather than cfg property (app wide)
* 20170904 rts added site name and made it & domain accessible as statics
* 20170906 rts moved domain and sitename to SiteVars
*******************************************************/
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.layout.LabeledPairPanel;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import java.io.File;
import javax.swing.*;

public final class BuildCriteria
	extends LabeledPairPanel
	{
	public static final String PKEY_DOMAIN="cfg.DOMAIN";
	public static final String PKEY_DIR_DEST="cfg.dir.target";
	public static final String PKEY_DIR_SRC="cfg.dir.source";
	public static final String PKEY_DIR_APP="cfg.dir.app";
	public static final String PKEY_DIR_CSS="cfg.dir.css";
	public static final String PKEY_DIR_LIB="usr.dir.lib";
	public static final String PKEY_CSS_FILES="cfg.css.files";
	public static final String PKEY_JS_FILES="cfg.js.files";
	public static final String PKEY_DO_CAT="cfg.cat.build";
	public static final String PKEY_USE_CHROME="cfg.menu.chrome";
	public static final String PKEY_USE_TOPMENU="cfg.menu.top";

	private static File _libDir = SBProperties.getInstance().getFile(PKEY_DIR_LIB);
	private static File _srcDir;
	private static File _dstDir;
	private static File _cssDir;
	private static File _appDir;
	private static File _srcPublicDir; // either _srcDir OR _srcDir/public_html
	private static File _dstPublicDir; // either _dstDir OR _dstDir/public_html

	public static final void load() {
		SBProperties props = SBProperties.getInstance();
		_dstDir = props.getFile(PKEY_DIR_DEST);
		_srcDir = props.getFile(PKEY_DIR_SRC);
		_libDir = props.getFile(PKEY_DIR_LIB);
		String cssDir = props.getProperty(PKEY_DIR_CSS, "/css/");
		String appDir = props.getProperty(PKEY_DIR_APP, "/app/");

		// if a public_html subdir exists, then put the assets dir
		// inside of it. Otherwise, put them directly under the _srcDir
		_srcPublicDir = new File(_srcDir, "public_html");
		_dstPublicDir = new File(_dstDir, "public_html");
		if (!_srcPublicDir.exists()) {
			_srcPublicDir = _srcDir;
			_dstPublicDir = _dstDir;
			}
		SBLog.write("set source public dir: " + _srcPublicDir);
		_cssDir = new File(_srcPublicDir, cssDir);

		// puts apps inside the _srcDir
		_appDir = new File(_srcDir, appDir);
		}

	public static final File libDir() { return _libDir; }
	public static final File themeDir() { return new File(_libDir, "_theme"); }
	public static final File appDir() { return _appDir; }
	public static final File cssDir() { return _cssDir; }
	public static final File srcDir() { return _srcDir; }
	public static final File dstDir() { return _dstDir; }
	public static final File srcPublicDir() { return _srcPublicDir; }
	public static final File dstPublicDir() { return _dstPublicDir; }

	private final JTextField tfTarget = new JTextField();
	private final JTextField tfSource = new JTextField();
	private final JTextField tfDirApp = new JTextField();
	private final JTextField tfDirAssets = new JTextField();
	private final JTextField tfLibrary = new JTextField();
	private final JCheckBox  chkDoCatalog = new JCheckBox();
	private final JCheckBox  chkUseChrome = new JCheckBox("Chrome");
	private final JCheckBox  chkUseTopMenu = new JCheckBox("Simple");

	private final JTextField tfCssFiles = new JTextField();
	private final JTextField tfJsFiles = new JTextField();

	public BuildCriteria()
		{
		super();
		addRow( "Target Directory",   tfTarget );
		addRow( "Source Directory",   tfSource );
		addRow( "app Path",           tfDirApp );
		addRow( "Assets Path",        tfDirAssets );
		addRow( "Library Directory",  tfLibrary );
		addRow( "Style Sheets (css)", tfCssFiles );
		addRow( "Java Script (js)",   tfJsFiles );
		addRow( "Build Catalog",      chkDoCatalog );
		addRow( "Menu Type",    chkUseChrome, chkUseTopMenu );
		tfTarget.setToolTipText("Destination directory for the finished site");
		tfSource.setToolTipText("Directory containing site content & layout files");
		tfDirAssets.setToolTipText("relative path to theme files");
		tfLibrary.setToolTipText("Directory containing shared modules");
		}

	public void populate()
		{
		SBProperties props = SBProperties.getInstance();
		tfTarget.setText(props.getProperty(PKEY_DIR_DEST, ""));
		tfSource.setText(props.getProperty(PKEY_DIR_SRC, "templates"));
		tfDirApp.setText(props.getProperty(PKEY_DIR_APP));
		tfDirAssets.setText(props.getProperty(PKEY_DIR_CSS, "/css/"));
		tfLibrary.setText(props.getProperty(PKEY_DIR_LIB, ""));
		tfCssFiles.setText(props.getProperty(PKEY_CSS_FILES, ""));
		tfJsFiles.setText(props.getProperty(PKEY_JS_FILES, ""));
		chkDoCatalog.setSelected(props.getBoolean(PKEY_DO_CAT, false));
		chkUseChrome.setSelected(props.getBoolean(PKEY_USE_CHROME, false));
		chkUseTopMenu.setSelected(props.getBoolean(PKEY_USE_TOPMENU, false));
		}

	/**
	* get a user specifed directory from a text field and verify
	* that it exists and is a valid directory
	*/
	private File getDir(String aLabel, JTextField aTextField)
		throws Exception
		{
		File dir = new File(aTextField.getText());
		if ( dir == null
		||  ( dir.exists() && !dir.isDirectory()))
			throw new Exception(aLabel + " must be a valid directory");
		return dir;
		}

	public boolean getCriteria()
		{
		try
			{
			_dstDir = getDir("Target", tfTarget);
			_srcDir = getDir("Source", tfSource);
			_libDir = getDir("Library", tfLibrary);
String cssSubdir = tfDirAssets.getText().replace(File.separatorChar, '/');
if (!cssSubdir.endsWith("/")) cssSubdir += "/";
_cssDir = new File(_srcDir, cssSubdir);

String appSubdir = tfDirApp.getText().replace(File.separatorChar, '/');
if (!appSubdir.endsWith("/")) appSubdir += "/";
_appDir = new File(_srcDir, appSubdir);
			SBProperties props = SBProperties.getInstance();
			props.setProperty(PKEY_DIR_DEST,  _dstDir);
			props.setProperty(PKEY_DIR_SRC,   _srcDir);
			props.setProperty(PKEY_DIR_LIB,   _libDir);
			props.setProperty(PKEY_DIR_APP,   appSubdir);
			props.setProperty(PKEY_DIR_CSS,   cssSubdir);
			props.setProperty(PKEY_CSS_FILES, tfCssFiles.getText());
			props.setProperty(PKEY_JS_FILES,  tfJsFiles.getText());
			props.setProperty(PKEY_DO_CAT,    chkDoCatalog.isSelected());
			props.setProperty(PKEY_USE_CHROME,chkUseChrome.isSelected());
			props.setProperty(PKEY_USE_TOPMENU,chkUseTopMenu.isSelected());
			return true;
			}
		catch (Exception e) { return falseBecause("Error: " + e.getMessage()); }
		}

	private boolean falseBecause(String why) { return SBDialog.inputError(why); }
	}

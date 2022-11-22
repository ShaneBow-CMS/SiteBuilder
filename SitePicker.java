package com.shanebow.web.SiteBuilder;
/********************************************************************
* @(#)SitePicker.java	1.0 20110707
* Copyright © 2011-2015 Richard T. Salamone, Jr All Rights Reserved.
*
* SitePicker: Extends SBFilePicker to select from a list of
* xxx.cfg files in the program's jar directory.
*
* @author Rick Salamone
* @version 1.0
* 20110707 rts created
* 20150827 rts decoupled from SiteBuilder
* 20150831 rts DUPLICATE OF SiteManager's SitePicker
*********                                         *******
********* HAVEN'T FIGURED OUT WHERE TO PACKAGE IT *******
*********                                         *******
*
*******************************************************/
import com.shanebow.ui.SBFilePicker;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.*;

public abstract class SitePicker
	extends SBFilePicker
	{
	/**
	* These properties are used by fSitePicker - need to have
	* both usr.cfg and usr.cfg.fake cause SBFilePicker updates
	* the value of the mru prop before have a chance to save
	* any dirty files and call SBProperties' changeCfg().
	*/ 
	public static final String DIR_PROPERTY="app.cfg.dir";
	public static final String EXT_PROPERTY="app.cfg.ext";
	public static final String MRU_PROPERTY="usr.cfg.fake";

	public SitePicker() {
		super(false, "Site Name", DIR_PROPERTY, EXT_PROPERTY, MRU_PROPERTY);
		Dimension s = getPreferredSize();
		s.height = 24;
		setPreferredSize(s);
		setMaximumSize(s);
		setOpaque(true);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String aName = (String)getSelectedItem();
				if ( exists(aName) && !loadSite(aName))
					setSelectedItem(SBProperties.getInstance()
						.getProperty("usr.cfg", null));
				}
			});
		}

	@Override public void addNotify() {
		super.addNotify();
		setSelectedItem(SBProperties.getInstance().getProperty(MRU_PROPERTY, null));
		}

	/**
	* @return true new site was loaded
	* return false to restore the previous selection
	*/
	abstract protected boolean loadSite(String aCfgFile);
	}

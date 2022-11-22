package com.shanebow.web.SiteBuilder.style;
/********************************************************************
* @(#)StylePreferencesEditor.java 1.00 20110928
* Copyright © 2011-2015 by Richard T. Salamone, Jr. All rights reserved.
*
* StylePreferencesEditor: Allows user to configure color variables
* for the sites style sheets. Upon applying changes all style sheet
* templates have their last modified date touched to force them to
* be rebuild the next time the site is built.
*
* @author Rick Salamone
* @version 1.00
* 20110928 rts created
* 20130717 rts revised layout
* 20140621 rts handles case where no css files exist
* 20150307 rts bug fix to display rgb by overriding btn's setBackground
*******************************************************/
import com.shanebow.ui.ColorButton;
import com.shanebow.ui.LAF;
import com.shanebow.ui.PreferencesEditor;
import com.shanebow.util.SBMisc;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Date;
import javax.swing.*;

public final class StylePreferencesEditor
	extends JPanel
	implements PreferencesEditor
	{
	String propertyPrefix = "cfg.style.";

	private final ColorSelector fColorP1 = new ColorSelector("<P1>");
	private final ColorSelector fColorP2 = new ColorSelector("<P2>");
	private final ColorSelector fColorP3 = new ColorSelector("<P3>");
	private final ColorSelector fColorS1 = new ColorSelector("<S1>");
	private final ColorSelector fColorS2 = new ColorSelector("<S2>");
	private final ColorSelector fColorS3 = new ColorSelector("<S3>");

	public StylePreferencesEditor()
		{
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		// Set up colors
		JPanel cp = new JPanel(new GridLayout(0,3, 3, 3));
		cp.add(fColorP1);
		cp.add(fColorP2);
		cp.add(fColorP3);
		cp.add(fColorS1);
		cp.add(fColorS2);
		cp.add(fColorS3);

		add( LAF.titled(cp, "Site Color Scheme"));
		add( Box.createVerticalGlue());
//		updateProps();
		}

	private String propKey(String suffix) { return propertyPrefix + suffix; }

	/**
	* Return a GUI component which allows the user to edit these preferences.
	*/  
	@Override public JComponent getComponent()
		{
		SBProperties props = SBProperties.getInstance();

		fColorP1.set( props.getColor(propKey("p1"), Color.BLUE));
		fColorP2.set( props.getColor(propKey("p2"), Color.RED));
		fColorP3.set( props.getColor(propKey("p3"), Color.YELLOW));
		fColorS1.set( props.getColor(propKey("s1"), Color.DARK_GRAY));
		fColorS2.set( props.getColor(propKey("s2"), Color.GRAY));
		fColorS3.set( props.getColor(propKey("s3"), Color.LIGHT_GRAY));
		return this;
		}

	/**
	* The name of the tab in which this editor will be placed.
	*/
	@Override public String getTitle() { return "Style"; }

	/**
	* The mnemonic to appear in the tab name.
	*/
	@Override public int getMnemonic() { return 'S'; }
  
	/**
	* Store the related preferences as they are currently displayed,
	* overwriting all corresponding settings.
	*/
	@Override public void savePreferences()
		{
		updateProps();

		// touch the style sheets to force rebuild next time..
		File sourceRoot = new File(SBProperties.get("cfg.dir.source"));

		String subDir = "css";
		String ext = "." + subDir;
		File fromDir = new File(sourceRoot, subDir);
		String[] names = SBMisc.fileList( fromDir, ext);
		if (names != null) for ( String name : names )
			new File(fromDir, name+ext).setLastModified(new Date().getTime());
		}

	private void updateProps()
		{
		SBProperties props = SBProperties.getInstance();
		props.set(propKey("p1"), fColorP1.get());
		props.set(propKey("p2"), fColorP2.get());
		props.set(propKey("p3"), fColorP3.get());
		props.set(propKey("s1"), fColorS1.get());
		props.set(propKey("s2"), fColorS2.get());
		props.set(propKey("s3"), fColorS3.get());
		}

	/**
	* Reset the related preferences to their default values, but only as 
	* presented in the GUI, without affecting stored preference values.
	*/
	@Override public void matchGuiToDefaultPreferences() {}
	}

class ColorSelector
	extends JPanel
	{
	private final JTextField tfRGB = new JTextField();
	private final ColorButton btn = new ColorButton() {
		@Override public void setBackground(Color c) {
			super.setBackground(c);
			tfRGB.setText(String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
			}
		};

	ColorSelector(String aName) {
		super(new GridLayout(3,0,0,5));
		add(new JLabel(aName));
		add(btn);
		add(tfRGB);
		}

	public void set(Color c) { btn.setBackground(c); }
	public Color get() { return btn.getBackground();}
	}

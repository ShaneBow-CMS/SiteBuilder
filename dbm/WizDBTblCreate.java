package com.shanebow.web.SiteBuilder.dbm;
/********************************************************************
* @(#)WizDBTblCreate.java 1.00 20151102
* Copyright © 2016-2017 by Richard T. Salamone, Jr. All rights reserved.
*
* WizDBTblCreate: Wizard to create a host database table from an sql file
* 1) Select the sql file, and upload on NEXT
* 2) Show results of sending file
*
* @author Rick Salamone
* @version 1.00
* 20160117 rts created
* 20160107 rts handles configurable apps dir
*******************************************************/
import com.shanebow.web.SiteBuilder.build.BuildCriteria;
import com.shanebow.web.host.PostResponse;
import com.shanebow.ui.wizard.*;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import com.shanebow.web.host.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

public class WizDBTblCreate
	extends DlgWizard
	{
	public WizDBTblCreate(Frame frame, Host aHost)
		{
		super(frame, "Create DB Table", true, new DBTblCreateWiz(aHost));
		setMinimumSize( new Dimension(600,400));
		setVisible(true);
		}
	}

final class DBTblCreateWiz
	extends WizPanel
	{
//	private static final String HOST_APP = "apps/nimda/dba.php";

	private WizFileChooser fChooser;
	private PostResponse fResp;

	public DBTblCreateWiz(final Host aHost)
		{
		super();

		// 1) Select the test file, and upload on NEXT
		WizStep chooserStep = new WizStep() {
			@Override public JComponent getComponent() {
				File targetRoot = BuildCriteria.dstPublicDir();
				String appDir = SBProperties.getInstance().getProperty(BuildCriteria.PKEY_DIR_APP);
				fChooser = new WizFileChooser(new File(targetRoot, appDir), "sql") {
					@Override protected void clicked(File f) {
						enableControl(NEXT, f != null);
						}
					};
				return fChooser;
				}
			@Override public void onVisible() {
				enableControl(NEXT, fChooser.getSelectedFile()!= null);
				}
			@Override public boolean onNext() {
				String name = fChooser.getSelectedFile().getName();
				name = name.substring(4, name.length() - 4);
				String app = aHost.appPath("nimda", "dba.php");
				fResp = aHost.post(app, "c=" + name);
				return true;
				}
			};

		// 2) Show results of sending file
		WizStep resultsStep = new WizStep() {
			JLabel lblResult = new JLabel("", JLabel.CENTER);
			@Override public JComponent getComponent() { return new JScrollPane(lblResult); }
			@Override public void onVisible() {
System.out.println("resp dat: " + fResp.dat + "\nmsg: " + fResp.msg);
				// lblResult.setText("<html>" + fResp.dat);
				lblResult.setText("<html>" + fResp.html());
				}
			};

		addStep("Select SQL Definition File", chooserStep, NEXT);
		addStep("Reply", resultsStep, CLOSE);
		}
	}

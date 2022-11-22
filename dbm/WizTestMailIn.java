package com.shanebow.web.SiteBuilder.dbm;
/********************************************************************
* @(#)WizTestMailIn.java 1.00 20151102
* Copyright © 2015-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* WizTestMailIn: Wizard to test the Rox mail receiver code
* 1) Select the test file, and upload on NEXT
* 2) Show results of sending file
*
* @author Rick Salamone
* @version 2.00
* 20151102 rts created
* 20160111 rts reads various char encodings
*******************************************************/
import com.shanebow.web.host.PostResponse;
import com.shanebow.ui.wizard.*;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBMisc;
import com.shanebow.web.host.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

public class WizTestMailIn
	extends DlgWizard
	{
	public WizTestMailIn(Frame frame)
		{
		super(frame, "Test Rox Mail Receiver", true, new TestMailInWiz());
		setMinimumSize( new Dimension(600,400));
		setVisible(true);
		}
	}

final class TestMailInWiz
	extends WizPanel
	{
	private static final String CHOOSER_DIR = "c:/apps/src/com/shanebow/_reference/rox/mail_read/emails/";
	private static final String HOST_APP = "apps/nimda/test.php";

	private WizFileChooser fChooser;
	private PostResponse fResp;

	public TestMailInWiz()
		{
		super();

		// 1) Select the test file, and upload on NEXT
		WizStep chooserStep = new WizStep() {
			@Override public JComponent getComponent() {
				fChooser = new WizFileChooser(new File(CHOOSER_DIR), "txt") {
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
				fResp = new Post().send(HOST_APP,
					FileContents.read(fChooser.getSelectedFile()));
				return true;
				}
			};

		// 2) Show results of sending file
		WizStep resultsStep = new WizStep() {
			JLabel lblResult = new JLabel("", JLabel.CENTER);
			@Override public JComponent getComponent() { return new JScrollPane(lblResult); }
			@Override public void onVisible() {
				// lblResult.setText("<html>" + fResp.dat);
				lblResult.setText("<html>" + fResp.html());
				}
			};

		addStep("Mime email to decode", chooserStep, NEXT);
		addStep("Reply", resultsStep, CLOSE);
		}
	}

class FileContents {
	public static String read(String filespec) { return read(new File(filespec)); }

	public static String read(File aFile)
		{
//		System.out.println ( "FileContents.read(" + aFile + ")" );
		BufferedReader stream = null;
		try
			{
			stream = SBMisc.utfReader(aFile); 
			if (stream == null )
				{
				System.err.println ( "File open error: " + aFile );
				return null;
				}
			StringBuilder it = new StringBuilder();
			String line;
			while ((line = stream.readLine()) != null )
				it.append(line).append('\n');
			return it.toString();
			}
		catch (Exception e)
			{
			System.err.println(aFile.getName() + " Error: " + e.toString());
			return null;
			}
		finally { try { stream.close(); } catch (Exception ignore) {}}
		}
	}
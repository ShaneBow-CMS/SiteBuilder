package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)DlgBuild.java 1.00 20110104
* Copyright © 2011-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* DlgBuild: Manageges and provides user feedback during the build
* background process. Lists the files as they are processed (e.g.
* converted from .content to .html, or skipped).
* At the end of the build displays a summary or any errors.
*
* @author Rick Salamone
* @version 1.00
* 20110104 rts created based on file worker code
* 20110108 rts using HitList to display results
* 20110108 rts using HitList to display results
* 20130216 rts documentation
*******************************************************/
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.ToggleOnTop;
import com.shanebow.ui.LAF;
import com.shanebow.util.SBLog;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;

public class DlgBuild
	extends JDialog
	{
	private static final String CMD_START="Start";
	private static final String CMD_CANCEL="Cancel";
	private static final String CMD_CLOSE="Close";

	private JButton       button;
	private final JLabel  fStatus = new JLabel("building site");
	private final HitList fHitList = new HitList();
	private int           fBuildCount;
	private int           fErrorCount;
	private int           fSkipCount;
	private int           fCopyCount;

	public DlgBuild(String title)
		{
		super((Frame)null, LAF.getDialogTitle(title), false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel content = new JPanel(new BorderLayout());
		content.add(fHitList,      BorderLayout.CENTER);
		content.add(controlPanel(),BorderLayout.SOUTH);
		fHitList.setBorder( new TitledBorder("Results"));
		content.setBorder(new EmptyBorder(5, 10, 2, 10));
		setContentPane(content);

		pack();
		setLocationByPlatform(true);
		try { setAlwaysOnTop(true); }
		catch (Exception e) {} // guess we can't always be on top!
		LAF.addUISwitchListener(this);
		setVisible(true);
		}

	private JPanel controlPanel()
		{
		JPanel p = new JPanel(new FlowLayout(FlowLayout.TRAILING));

		button = new JButton(CMD_START);
		button.addActionListener( new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				JButton src = (JButton)e.getSource();
				String cmd = src.getText();
				if ( cmd.equals(CMD_START))
					doStart();
				else if ( cmd.equals(CMD_CANCEL))
					setVisible(false);
				else if ( cmd.equals(CMD_CLOSE))
					setVisible(false);
				}
			});
		p.add( button );
		ToggleOnTop tot = new ToggleOnTop();
		p.add( tot );
		tot.setSelected(true);

		JPanel cp = new JPanel(new BorderLayout());
		cp.add(fStatus, BorderLayout.WEST);
		cp.add(p, BorderLayout.EAST);
		return cp;
		}

	public void doStart()
		{
		fBuildCount = fCopyCount = fSkipCount = fErrorCount = 0;
		button.setText(CMD_CANCEL);
		button.requestFocus();
		new BuildWorker()
			{
			@Override protected void process(java.util.List<BuildEvent> events)
				{
				for ( BuildEvent e : events )
					handleBuildEvent(e);
				//	setProgress( line * 100 / fFileCount );
				}

			@Override public void done()
				{
				String summary =	String.format(	"<html><b>%d</b> Built, <b>%d</b> Copied,"
				                    + " <b>%d</b> Skipped, <b>%d</b> Errors (%d seconds)",
						fBuildCount, fCopyCount, fSkipCount, fErrorCount,
						(int)getElapsedSeconds());
				fStatus.setText(summary);
				java.awt.Toolkit.getDefaultToolkit().beep();
				promptClose();
				if ( fErrorCount > 0 )
					reportErrors();
				}
			}.execute();
		}

	private void reportErrors()
		{
		try { setAlwaysOnTop(false); }
		catch (Exception e) {} // guess we can't always be on top!
		SBDialog.error("Build Site", "This operation completed, but\n" + fErrorCount
				+ " exceptions were ecountered.\nSee the list for details");
		}

	protected void promptClose()
		{
		button.setText(CMD_CLOSE);
		button.setVisible(true);
		button.requestFocus();
		}

	public final void handleBuildEvent( BuildEvent aEvent )
		{
setStatus( aEvent.getFile() );
		switch(aEvent.getAction())
			{
			case BUILT:   ++fBuildCount; break;
			case SKIPPED: ++fSkipCount;  break;
			case FAILED:  ++fErrorCount;  break;
			case COPIED:  ++fCopyCount;  break;
			}
		fHitList.add(aEvent);
		}

	private final void setStatus( File f )
		{
		String text = f.toString();
		int length = text.length();
		if ( length > 30 )
			text = text.substring(0,2) + "..." + text.substring(length - 24,length);
		fStatus.setText(text);
		}
	}

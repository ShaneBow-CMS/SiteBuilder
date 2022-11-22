package com.shanebow.web.SiteBuilder.links;
/********************************************************************
* @(#)DlgProgress.java 1.00 20100807
* Copyright © 2010-2015 by Richard T. Salamone, Jr. All rights reserved.
*
* DlgProgress: Provides user feedback during a background process.
* Implemented as a dialog box containing an error/status log,
* progress bar, and buttons. Works in conjuction with FileWorker
* and FileLineParser to display summary results at the end of the
* run, but will also work independently of these classes.
*
* @author Rick Salamone
* @version 1.00
* 20100807 rts created
* 20100822 rts buttons now use trailing flow layout
* 20130216 rts repackaged to reduce common lib size
* 20150129 rts made more subclass friendly including addButtons() hook
*******************************************************/
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBTextPanel;
import com.shanebow.ui.ToggleOnTop;
import com.shanebow.ui.LAF;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public abstract class DlgTaskMonitor
	extends JDialog
	{
	private JProgressBar  fProgressBar;
	protected JButton     fbtnCancel;
	private JButton       fbtnClose;
	private JLabel        fStatus;

	public DlgTaskMonitor(String title)
		{
		super((Frame)null, LAF.getDialogTitle(title), false);
		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(5, 10, 2, 10));
		content.add(mainPanel(), BorderLayout.CENTER);
		content.add(btnPanel(), BorderLayout.SOUTH);
		setContentPane(content);
		pack();
		setLocationByPlatform(true);
		try { setAlwaysOnTop(true); }
		catch (Exception e) {} // guess we can't always be on top!
		LAF.addUISwitchListener(this);
		setVisible(true);
		}

	abstract protected JComponent mainPanel();
	abstract protected void cancel();
	protected void addButtons(JPanel p) {}

	private final JPanel btnPanel()
		{
		JPanel btns = new JPanel(new BorderLayout());
		ToggleOnTop tot = new ToggleOnTop();
		tot.setSelected(true);
		btns.add(tot, BorderLayout.WEST);

		btns.add(fStatus = new JLabel(), BorderLayout.CENTER);

		JPanel p = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		fProgressBar = new JProgressBar(0, 100);
		fProgressBar.setValue(0);

		// Call setStringPainted now so that the progress bar height
		// stays the same whether or not the string is shown.
		fProgressBar.setStringPainted(true);
		p.add(fProgressBar);

		fbtnCancel = new SBAction("Cancel", 'C', "Terminate this process", null) {
			public void action() { promptClose(0); cancel(); }
			}.makeButton();
		fbtnClose = new SBAction("Dismiss", 'D', "Close dialog", null) {
			public void action() { setVisible(false); }
			}.makeButton();
		fbtnClose.setVisible(false);

		p.add(fbtnCancel);
		p.add(fbtnClose);
		addButtons(p);
		btns.add(p, BorderLayout.EAST);
		return btns;
		}

	public final void setProgress(int aWorkDone, int aTotalWork) {
		if (aWorkDone > 0)
			fProgressBar.setIndeterminate(false);
		fProgressBar.setValue(aWorkDone * 100 / aTotalWork);
		}

	public final void promptClose(int aErrorCount)
		{
		java.awt.Toolkit.getDefaultToolkit().beep();
		if (aErrorCount != 0)
			fStatus.setText("<html><font color='red'>"
			                + aErrorCount + " </font>errors");
		fbtnCancel.setVisible(false);
		fbtnClose.setVisible(true);
		fbtnClose.requestFocus();
		}
	}

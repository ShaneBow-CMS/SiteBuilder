package com.shanebow.web.SiteBuilder.editor;
/********************************************************************
* @(#)ContentTab.java 1.00 20111019
* Copyright © 2011-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* ContentTab: Extends UniEditor to allow editing the SEO information
* in addition to the content (which is handled by the super class).
*
* @author Rick Salamone
* @version 1.00, 20111019 rts created to allow edit SEO info
*******************************************************/
import com.shanebow.tools.uniedit.UniEditor;
import com.shanebow.web.SiteBuilder.pages.SitePages;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

final public class ContentTab
	extends UniEditor
	{
	private SBAction fActHead; // Launches dlg to enter <head> data
	private SBAction fActSEO; // Launches dlg to enter meta data
	private SBAction fActKamsap; // Launches dlg to enter Thai words/sentences
	private final SEOEditor fSEOEditor = new SEOEditor();
	private final HeadEditor fHeadEditor = new HeadEditor();
	private final JPopupMenu   fPopup = new JPopupMenu();
	private DlgKamsap dlgKamsap;

	public ContentTab(String aSubDir) { this(aSubDir, ".content", true); }
	public ContentTab(String aSubDir, String aFileExt, boolean aShowTree)
		{
		super(aSubDir, aFileExt, aShowTree);
//		setDropHandler( new LinkDropHandler()); // should set here but only works in superclass
//		fContentEditor.setTransferHandler(new LinkDropHandler());

		fPopup.add(fActHead);
		fPopup.add(fActKamsap);
		fContentEditor.addMouseListener ( new MouseAdapter ()
			{
			private void showPopup (MouseEvent e)
				{
				fContentEditor.setCaretPosition(fContentEditor.viewToModel(e.getPoint()));
fContentEditor.insert("HELLO");
System.out.println("INSERT");
	//			fPopup.show (e.getComponent(), e.getX(), e.getY());
				}

			public void mousePressed(MouseEvent e)
				{
System.out.println("MOUSE PRESSED");
/******
					JTextArea ta = (JTextArea)e.getSource();
					try
						{
						fLine = ta.getLineOfOffset(ta.getCaretPosition());
						fChart.onSelectM5(fProblems.get(fLine));
						}
					catch (Exception ex) { System.out.println("click error: " + ex); }
******/
				if (SwingUtilities.isRightMouseButton(e)) showPopup(e);
				}

			public void mouseReleased (MouseEvent e)
				{
System.out.println("MOUSE RELEASED");
/******
				if (e.getClickCount() == 2)// doubleClick ();
					{
					JTextArea ta = (JTextArea)e.getSource();
					try
						{
						fLine = ta.getLineOfOffset(ta.getCaretPosition());
						fChart.onRepair(fProblems.get(fLine));
						}
					catch (Exception ex) { System.out.println("click error: " + ex); }
					}
******/
				}
			});

		}

	@Override protected void save(File aFile)
		{
		super.save(aFile);
		SitePages.saved(aFile);
		}

	@Override protected JPanel leftPanel()
		{
		JPanel it = super.leftPanel();
		it.add(Box.createRigidArea(BOX_SPACER));
		it.add(moreTools());
		return it;
		}

	private JComponent moreTools()
		{
		fActKamsap = new SBAction("Kamsap", 'K', "Edit thai vocab on this page", null)
			{
			public void action() {
				if (dlgKamsap == null)
					dlgKamsap = new DlgKamsap();
				dlgKamsap.show(selected(".thai"));
				}
			};
		fActKamsap.setEnabled(false);

		fActHead = new SBAction("Head", 'H', "Edit scripts/styles for this page", null)
			{
			public void action() { edit(".head", fHeadEditor); }
			};
		fActHead.setEnabled(false);

		fActSEO = new SBAction("SEO", 'O', "Optimize for Search Engines", null)
			{
			public void action() { edit(".seo", fSEOEditor); }
			};
		fActSEO.setEnabled(false);

		JPanel it = new JPanel(new GridLayout(1,0));
		it.add(fActHead.makeButton());
		it.add(fActSEO.makeButton());
		it.add(fActKamsap.makeButton());
		return it;
		}

	@Override protected void fileSelected(File aFileOrDir)
		{
		super.fileSelected(aFileOrDir);
		File file = getSelectedFile();
		if (dlgKamsap != null)
			dlgKamsap.reset(selected(".thai"));
		boolean haveFile = file != null;
		fActHead.setEnabled(haveFile);
		fActSEO.setEnabled(haveFile);
		fActKamsap.setEnabled(haveFile);
		}

	private File selected(String aExt)
		{
		File file = getSelectedFile();
		if (file == null) return null;
		String filespec = getSelectedFile().getPath();
		int dotAt = filespec.lastIndexOf('.');
		return new File(filespec.substring(0,dotAt) + aExt);
		}

	private void edit(String aExt, AbstractContentEditor aEditor)
		{
		aEditor.edit(selected(aExt), this);
		}
	}

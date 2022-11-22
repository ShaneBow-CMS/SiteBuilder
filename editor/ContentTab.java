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
import java.io.File;
import javax.swing.*;

final public class ContentTab
	extends UniEditor
	{
	private SBAction fActHead; // Launches dlg to enter <head> data
	private SBAction fActSEO; // Launches dlg to enter meta data
	private final SEOEditor fSEOEditor = new SEOEditor();
	private final HeadEditor fHeadEditor = new HeadEditor();

	public ContentTab(String aSubDir) { super(aSubDir, ".content"); }
	public ContentTab(String aSubDir, String aFileExt, boolean aShowTree)
		{
		super(aSubDir, aFileExt, aShowTree);
//		setDropHandler( new LinkDropHandler()); // should set here but only works in superclass
//		fContentEditor.setTransferHandler(new LinkDropHandler());
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
		fActHead = new SBAction("Head", 'H', "Edit scripts/styles for this page", null)
			{
			public void actionPerformed(ActionEvent e) { edit(".head", fHeadEditor); }
			};
		fActHead.setEnabled(false);

		fActSEO = new SBAction("SEO", 'O', "Optimize for Search Engines", null)
			{
			public void actionPerformed(ActionEvent e) { edit(".seo", fSEOEditor); }
			};
		fActSEO.setEnabled(false);

		JPanel it = new JPanel(new GridLayout(1,0));
		it.add(fActHead.makeButton());
		it.add(fActSEO.makeButton());
		return it;
		}

	@Override protected void fileSelected(File aFile)
		{
		super.fileSelected(aFile);
		boolean haveFile = getSelectedFile() != null;
		fActHead.setEnabled(haveFile);
		fActSEO.setEnabled(haveFile);
		}

	private File selected(String aExt)
		{
		String filespec = getSelectedFile().getPath();
		int dotAt = filespec.lastIndexOf('.');
		return new File(filespec.substring(0,dotAt) + aExt);
		}

	private void edit(String aExt, AbstractContentEditor aEditor)
		{
		aEditor.edit(selected(aExt), this);
		}
	}

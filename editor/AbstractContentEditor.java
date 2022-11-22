package com.shanebow.web.SiteBuilder.editor;
/********************************************************************
* @(#)AbstractContentEditor.java 1.00 20111022
* Copyright © 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* AbstractContentEditor: Abstract base class for components that edit
* pieces of the content that are ultimately cobbled together to create
* an html file. For instance, an seo editor would extend this class to
* allow editing some meta tags in the head.
*
* @author Rick Salamone
* @version 1.00, 20111022 rts created
*******************************************************/
import com.shanebow.web.SiteBuilder.pages.SitePages;
import com.shanebow.ui.LAF;
import java.awt.*;
import java.io.File;
import javax.swing.*;

abstract class AbstractContentEditor
	extends JPanel
	{
	public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	final class WrappedArea extends JTextArea
		{
		WrappedArea(int r, int c)
			{
			super(r, c);
			setFont(DEFAULT_FONT);
			setLineWrap(true);
			setWrapStyleWord(true);
			setTabSize(3);
			setForeground(Color.BLUE);
			}
		}

	public AbstractContentEditor()
		{
		super(new BorderLayout());
		setPreferredSize(new Dimension(500,400));
		}

	abstract boolean isEmpty();
	abstract void clear();
	abstract void read( File aFile );
	abstract void write( File aFile );

	private static final String[] OPTIONS = { "Save", "Cancel" };
	public final void edit(File aFile, Component aOwner)
		{
		try
			{
			if (aFile.exists()) read(aFile);
			else clear();
			if ( 0 != JOptionPane.showOptionDialog(aOwner, this,
				LAF.getDialogTitle("Edit "+aFile.getName()), JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, OPTIONS, OPTIONS[0] ))
				return;
			_write(aFile);
			}
		catch (Exception ignore) {}
		}

	private final void _write(File aFile)
		{
		if ( isEmpty())
			aFile.delete();
		else
			write(aFile);	
		SitePages.saved(aFile);
		}
	}

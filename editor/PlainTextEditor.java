package com.shanebow.web.SiteBuilder.editor;
/********************************************************************
* @(#)PlainTextEditor.java 1.00 20111022
* Copyright © 2011-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* PlainTextEditor: Extends AbstractContentEditor to provide a simple
* dialog editor for plain text files. Originally created for editing
* the site's robot.txt file, but probably has other uses.
*
* @author Rick Salamone
* @version 1.00
* 20111027 rts created
* 20160111 rts support for char encodings
*******************************************************/
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.SBFileSaver;
import com.shanebow.util.SBMisc;
import java.awt.*;
import java.io.*;
import javax.swing.*;

public final class PlainTextEditor
	extends AbstractContentEditor
	{
	private final JTextArea fTextArea = new WrappedArea(6, 50);

	public PlainTextEditor()
		{
		super();
		add( new JScrollPane(fTextArea), BorderLayout.CENTER);
		}

	@Override void clear() { fTextArea.setText(""); }

	@Override void read( File aFile )
		{
		clear();
		BufferedReader fis = null;
		try
			{
			fis = SBMisc.utfReader(aFile);
			String line;
			while ((line = fis.readLine()) != null )
				fTextArea.append(line + "\n");
			}
		catch (Exception e) { SBDialog.error("File Open Error", e.toString(), this); }
		finally { try { fis.close(); } catch (Exception ignore) {} }
		}

	@Override boolean isEmpty() { return fTextArea.getText().isEmpty(); }

	@Override void write(File aFile)
		{
		Thread saver = new SBFileSaver(aFile, fTextArea.getDocument());
		saver.start();
		}
	}

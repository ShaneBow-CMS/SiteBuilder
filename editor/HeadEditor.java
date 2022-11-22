package com.shanebow.web.SiteBuilder.editor;
/********************************************************************
* @(#)HeadEditor.java 1.00 20111020
* Copyright © 2011-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* HeadEditor: Component to edit the contents of a ".head" file which
* contains style and script entries specific to a single HTML file.
*
* @version 1.00
* @author Rick Salamone
* 20111022 rts created
* 20111027 rts now has separate areas for style & script
* 20121220 rts now field for adding css files
* 20130306 rts bug fix - read head was putting js files in the css field
*******************************************************/
import com.shanebow.web.html.ParseHTML;
import com.shanebow.web.html.HTMLTag;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBDialog;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;

public final class HeadEditor
	extends AbstractContentEditor
	{
	private static final String STYLE_OPEN = "<style>";
	private static final String SCRIPT_OPEN = "<script type=\"text/javascript\">";

	private final JTextField tfCSSFiles = new JTextField();
	private final JTextField tfJSFiles = new JTextField();
	private final JTextArea  tfStyle = new WrappedArea(4, 50);
	private final JTextArea  tfScript = new WrappedArea(4, 50);

	public HeadEditor()
		{
		super();
		JPanel top = new JPanel(new GridLayout(0,1));
		top.add( LAF.titled("include css", tfCSSFiles));
		top.add( LAF.titled("include js", tfJSFiles));
		JPanel mid = new JPanel(new GridLayout(0,1));
		mid.add( LAF.titled(STYLE_OPEN,  new JScrollPane(tfStyle)));
		mid.add( LAF.titled(SCRIPT_OPEN, new JScrollPane(tfScript)));
		add( top, BorderLayout.NORTH);
		add( mid, BorderLayout.CENTER);
		}

	@Override void clear()
		{
		tfCSSFiles.setText("");
		tfJSFiles.setText("");
		tfStyle.setText("");
		tfScript.setText("");
		}

	@Override void read( File aFile )
		{
		clear();
		FileInputStream fis = null;
		try
			{
			ParseHTML parser = new ParseHTML(fis = new FileInputStream(aFile));
			while (parser.advanceToTag())
				{
				HTMLTag tag = parser.getTag();
				String tagName = tag.getName().toLowerCase();
				if ( tagName.equals("style"))
					tfStyle.setText(parser.cdata().trim());
				else if ( tagName.equals("script"))
					{
					String src = tag.getAttributeValue("src");
					if ( src != null )
						append(tfJSFiles, src);
					else
						tfScript.setText(parser.cdata().trim());
					}
				// <link rel='stylesheet' type='text/css' href='./css/t_calendar.css'>
				else if ( tagName.equals("link"))
					{
					String src = tag.getAttributeValue("href");
					if ( src != null )
						append(tfCSSFiles, src);
					}
				}
			}
		catch (Exception e) { SBDialog.error("File Open Error", e.toString(), this); }
		finally { try { fis.close(); } catch (Exception ignore) {} }
		}

	private void append(JTextField tf, String src)
		{
		String csv = tf.getText();
		if ( !csv.isEmpty())
			csv += ",";
		tf.setText(csv + src);
		}

	@Override boolean isEmpty()
		{
		return tfCSSFiles.getText().trim().isEmpty()
		    && tfJSFiles.getText().trim().isEmpty()
		    && tfStyle.getText().trim().isEmpty()
		    && tfScript.getText().trim().isEmpty();
		}

	@Override void write(File aFile)
		{
		PrintWriter out = null;
		try
			{
			out = new PrintWriter( aFile );
			// <link rel='stylesheet' type='text/css' href='./css/t_calendar.css'>
			String cssFiles = tfCSSFiles.getText().trim();
			if ( !cssFiles.isEmpty())
				for ( String css : cssFiles.split(","))
					out.println("<link rel='stylesheet' type='text/css' href='" + css + "'>");

			String jsFiles = tfJSFiles.getText().trim();
			if ( !jsFiles.isEmpty())
				for ( String jsFile : jsFiles.split(","))
					out.println("<script src=\"" + jsFile + "\"></script>");

			_writeTag(out, STYLE_OPEN,  tfStyle,  "</style>" );
			_writeTag(out, SCRIPT_OPEN, tfScript, "</script>" );
			}
		catch (Exception e) { SBDialog.error("File Save Error", e.toString(), this); }
		finally { try { out.close(); } catch (Exception ignore) {} }
		}

	private void _writeTag(PrintWriter pw, String aTagOpen, JTextArea ta, String aTagClose)
		throws Exception
		{
		String text = ta.getText();
		if (ta.getText().trim().isEmpty())
			return;
		pw.println(aTagOpen);
		int totalLines = ta.getLineCount();
		for (int i=0; i < totalLines; i++)
			{
			int start = ta.getLineStartOffset(i);
			int end = ta.getLineEndOffset(i);
			pw.print(text.substring(start, end));
			}
		pw.println("\n" + aTagClose);
		}
	}

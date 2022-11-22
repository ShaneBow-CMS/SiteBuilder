package com.shanebow.web.SiteBuilder.editor;
/********************************************************************
* @(#)SEOEditor.java 1.00 20111020
* Copyright © 2011-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* SEOEditor: Component to edit the contents of a ".seo" file which
* contains HTML headers related to SEO.
*
* @author Rick Salamone
* @version 1.00
* 20111020 rts created
* 20120711 rts title suffix uses domain name property
*******************************************************/
import static com.shanebow.web.SiteBuilder.build.BuildCriteria.PKEY_DOMAIN;
import com.shanebow.web.html.ParseHTML;
import com.shanebow.web.html.HTMLTag;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBDialog;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;

public final class SEOEditor
	extends AbstractContentEditor
	{
	private final JTextArea tfTitle = new WrappedArea(1, 50);
	private final JTextArea tfDesc = new WrappedArea(4, 50);
	private final JTextArea tfKeyWords = new WrappedArea(4, 50);
	private final JLabel    lblOther = new JLabel();

	public SEOEditor()
		{
		super();
		JPanel top = new JPanel(new BorderLayout());
		top.add( tfTitle, BorderLayout.CENTER );
		JPanel mid = new JPanel(new GridLayout(0,1));
		mid.add( LAF.titled("Description", new JScrollPane(tfDesc)));
		mid.add( LAF.titled("Key Words",   new JScrollPane(tfKeyWords)));
		mid.add( LAF.titled("Other",       new JScrollPane(lblOther)));
		add( LAF.titled("Title", top), BorderLayout.NORTH);
		add( mid, BorderLayout.CENTER);
		}

	@Override void clear()
		{
		tfTitle.setText("");
		tfDesc.setText("");
		tfKeyWords.setText("");
		lblOther.setText("");
		}

	@Override void read( File aFile )
		{
		clear();
		FileInputStream fis = null;
		try
			{
			ParseHTML parser = new ParseHTML(fis = new FileInputStream(aFile));
			if ( parser.advanceTo("title", 1))
				{
				String title = parser.cdata();
				String titleSuffix = " &rsaquo; " + SBProperties.get(PKEY_DOMAIN);
				title = title.substring(0, title.length()-titleSuffix.length());
				tfTitle.setText(title);
				}
			String otherMeta = "<html>";
			while ( parser.advanceTo( "meta", 1 ))
				{
				HTMLTag tag = parser.getTag();
				String name = tag.getAttributeValue("name");
				if ( name == null )
					continue;
				String content=tag.getAttributeValue("content");
				if ( name.equals("keywords"))
					tfKeyWords.setText(content);
				else if ( name.equals("description"))
					tfDesc.setText(content);
				else otherMeta += "<b>" + name + "</b> " + content + "<br>";
				}
			lblOther.setText(otherMeta);
			}
		catch (Exception e) { SBDialog.error("File Open Error", e.toString(), this); }
		finally { try { fis.close(); } catch (Exception ignore) {} }
		}

	@Override boolean isEmpty()
		{
		return tfTitle.getText().trim().isEmpty()
		    && tfDesc.getText().trim().isEmpty()
		    && tfKeyWords.getText().trim().isEmpty();
		}

	@Override void write(File aFile)
		{
		String title = tfTitle.getText().trim();
		String desc  = tfDesc.getText().trim();
		String keys  = tfKeyWords.getText().trim();
		PrintWriter out = null;
		try
			{
			out = new PrintWriter( aFile );
			String titleSuffix = " &rsaquo; " + SBProperties.get(PKEY_DOMAIN);
			out.println("<title>" + title + titleSuffix + "</title>");
			meta( out, "author", "Rick Salamone" );
			meta( out, "generator", "Salamone Site Builder" );
			meta( out, "description", desc );
			meta( out, "keywords", keys );
			}
		catch (Exception e) { SBDialog.error("File Save Error", e.toString(), this); }
		finally { try { out.close(); } catch (Exception ignore) {} }
		}

	public void meta(PrintWriter aPW, String aName, String aContent)
		throws IOException
		{
		aPW.println("  <meta name=\"" + aName + "\" content=\"" + aContent + "\" />");
		}
	}

package com.shanebow.web.SiteBuilder.dbm;
/********************************************************************
* @(#)DBSettings.java	1.0 20100428
* Copyright © 2010-2015 Richard T. Salamone, Jr All Rights Reserved.
*
* PDBSettings: Site's MySQL settings
*
* @author Rick Salamone
* @version 2.0
* 20100428 rts created in DlgConnect for SQL app
* 20110908 rts decoupled from DlgConnect
* 20140726 rts revised fields for Site builder dbm module
* 20151023 rts total rewrite to read and write dbXXX.cfg files
* 20160112 rts using the include prefix constant
* 20160401 rts now looking for files in BuildCriteria.srcPublicDir()/apps
*******************************************************/
import static com.shanebow.web.SiteBuilder.build.PageLayout.INCLUDE_PREFIX;
import com.shanebow.web.SiteBuilder.build.BuildCriteria;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.layout.LabeledPairPanel;
import com.shanebow.util.SBProperties;
import java.io.*;
import javax.swing.*;

public final class DBSettings
	extends LabeledPairPanel
	{
	static final String ROX_INCLUDE="" + INCLUDE_PREFIX + INCLUDE_PREFIX + "ubow/apps/rox-db.cfg";
	static final String DB_FILE = "db.cfg";
	static final String DB_LAMRON_FILE = "db-lamron.cfg";
	static final String DB_NIMDA_FILE = "nimda/db-nimda.cfg";

	static final String[] LABELS = {
		"dbname",     // DB_FILE
		"dbhost",
		"tblPrefix",

		"n:dbusr",      // DB_LAMRON_FILE
		"n:dbpwd",

		"a:dbusr",      // DB_NIMDA_FILE
		"a:dbpwd"
		};

	private final JTextField[] tfields = new JTextField[LABELS.length];
	private final JCheckBox chkRox = new JCheckBox("Rox enabled");

	public DBSettings()
		{
		super(4,2);
		for (int i = 0; i < LABELS.length; i++)
			addRow(LABELS[i], tfields[i] = new JTextField(50));
		addRow("", chkRox);
		setBorder(LAF.getBorder(LAF.bevel(5,5), 5));
		}

	private BufferedReader open(File root, String filespec) {
		try { return new BufferedReader(new FileReader(new File(root, filespec))); }
		catch (FileNotFoundException e) { return null; }
		}
	private String readField(BufferedReader in) {
		String it = "";
		try {
			String line = in.readLine();
			boolean infield = false;
			for (int i = 0; i < line.length(); i++ )
				if (line.charAt(i) == '\'') {
					if (infield) break;
					else infield = true;
					}
				else if (infield) it += line.charAt(i);
			}
		catch (Exception e) {}
		return it;
		}

	public final void read()
		{
		File srcRoot = BuildCriteria.appDir();
		BufferedReader in = open(srcRoot, DB_FILE);
		tfields[0].setText(readField(in));
		tfields[1].setText(readField(in));
		tfields[2].setText(readField(in));
		try { chkRox.setSelected(in.readLine().trim().charAt(0) != '-'); }
		catch (Exception e) { chkRox.setSelected(false); }
		try { in.close(); } catch(Exception ignore){}

		in = open(srcRoot, DB_LAMRON_FILE);
		tfields[3].setText(readField(in));
		tfields[4].setText(readField(in));
		try { in.close(); } catch(Exception ignore){}

		in = open(srcRoot, DB_NIMDA_FILE);
		tfields[5].setText(readField(in));
		tfields[6].setText(readField(in));
		try { in.close(); } catch(Exception ignore){}
		}

	private String nameValue(int fld)
		{
		int colonAt = LABELS[fld].indexOf(":") + 1;
		return "$" + LABELS[fld].substring(colonAt)
		     + " = '" + tfields[fld].getText() + "';";
		}

	private void writeFile(File root, String file, int fld0, int fld1) {
		PrintWriter out = null;
		File f = new File(root, file);
		try {
			out = new PrintWriter(f);
			for (int i = fld0; i <= fld1; i++)
				out.println(nameValue(i));
			if (file.equals(DB_FILE))
				out.println((chkRox.isSelected()?"":"-") + ROX_INCLUDE);
			}
		catch (Exception e) { SBDialog.error("Write " + f, e.getMessage()); }
		finally { try { out.close(); } catch (Exception e) {} }
		}
			
	public final void save()
		{
		File srcRoot = BuildCriteria.appDir();
		writeFile(srcRoot, DB_FILE, 0, 2);
		writeFile(srcRoot, DB_LAMRON_FILE, 3, 4);
		writeFile(srcRoot, DB_NIMDA_FILE, 5, 6);
		}
	}

package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)SiteVars.java 1.00 20170904
* Copyright © 2011-2017 by Richard T. Salamone, Jr. All rights reserved.
*
* SiteVars:
*
* @author Rick Salamone
* @version 1.00
* 20170904 rts created
*******************************************************/
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.layout.LabeledPairPanel;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public final class SiteVars
	extends JPanel
	{
	private static final String[] fHeads = {"Key", "Value"};
	private static String[] fKeys = {
		"DOMAIN",
		"SITE_NAME",
		"UDB_PREFIX",
		"DEFAULT_VIEW",
		"SITE_LOCK_URL"
		};
	private static String[] fValues = new String[fKeys.length];
		
	private final AbstractTableModel fModel = new AbstractTableModel(){
		public int getRowCount() { return fKeys.length; }
		public int getColumnCount() { return fHeads.length; }
		public Object getValueAt(int r, int c) { return (c==0)? fKeys[r] : fValues[r]; }
		public boolean isCellEditable(int r, int c) { return (c == 1); }
		public String  getColumnName(int c) { return fHeads[c]; }
		public void setValueAt(Object value, int r, int c) {
			fValues[r] = value.toString();
			fireTableCellUpdated(r, c);
			}
		};

	public SiteVars() {
		super(new BorderLayout());
//		add( new JScrollPane(taLog), BorderLayout.NORTH);

		JTable table = new JTable(fModel);
		table.setFillsViewportHeight(true);
		add( new JScrollPane(table), BorderLayout.CENTER);
		}

	public static String process(String line) {
		int i = 0;
		for (String key : fKeys) {
			line = line.replace("~" + key + "~", fValues[i++]);
			if (line.indexOf(PageLayout.INCLUDE_PREFIX) < 0) break;
			}
		return line;
		}

	public static final void load() {
		SBProperties props = SBProperties.getInstance();
		int i = 0;
		for (String key : fKeys)
			fValues[i++] = props.getProperty("cfg." + key,"");
		}

	public void populate()
		{
		load();
		}

	public boolean save() {
		SBProperties props = SBProperties.getInstance();
		int i = 0;
		for (String key : fKeys) {
			System.out.println("-- setProperty(cfg." + key + "," + fValues[i] + ")" );
			props.setProperty("cfg." + key, fValues[i++]);
			}
		return true;
		}
	}

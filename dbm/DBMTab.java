package com.shanebow.web.SiteBuilder.dbm;
/********************************************************************
* @(#)DBMTab.java 1.00 20140726
* Copyright © 2014-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* DBMTab: Database Management Tab for creating, maintaining and
* viewing contents of the site's database tables
*
* @author Rick Salamone
* @version 1.00
* 20140726 rts created
* 20151020 rts added extra buttons for creating rox db
* 20160118 rts create table now a wizard
* 20160129 rts added Tables & Post actions
* 20160401 rts uses srcPublicDir & shows inform dlg if creates subdir
*******************************************************/
import com.shanebow.tools.uniedit.RootedFileNavigator;
import com.shanebow.web.host.*;
import com.shanebow.web.SiteBuilder.build.BuildCriteria;
import com.shanebow.web.SiteBuilder.editor.PlainTextEditor;
import com.shanebow.ui.FontSizeSlider;
import com.shanebow.ui.Reloadable;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.SplitPane;
import com.shanebow.ui.layout.LabeledPairPanel;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.AbstractTableModel;

final public class DBMTab
	extends SplitPane
	implements Reloadable
	{
	private File fSelectedFile;
	private SBAction fActEdit; // Launches dlg to edit selected file
	private final PlainTextEditor fTDefEditor = new PlainTextEditor();
	private final RootedFileNavigator fFilesList;
	private final Host fHost = new Host();
	private final DBTableView fTableView = new DBTableView();
	private File fCWD;
	private final DBSettings fDBConfig = new DBSettings();
	private String post_dir;

	public DBMTab()
		{
		super(HSPLIT);
		fCWD = new File(".");
		fFilesList = new RootedFileNavigator(fCWD, null, true); // hide file ext?
		setLeftComponent(leftPanel());
		setRightComponent(fTableView);
		setDividerLocation("usr.uedit.split", 260);

		CompoundBorder border = new CompoundBorder(new EmptyBorder(5,5,5,5),
		                            new SoftBevelBorder(SoftBevelBorder.LOWERED));
		fFilesList.setBorder(border);
		fTableView.setBorder(border);

		fFilesList.addPropertyChangeListener(RootedFileNavigator.SELECTED_FILE,
			new PropertyChangeListener()
				{
				@Override public void propertyChange(PropertyChangeEvent e)
					{
					fileSelected((File)(e.getNewValue()));
					}
				});
//		setDropHandler( new LinkDropHandler()); // should set here but only works in superclass
//		fTableView.setTransferHandler(new LinkDropHandler());
		}

	@Override public boolean exitOK() { return true; }

	@Override public boolean reload()
		{
		fCWD = BuildCriteria.appDir();
		fHost.reload();
		fTableView.say("Posting to " + fHost);
		if (!fCWD.exists()) {
			SBDialog.inform(this.getClass().getSimpleName(),"<html>Creating directory:<br>"+fCWD);
			fCWD.mkdir();
			}
		fFilesList.resetRoot( fCWD );
		fDBConfig.read();
		return true;
		}

	private final JPanel leftPanel()
		{
		String fontKey = FontSizeSlider.DEFAULT_FONTSIZE_KEY;
		Dimension BOX_SPACER = new Dimension(0, 5);
		JPanel it = new JPanel();
		it.setLayout(new BoxLayout(it, BoxLayout.PAGE_AXIS));
		it.add(fFilesList);
		it.add(Box.createVerticalGlue());
		it.add(Box.createRigidArea(BOX_SPACER));
		it.add(fHost.fDomainRadio);
		it.add(Box.createRigidArea(BOX_SPACER));
		it.add(fDBConfig);
		it.add(Box.createRigidArea(BOX_SPACER));
		it.add(new FontSizeSlider(fTableView, fontKey));
		it.add(Box.createRigidArea(BOX_SPACER));
		it.add(moreTools());
		return it;
		}

	private JComponent moreTools()
		{
		fActEdit = new SBAction("Edit", 'E', "Edit selected file", null){
			public void action() {edit();}
			};
		fActEdit.setEnabled(false);

		SBAction fActSaveCfg = new SBAction("Save", 'S', "Save dbXXX.cfg files", null) {
			public void action() { fDBConfig.save(); }
			};

		SBAction fActUDB = new SBAction("UDB", 'U', "User apps/nimda/udb.php", null){
			public void action() {
				String app = fHost.appPath("nimda", "udb.php");
				String data = "t=member,audit,session";
				fHost.postView(name(), app, data);
				}
			};

		SBAction fActRDB = new SBAction("RDB", 'R', "Build Rox Tables", null){
			public void action() {
				String app = fHost.appPath("nimda", "rdb.php");
				String data = "";
				fHost.postView(name(), app, data);
				}
			};

		SBAction fActBDB = new SBAction("BDB", 'B', "Blog apps/nimda/bdb.php", null){
			public void action() {
				String app = fHost.appPath("nimda", "bdb.php");
				String data = "t=blog";
				fHost.postView(name(), app, data);
				}
			};

		SBAction fActCreateTbl = new SBAction("Create", 'C', "Create DB Table", null){
			public void action() { new WizDBTblCreate(getFrame(), fHost); }
			};

		SBAction fActDropTbl = new SBAction("Drop", 'D', "Drop DB Table", null){
			public void action() {
				String app = fHost.appPath("nimda", "dba.php");
				String tbl = JOptionPane.showInputDialog(
					"<html><b>Beware!</b> Enter table to drop:", selectedTable());
				if (tbl == null)
					return;
				String data = "drop=" + tbl;
				fHost.postView(name(), app, data);
				}
			};

		SBAction fActMyISAM = new SBAction("MyISAM", 'I', "Convert to MyISAM engine", null){
			public void action() {
				String app = fHost.appPath("nimda", "dba.php");
				String tbl = JOptionPane.showInputDialog(
					"<html><b>Beware!</b> Enter table to Convert to MyISAM:", selectedTable());
				if (tbl == null)
					return;
				String data = "myisam=" + tbl;
				fHost.postView(name(), app, data);
				}
			};

		SBAction fActRows = new SBAction("Rows", 'R', "List table data", null){
			public void action() {
				String tbl = JOptionPane.showInputDialog("Enter table name:", selectedTable());
				if (tbl != null)
					_tableRows(tbl);
				}
			};

		SBAction fActTables = new SBAction("Tables", 'T', "List tables in db", null){
			public void action() { _tableRows(""); }
			};

		SBAction fActPost = new SBAction("Post", 'P', "Post an arbitrary message to host", null){
			@Override public void action() {
		 		String[] options = { "OK", "Cancel" };
				JTextField tfApp = new JTextField(fHost.appPath("nimda/"));
				JTextField tfDat = new JTextField(25);
				LabeledPairPanel panel = new LabeledPairPanel();
				panel.addRow("app",tfApp);
				panel.addRow("dat",tfDat);
				if ( JOptionPane.OK_OPTION != JOptionPane.showOptionDialog(null, panel,
					LAF.getDialogTitle(toString()), JOptionPane.OK_CANCEL_OPTION, // JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, null, null)) //options, options[0] ))
					return;
				String app = tfApp.getText();
				String dat = tfDat.getText();
				fHost.postView(name(), app, dat);
				}
			};

		SBAction fActTest = new SBAction("Test", 'T', "Run test.php", null){
			public void action() { new WizTestMailIn(getFrame()); }
			};

		JPanel it = new JPanel(new GridLayout(0,3));
		it.add(fActEdit.makeButton());
		it.add(fActSaveCfg.makeButton());
		it.add(fActPost.makeButton());
		it.add(fActUDB.makeButton());
		it.add(fActRDB.makeButton());
		it.add(fActBDB.makeButton());
		it.add(fActDropTbl.makeButton());
		it.add(fActCreateTbl.makeButton());
		it.add(fActTables.makeButton());
		it.add(fActRows.makeButton());
		it.add(fActMyISAM.makeButton());
		it.add(fActTest.makeButton());
		return it;
		}

	/**
	* @param tbl - list the columns in this table
	* if blank, will list all the tables in the db
	*/
	private void _tableRows(String tbl) {
		String app = fHost.appPath("dbm1.php");
		boolean headers = true;
		String data = "tbl=" + tbl;
		PostResponse resp = fHost.post(app, data);
		if (resp.err == 0) {
			String[] pieces = resp.dat.split("\\^",3);
			int nRows = Integer.parseInt(pieces[0],10);
			int nCols = Integer.parseInt(pieces[1],10);
			fTableView.display(pieces[2], nCols, '^', headers);
			}
		else
			new DlgPostResponse(tbl.isEmpty()?"DB Tables" : ("Rows in "+tbl), app, data, resp);
		}

	void edit() {fTDefEditor.edit(fSelectedFile, this);}

	private Frame getFrame() { return (Frame)SwingUtilities.getAncestorOfClass(Frame.class, this); }

	/**
	* If the the selected file follows the naming scheme tbl-some-name.sql,
	* this method returns "some-name", otherwise it returns an empty String
	*/
	private String selectedTable() {
		if (fSelectedFile != null) {
			String name = fSelectedFile.getName();
			if (name.startsWith("tbl-") && name.endsWith(".sql"))
				return name.substring(4,name.length()-4);
			}
		return "";
		}

	void fileSelected(File aFile)
		{
		if ( aFile != null )
			{
			if (aFile.isDirectory()) {
				fCWD = aFile;
				fSelectedFile = null;
				}
			else if ( !aFile.equals(fSelectedFile))
				fTableView.fileSelected(fSelectedFile = aFile);
			}
// else cmdFileNew();
		fTableView.requestFocus();
		fActEdit.setEnabled(fSelectedFile != null);
		}
	}

class DBTableView
	extends JPanel
	{
	private int ncols = 1;
	private String[] data = {""};
	private String[] head;
	private final AbstractTableModel fModel = new AbstractTableModel(){
		public int getRowCount() { return data.length / ncols; }
		public int getColumnCount() { return ncols; }
		public Object getValueAt(int r, int c) { return data[r * ncols + c]; }
		public boolean isCellEditable(int r, int c) { return false; }
		public String  getColumnName(int c) { return (head == null)?  "" + c : head[c]; }
		};
	JTextArea taLog = new JTextArea(10,50);

	DBTableView() {
		super(new BorderLayout());
		add( new JScrollPane(taLog), BorderLayout.NORTH);

		JTable table = new JTable(fModel);
		table.setFillsViewportHeight(true);
		add( new JScrollPane(table), BorderLayout.CENTER);
		}

	void say(String msg) {taLog.append("\n" + msg);}
	void fileSelected(File aFile) {say("fileSelected(" + aFile + ")");}

	void display(String dat, int ncols, char aSeparator, boolean hasHeadings)
		{
		say(dat);
		if ( hasHeadings )
			{
			head = dat.split("\\^", ncols + 1);
			if (head.length > ncols)
				{
				dat = head[ncols];
				head[ncols] = null;
				}
			else dat = ""; // no rows in table
			}
		else head = null;
		this.data = dat.split("\\^"); // + aSeparator);
		this.ncols = ncols;
		say("ncols: " + ncols + " nfields : " + data.length);
		fModel.fireTableStructureChanged();
		}
	}

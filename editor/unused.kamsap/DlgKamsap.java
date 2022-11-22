package com.shanebow.web.SiteBuilder.editor;
/********************************************************************
* @(#)DlgKamsap.java 1.00 20140917
* Copyright © 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* DlgKamsap: Displays all thai entries on the current page and allows
* user to create sentences with these words that can be inserted into
* the page.
*
* @author Rick Salamone
* @version 2.00
* 20140917 rts created
*******************************************************/
import com.thaidrills.lib.*;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.SplitPane;
import com.shanebow.ui.ToggleOnTop;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import com.shanebow.util.TextFile;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

public final class DlgKamsap
	extends JDialog
	{
	class KamsapFileTableModel
		extends KamsapTableModel
		{
		KamsapFileTableModel() {
			super();
			fList = new KamsapList();
			}

		void add(Kamsap aKamsap) {
			if (((KamsapList)fList).addNoDup(aKamsap))
				fireTableDataChanged();
			}

		void save() {
System.out.println("SAVE CURRENT FILE NOT IMPLEMNTED");
	//		TextFile.freeze( fList, filespec(MASTER), null );
			}
		void read() {
			fList.clear();
			if (fCurrentFile != null)
				{
//				String filespec = filespec(MASTER);
				}
			fireTableDataChanged();
			}
		}

String masterFilespec = "c:/apps/src/com/thaidrills/admin/data/master.csv";
private KamsapList _master;
	private static final String PKEY_PREFIX="usr.DlgKamsap.";
	private File   fCurrentFile;
	private final KamsapFileTableModel fModel = new KamsapFileTableModel();
	private final SentenceBuilder fSentenceBuilder
		= new SentenceBuilder(new SBAction("Save", 'S',	"Save sentence and start a new one", null) {
		@Override public void action() { saveSentence(); }
		});
	private JLabel lblFileName = new JLabel("No file selected");
	private final DefaultListModel fSentences = new DefaultListModel();

	public DlgKamsap()
		{
		super((java.awt.Frame)null, false);
		JTable table = new KamsapMouseTable(fModel) {
			@Override protected void doubleClicked(Kamsap aKamsap, byte aField) {
				fSentenceBuilder.append(aKamsap);
				}
			@Override protected void rightClicked(Kamsap aKamsap, byte aField) {
//				fSentenceBuilder.append("rightClicked " + aKamsap + '\n');
				}
			};
		setLayout(new BorderLayout());
		final JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Page Kamsap", null, new SplitPane( SplitPane.VSPLIT,
			LAF.titled("Words on Page", new JScrollPane(table)),
			LAF.titled("Sentence Builder", fSentenceBuilder),
			PKEY_PREFIX+"split", 150 ));

		// load master, stick it into a table on it's own tab
String filespec = masterFilespec;
		_master = new KamsapList();
		try
			{
			SBLog.write ( "Loading master from " + filespec );
			TextFile.thaw( Kamsap.class, filespec, _master, false );
			}
		catch (Exception e) {SBDialog.error("Read: " + filespec, " Error: " + e);}
		KamsapTableModel masterModel = new KamsapTableModel();
		table = new KamsapMouseTable(masterModel) {
			@Override protected void doubleClicked(Kamsap aKamsap, byte aField) {
				fModel.add(aKamsap);
				}
			};
		masterModel.reset(_master);
		tabs.addTab("Master", null, new JScrollPane(table));

		// create the list of sentences on it's own tab
		JList list = new JList(fSentences);
		tabs.addTab("Sentences", null, new JScrollPane(list));

		add(tabs, BorderLayout.CENTER);
		add(buttonPanel(), BorderLayout.SOUTH);
		LAF.addUISwitchListener(this);
		setBounds( SBProperties.getInstance()
		                       .getRectangle( PKEY_PREFIX+"bounds", 50,50,500,400));
		addComponentListener( new ComponentAdapter()
			{
			public void componentMoved(ComponentEvent e) { saveBounds(); }
			public void componentResized(ComponentEvent e) { saveBounds(); }
			});
		}

	private void saveBounds()
		{
		SBProperties.getInstance().setProperty(PKEY_PREFIX+"bounds", getBounds());
		}

	public final void toggle() { setVisible(!isVisible()); }

	void show(File aContentFile)
		{
		reset(aContentFile);
		setVisible(true);
		}

	void reset(File aContentFile)
		{
System.out.println("DlgKamsap.reset(" + aContentFile + ")");
// SAVE fCurrentFile
		if (fCurrentFile != null)
			fModel.save();
		fCurrentFile = aContentFile;
		if (fCurrentFile != null)
			{
			lblFileName.setText(fCurrentFile.toString());
			setTitle (fCurrentFile.toString());
			}
		fModel.read();
		}

	private void saveSentence() {
		Brayot theSentence = fSentenceBuilder.brayot();
		fSentenceBuilder.clear();
		fSentences.addElement(theSentence);
		}

	private JPanel buttonPanel()
		{
		JPanel p = new JPanel();
		p.add(lblFileName);
		p.add(new ToggleOnTop());
		return p;
		}

	public String toString() { return getClass().getSimpleName(); }
	}

class KamsapMouseTable
	extends KamsapTable
	{
	public KamsapMouseTable(KamsapTableModel model) {
		super(model);
		addMouseListener(new MouseAdapter() // to handle double clicks
			{
//			public void mouseClicked(MouseEvent e)
//			public void mousePressed (MouseEvent e)
			public void mouseReleased (MouseEvent e)
				{
				java.awt.Point p = e.getPoint();
				byte field = (byte)convertColumnIndexToModel(columnAtPoint(p));
				Kamsap kamsap = ((KamsapTableModel)getModel()).getRow(rowAtPoint(p));

				if (SwingUtilities.isRightMouseButton( e ))
					rightClicked(kamsap, field);
				else if ( e.getClickCount() > 1 )
					doubleClicked(kamsap, field);
				else
					clicked(kamsap, field);
				}
			});
		}

	protected void clicked(Kamsap aKamsap, byte aField) {}
	protected void doubleClicked(Kamsap aKamsap, byte aField) {}
	protected void rightClicked(Kamsap aKamsap, byte aField) {}
	}

class SentenceBuilder
	extends JPanel
	{
	private Brayot fBrayot = new Brayot();
	private final JTextArea fTextArea = new JTextArea(25,10);
	private final SBAction fActClear = new SBAction("Clear", 'C',
		"Discard contents", null) {
		@Override public void action() { clear(); }
		};
	private final SBAction fActSave;

	SentenceBuilder(SBAction aActSave) {
		super(new BorderLayout());
		add(new JScrollPane(fTextArea), BorderLayout.CENTER);
		JPanel buttons = new JPanel(new GridLayout(0, 1, 5, 5));
		buttons.add(fActClear.makeButton());
		fActSave = aActSave;
		buttons.add(fActSave.makeButton());
		add(buttons, BorderLayout.EAST);
		setButtonsEnabled(false);
		}

	private void setButtonsEnabled(boolean on) {
		fActClear.setEnabled(on);
		fActSave.setEnabled(on);
		}

	public void clear() {
		fBrayot.clear();
		fTextArea.setText("");
		setButtonsEnabled(false);
		}

	public void append(Kamsap aKamsap) {
		fBrayot.add(aKamsap);
		fTextArea.setText(fBrayot.thai());
		fTextArea.append("\n" + fBrayot.phonetic() + '\n');
		setButtonsEnabled(true);
		}

	public Brayot brayot() { return fBrayot.copy(); }
	}

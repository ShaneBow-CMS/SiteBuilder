package com.shanebow.web.SiteBuilder.layout;
/********************************************************************
* @(#)DivTreeView.java 1.00 20121226
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* DivTreeView: Component for displaying the <div> hierarchy of an
* html page.
*
* @author Rick Salamone
* 20121226 rts created
*******************************************************/
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import java.io.File;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.awt.datatransfer.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;

public final class DivTreeView
	extends JPanel
	{
	private static final int LEFT_PANEL_WIDTH=200;
	private static final String HTML_NAME = "sitemap.content";
	private static final String XML_NAME = "sitemap.xml";

	private final PageTable fAllPagesTable = new PageTable("All Pages");
	private final PageTable fUnassignedTable = new PageTable("Unassigned Pages");
	private final JTree tree;
	private final SBAction fActSave = new SBAction("Open", 'O', "Open new file", null)
		{
		@Override public void actionPerformed(ActionEvent e) { save(); }
		};

	public final SBAction fActRobots = new SBAction("Robots.txt", 'R', "Edit Robots.txt", null)
		{
		@Override public void actionPerformed(ActionEvent e) { editRobots(); }
		};

	public DivTreeView()
		{
		super(new BorderLayout());

		tree = createTree();
		tree.setRootVisible(true);
		tree.getSelectionModel().setSelectionMode
			(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tree.addTreeSelectionListener(new TreeSelectionListener()
			{
			public void valueChanged(TreeSelectionEvent e)
				{
				// Returns the last path element of the selection.
				// Following line only works for single selection model
				DivNode node = (DivNode)tree.getLastSelectedPathComponent();
				if (node == null) // Nothing is selected
					return;

				Page page = node.page();
System.out.println(e.getSource().toString() + "> "
 + page + " selected: " + node.getChildCount() + " children");
// + (node.isLeaf()? "is" : "is not") + " leaf");
				}
			});
		tree.setTransferHandler(new PageTreeTransferHandler());
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.setDragEnabled(true);

		//LEFT COLUMN
		JPanel leftPanel = createVerticalBoxPanel();
		JScrollPane treeView = createScroller(tree);
		treeView.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, 1000)); // w, h
		leftPanel.add(treeView);

		JPanel btns = new JPanel(new GridLayout(1,0,5,0));
		btns.add(fActRobots.makeButton());
		btns.add(fActSave.makeButton());
		leftPanel.add(btns);

		add(leftPanel, BorderLayout.CENTER);
		setBorder(LAF.getStandardBorder());
		}

	final public void save()
		{
		System.out.println("Read another file");
		}

	protected JPanel createVerticalBoxPanel()
		{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.setBorder(LAF.getStandardBorder());
		return p;
		}

	private final void busy(boolean aBusy)
		{
		setCursor( Cursor.getPredefinedCursor( aBusy? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR ));
		}

	private String sourceDir()
		{
		return com.shanebow.util.SBProperties.get(
			com.shanebow.web.SiteBuilder.build.BuildCriteria.PKEY_DIR_SRC);
		}

	private JTree createTree()
		{
		JTree tree = new JTree(new DefaultTreeModel(loadMaps()))
			{
			@Override public String toString() { return "JTree." + getName(); }
			};
		tree.setName("<div> Hierarchy");
		tree.expandRow(1);
		return tree;
		}

	DivNode loadMaps()
		{
		DivStructure reader = new DivStructure();

		try { root = reader.readTree( new File(sourceDir(), HTML_NAME )); }
		catch (Exception x)
			{
//			SBDialog.error("DivTreeView.html Error", x.toString(), this);
			if (root == null) root = new DivNode(SitePages.getByHref("/", "Welcome"), null);
			}
		java.util.List<Page> treePages = new ArrayList<Page>();
		Enumeration e = root.preorderEnumeration();
		while (e.hasMoreElements())
			treePages.add(((DivNode)e.nextElement()).page());
System.out.println(getClass().getName() + " TREE ROOT: " + root);
		return root;
		}

	public boolean exitOK()
		{
		return true;
		}

	public boolean reload()
		{
		((DefaultTreeModel)tree.getModel()).setRoot(loadMaps());
		tree.expandRow(0);
		return true;
		}

	private void setScrollerTitle(JComponent comp, String title)
		{
		JScrollPane scroller = (JScrollPane)comp.getParent().getParent();
		scroller.setBorder(BorderFactory.createTitledBorder(title));
		}

	private JScrollPane createScroller(JComponent comp)
		{
		JScrollPane scroller = new JScrollPane(comp);
		String title = comp.getName();
		if (title != null)
			scroller.setBorder(BorderFactory.createTitledBorder(title));
		return scroller;
		}
	}

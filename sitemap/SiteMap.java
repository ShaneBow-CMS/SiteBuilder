package com.shanebow.web.SiteBuilder.sitemap;
/********************************************************************
* @(#)SiteMap.java 1.00 20111026
* Copyright © 2010-2016 by Richard T. Salamone, Jr. All rights reserved.
*
* SiteMap: Component for managing the site's sitemap(s) that consists of:
* 1) A tree view of the sitemap.html
* 2) a table showing all of the site pages
* 3) a table showing only pages that do not appear in the tree
*    (i.e. unassigned pages)
* The interface is primarily driven by DnD technology.
*
* @author Rick Salamone
* 20101107 rts first wrote the apo assign work version
* 20111026 rts significant revisions for the SiteBuilder project
* 20111206 rts implements Reloadable to support multiple sites
* 20160401 rts uses build criteria for source root; now handles public_html case
*******************************************************/
import com.shanebow.ui.LAF;
import com.shanebow.ui.Reloadable;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import com.shanebow.web.SiteBuilder.build.BuildCriteria;
import com.shanebow.web.SiteBuilder.pages.*;
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

public final class SiteMap
	extends JPanel
	implements Reloadable
	{
	private static final int LEFT_PANEL_WIDTH=200;
	private static final String HTML_NAME = "sitemap.content";
	private static final String XML_NAME = "sitemap.xml";

	private final PageTable fAllPagesTable = new PageTable("All Pages");
	private final PageTable fUnassignedTable = new PageTable("Unassigned Pages");
	private final JTree tree;
	private final SBAction fActSave = new SBAction("Save", 'S', "Write it out", null)
		{
		@Override public void actionPerformed(ActionEvent e) { save(); }
		};

	public final SBAction fActRobots = new SBAction("Robots.txt", 'R', "Edit Robots.txt", null)
		{
		@Override public void actionPerformed(ActionEvent e) { editRobots(); }
		};

	public SiteMap()
		{
		super(new BorderLayout());

		tree = createTree();
		tree.setRootVisible(true);
		tree.getSelectionModel().setSelectionMode
			(TreeSelectionModel.SINGLE_TREE_SELECTION);
//		fAllPagesTable.setTransferHandler(new PageTableTransferHandler(TransferHandler.COPY));
		fUnassignedTable.setTransferHandler(new PageTableTransferHandler(TransferHandler.MOVE));
		fUnassignedTable.setDragEnabled(true);
		fAllPagesTable.setDragEnabled(false);
		SitePages.addActionListener(new ActionListener()
			{
			@Override public void actionPerformed(ActionEvent e)
				{
				Page page = (Page)e.getSource();
				if ( e.getActionCommand().equals(SitePages.ADDED))
					{
					fUnassignedTable.add(page);
					fAllPagesTable.add(page);
					}
				}
			});

		tree.addTreeSelectionListener(new TreeSelectionListener()
			{
			public void valueChanged(TreeSelectionEvent e)
				{
				// Returns the last path element of the selection.
				// Following line only works for single selection model
				PageNode node = (PageNode)tree.getLastSelectedPathComponent();
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

		//RIGHT COLUMN
		JSplitPane rightPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT );
		rightPanel.setContinuousLayout(true);
		rightPanel.setDividerLocation(300); // -1: honor upper left comp preferred size
		rightPanel.setTopComponent(createScroller(fUnassignedTable));
		rightPanel.setBottomComponent(createScroller(fAllPagesTable));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              leftPanel, rightPanel);
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(LEFT_PANEL_WIDTH);

		add(splitPane, BorderLayout.CENTER);
		setBorder(LAF.getStandardBorder());
		}

	final public void save()
		{
		try
			{
			MapIO writer = new MapIO();
			PageNode root = (PageNode)tree.getModel().getRoot();
			writer.write( root, new File(BuildCriteria.srcPublicDir(), HTML_NAME));
			writer.writeXml( new File(BuildCriteria.srcPublicDir(), XML_NAME));
			}
		catch (Exception x) { SBDialog.error("Sitemap Save Error", x.toString(), this); }
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

	private void editRobots()
		{
		File robots = new File( BuildCriteria.srcPublicDir(), "robots.txt");
		new com.shanebow.web.SiteBuilder.editor.PlainTextEditor().edit(robots, this);
		}

	private JTree createTree()
		{
		JTree tree = new JTree(new DefaultTreeModel(new PageNode()))
			{
			@Override public String toString() { return "JTree." + getName(); }
			};
		tree.setName("Site Map");
		tree.expandRow(1);
		return tree;
		}

	PageNode loadMaps()
		{
		fAllPagesTable.walkSite();
		PageNode root = null;
		MapIO reader = new MapIO();
File xmlFile = new File(BuildCriteria.srcPublicDir(), XML_NAME );
System.out.println("SiteMap load: " + xmlFile);
		try { reader.readXml( xmlFile ); }
		catch (Exception x) {
			SBDialog.error("SiteMap.xml Error",
			         "<html>Problem loading: <b>" + xmlFile + "</b><br>" + x.getMessage(), this);
			}

		try { root = reader.readTree( new File(BuildCriteria.srcPublicDir(), HTML_NAME )); }
		catch (Exception x)
			{
//			SBDialog.error("SiteMap.html Error", x.toString(), this);
			if (root == null) root = new PageNode(SitePages.getByHref("/", "Welcome"), null);
			}
		java.util.List<Page> treePages = new ArrayList<Page>();
		Enumeration e = root.preorderEnumeration();
		while (e.hasMoreElements())
			treePages.add(((PageNode)e.nextElement()).page());
		fUnassignedTable.reset(SitePages.missingFrom(treePages));
System.out.println("SiteMap TREE ROOT: " + root);
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

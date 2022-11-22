package com.shanebow.web.SiteBuilder.pages;
/********************************************************************
* @(#)PageTable.java 1.00 20111024
* Copyright 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* PageTable: Displays Web Site page information with DND support
* for building site maps. 
*
* @author Rick Salamone
* @version 1.00
* 20111024 rts created
* 20111024 rts walkSite() now forces a SitePages.refresh()
*******************************************************/
import com.shanebow.web.SiteBuilder.editor.SEOEditor;
import java.util.*;
import java.awt.Rectangle;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class PageTable
	extends JTable
	{
	static SEOEditor _seoEditor; // lazily constructed when 1st needed

	public PageTable(String aName)
		{
		super(new PageTableModel());
		setName(aName);
		javax.swing.table.JTableHeader header = getTableHeader();
		header.setUpdateTableInRealTime(true);
		header.setReorderingAllowed(true);
		header.addMouseListener(new MouseAdapter() // to handle sorts
			{
			public void mouseClicked(MouseEvent e)
				{
				javax.swing.table.TableColumnModel colModel = getColumnModel();
				int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
				int sortColumn = colModel.getColumn(columnModelIndex).getModelIndex();
				int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK;
				boolean ascending = (shiftPressed == 0);
				((PageTableModel)getModel()).sort(sortColumn, ascending);
				}
			});
		addMouseListener(new MouseAdapter() // to handle double clicks
			{
			public void mouseClicked(MouseEvent e)
				{
				if ( e.getClickCount() == 1 ) return;
				java.awt.Point p = e.getPoint();
				JTable table = (JTable)e.getSource();
				int col = table.convertColumnIndexToModel(table.columnAtPoint(p));
				int row = table.rowAtPoint(p);
				doubleClick(row, col);
				}
			});

		setFillsViewportHeight( true ); // req'd for drop on empty table
		setDropMode(DropMode.INSERT_ROWS);
//		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		getColumnModel().getColumn(Page.CHANGE_FREQ).setCellEditor(
			new DefaultCellEditor(new JComboBox(Page.CHANGE_FREQS)));

		getColumnModel().getColumn(Page.PRIORITY).setCellEditor(
			new DefaultCellEditor(new JComboBox(Page.PRIORITIES)));

		setDefaultRenderer(java.util.Date.class, new DefaultTableCellRenderer()
			{
			@Override public java.awt.Component getTableCellRendererComponent(JTable t,
				 Object value, boolean isSelected, boolean hasFocus, int r, int c)
				{
				JLabel it = (JLabel)
					super.getTableCellRendererComponent(t, value, isSelected, hasFocus, r, c);
				long time = ((java.util.Date)value).getTime() / 1000;
				it.setText( com.shanebow.util.SBDate.friendly(time));
				return it;
				}
			});
		}

	@Override public String toString() { return "PageTable: " + getName(); }

	final public void walkSite()
		{
		SitePages.refresh();
		((PageTableModel)getModel()).reset(SitePages.list());
		}

	final public void reset( List<Page> aPageList )
		{
		((PageTableModel)getModel()).reset(aPageList);
		}

	final public void remove( Page[] pages )
		{
		PageTableModel model = (PageTableModel)getModel();
		for ( Page page : pages )
			model.remove(page);
		}

	final public void add( Page aPage )
		{
		PageTableModel model = (PageTableModel)getModel();
		model.add(aPage);
		}

	private void doubleClick( int row, int col )
		{
		if ( col != Page.LMOD_SEO ) return;
		Page page = ((PageTableModel)getModel()).get(row);
		if ( _seoEditor == null )
			_seoEditor = new SEOEditor();
		_seoEditor.edit(page.sourceFile(".seo"), SwingUtilities.getRoot(this));
		}
	}

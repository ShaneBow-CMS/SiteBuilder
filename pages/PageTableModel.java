package com.shanebow.web.SiteBuilder.pages;
/********************************************************************
* @(#)PageTableModel.java 1.00 20111024
* Copyright (c) 2011-2017 by Richard T. Salamone, Jr. All rights reserved.
*
* PageTableModel: Table model for Thai vocabulary lists.
*
* @version 1.00
* @author Rick Salamone
* 20111024 rts created
* 20111101 rts added date comparison
*******************************************************/
import java.awt.event.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public final class PageTableModel
	extends AbstractTableModel
	{
	private List<Page> fList = null;
	private byte       fSortColumn = -1;
	public  int fColumnCount = Page.FIELD_NAMES.length;

	public PageTableModel()
		{
		SitePages.addActionListener(new ActionListener()
			{
			@Override public void actionPerformed(ActionEvent e)
				{
				if ( e.getActionCommand().equals(SitePages.ADDED))
					return;
				int row = indexOf((Page)e.getSource());
				if ( row >= 0 ) // in list
					fireTableRowsUpdated(row, row);
				}
			});
		}

	public boolean isCellEditable(int row, int col)
		{
		return col==Page.IN_XML_MAP || col==Page.CHANGE_FREQ || col==Page.PRIORITY;
		}

	public void setValueAt(Object value, int row, int col)
		{
		Page page = fList.get(row);
		if ( col == Page.IN_XML_MAP )
			page.setInXmlMap(((Boolean)value).booleanValue());
		else if ( col == Page.CHANGE_FREQ )
			page.setChangeFreq((String)value);
		else if ( col == Page.PRIORITY )
			page.setPriority((String)value);
		else return;
		SitePages.inform(page, SitePages.PROPERTIES); // eventually fires table event
		}

	public void reset( List<Page> aList )
		{
		fList = aList;
		fireTableDataChanged();
		}

	public void add( int row, Page aPage )
		{
		fList.add(row, aPage);
		fireTableRowsInserted(row, row);
		}

	public void add( Page aPage )
		{
		add ( fList.size(), aPage );
		}

	public Page get(int r) { return fList.get(r); }
	public int indexOf(Page aPage) { return (fList == null)? -1 : fList.indexOf(aPage); }

	public void remove( Page aPage ) // just remove from this list
		{
		int index = indexOf(aPage);
		if ( index >= 0 ) // in list
			removeRow(index);
		}

	public Page removeRow( int row ) // just remove from this list
		{
		Page page = fList.remove(row);
		fireTableRowsDeleted(row,row);
		return page;
		}

	public int    getColumnCount() { return fColumnCount; }
	public Class  getColumnClass(int c) { return Page.FIELD_TYPES[c]; }
	public int    getRowCount() { return (fList == null)? 0 : fList.size(); }
	public String getColumnName(int c) { return Page.FIELD_NAMES[c]; }
	public Object getValueAt(int r, int c)
		{
		return fList.get(r).get(c);
		}

	public void sort(int aSortColumn, final boolean aIsAscending )
		{
		fSortColumn = (byte)aSortColumn;
		fAscending = aIsAscending? 1 : -1;
		Comparator<Page> comparator
			= (fSortColumn == Page.PATH)? fPathCompare
			: (getColumnClass(fSortColumn) == Date.class)? fDateCompare
			: fStrCompare;
		Collections.sort(fList, comparator );
		fireTableDataChanged();
		}

	private int fAscending;
	private final Comparator<Page> fPathCompare = new Comparator<Page>()
		{
		public int compare(Page r1, Page r2)
			{
			return fAscending * r1.compareTo(r2);
			}
		};

	private final Comparator<Page> fDateCompare = new Comparator<Page>()
		{
		public int compare(Page r1, Page r2)
			{
			return fAscending
			  * ((Date)r1.get(fSortColumn)).compareTo((Date)r2.get(fSortColumn));
			}
		};

	private final Comparator<Page> fStrCompare = new Comparator<Page>()
		{
		public int compare(Page r1, Page r2)
			{
			return fAscending
			  * (r1.getString(fSortColumn)).compareTo(r2.getString(fSortColumn));
			}
		};
	}

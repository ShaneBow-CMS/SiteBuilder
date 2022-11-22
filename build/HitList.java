package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)HitList.java 1.00 20101226
* Copyright © 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* HitList: Lists the file in the specified directory.
*
* @author Rick Salamone
* @version 1.00 20101226, rts created
* @version 1.01 20101230, rts sorts files by extention and name
* @version 1.02 20101231, rts added drag source functionality
*******************************************************/
import java.awt.*;
import java.util.Vector;
import javax.swing.*;

public class HitList
	extends JPanel
	{
	private final JList fList;

	public HitList()
		{
		super(new BorderLayout());
		fList = new JList( new FileListModel());
		fList.setCellRenderer( new HitListCellRenderer());
		add( new JScrollPane(fList), BorderLayout.CENTER );
		}

	public void add(BuildEvent file )
		{
		((FileListModel)(fList.getModel())).add(file);
		}

	@Override public Dimension getPreferredSize() {return new Dimension(475,220);}

	private class FileListModel extends AbstractListModel
		{
		private final Vector<BuildEvent> fContent = new Vector<BuildEvent>();

		public Object getElementAt(int index) { return fContent.get(index); }
		public int getSize() { return fContent.size(); }
		private void removeAll()
			{
			int size = fContent.size();
			if ( size > 0 )
				{
				fContent.clear();
				fireIntervalRemoved(this, 0, size - 1);
				}
			}
		void add( BuildEvent file )
			{
			int size = fContent.size();
			fContent.add(file);
			fireIntervalAdded(this, size, size);
			}
		}
	}

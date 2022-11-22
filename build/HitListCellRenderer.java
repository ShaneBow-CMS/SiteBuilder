package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)HitListCellRenderer.java 1.00 20101227
* Copyright © 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* HitListCellRenderer: Displays the appropriate icon and the name
* sans directory for a File in a JList.
*
* @author Rick Salamone
* @version 1.00, 20101227
*******************************************************/
import java.awt.Color;
import java.awt.Component;
import com.arashpayan.filetree.Constants;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class HitListCellRenderer
	extends JLabel
	implements ListCellRenderer
	{
	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called
 	public Component getListCellRendererComponent(
		JList list,              // the list
		Object value,            // value to display
		int index,               // cell index
		boolean isSelected,      // is the cell selected
		boolean cellHasFocus)    // does the cell have focus
		{
		BuildEvent event = (BuildEvent)value;
		setFont(list.getFont());
		setOpaque(true);
		setText(event.formatted());
		try { setIcon(Constants.getIcon(event.getFile())); }
		catch (Exception e) {}

		BuildEvent.Action action = event.getAction();
		Color fg = (action == BuildEvent.Action.SKIPPED) ? Color.LIGHT_GRAY
		         : (action == BuildEvent.Action.BUILT)   ? Color.BLUE
		         : (action == BuildEvent.Action.COPIED)  ? Color.GREEN
		         : (action == BuildEvent.Action.FAILED)  ? Color.RED
		         :                                         Color.ORANGE;
		setForeground(fg);
		setBackground(isSelected ? list.getSelectionBackground()
		                         : list.getBackground());
//		setEnabled(list.isEnabled());
		return this;
		}
	}

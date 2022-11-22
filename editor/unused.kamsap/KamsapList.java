package com.shanebow.web.SiteBuilder.editor;
/********************************************************************
* @(#)KamsapList.java 1.00 20140918
* Copyright © 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* KamsapList: Extends Vector<Kamsap> with routines to find a specific
* kamsap id and to ensure that there are no duplicates in the list.
*
* @author Rick Salamone
* @version 1.00
* 20140918 rts created
*******************************************************/
import com.thaidrills.lib.*;
import java.util.Vector;

public class KamsapList
	extends Vector<Kamsap>
	{
	/**
	* find - does a sequential search for the Kamsap with
	* the specified id
	*
	* @param int the id of the desired Kamsap entry
	* @return Kamsap the Kamsap with the specified id or null if not found
	*/
	public Kamsap find(int tid) {
		for (Kamsap k : this)
			if (k.id() == tid)
				return k;
		return null;
		}

	public boolean addNoDup(Kamsap aKamsap)
		{
		if (find(aKamsap.id()) != null)
			return false;
		return add(aKamsap);
		}
	}
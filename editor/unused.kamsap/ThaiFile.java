package com.shanebow.web.SiteBuilder.editor;
/********************************************************************
* @(#)ThaiFile.java 1.00 20140918
* Copyright © 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* ThaiFile: Static methods for saving and retrieving collections stored
* in a csv file where each line is sent to the contructor for one
* object in the collection.
*
* @author Rick Salamone
* @version 1.00
* 20140918 rts created
*******************************************************/
import com.shanebow.util.SBMisc;
import com.thaidrills.lib.*;
import java.io.*;
import java.util.List;
import java.util.Vector;

public class ThaiFile
	{
	KamsapList words;
	List<Brayot> sentences;

	public void freeze(File file)
		{
		PrintWriter pw = null;

		try {
			pw = SBMisc.utfPrintWriter(file);
			String line = "";
			for (Kamsap k : words) line += "," + k.id();
			pw.println(line.substring(1));
			for (Brayot b : sentences)
				pw.println(b.csv());
			}
		catch (Exception e)
			{
			e.printStackTrace();
			System.err.println(file.toString() + " Error: " + e.toString());
			}
		finally
			{
			try { if (pw!=null) pw.close(); }
			catch (Exception ignore) {}
			}
		}

	public boolean thaw(KamsapList master, final File file)
		{
		System.out.println ( "ThaiFile.thaw(" + file + ")" );
		BufferedReader stream = null;
		try
			{
			stream = new BufferedReader(new FileReader(file)); 
			if (stream == null)
				{
				System.err.println ( "File open error: " + file );
				return false;
				}

			// First line is a csv of all tid's referenced in the file
			String csv = stream.readLine();
			String[] pieces = csv.split(",");
			for (String piece : pieces)
				words.add(master.find(Integer.parseInt(piece,10)));

			// Remaining lines are csv's representing sentences
			// made up of the words from the first line
			while ((csv = stream.readLine()) != null )
				{
				csv = csv.trim();
				if ( csv.isEmpty())
					continue;
				sentences.add(new Brayot(words, csv));
				}
			}
		catch (Exception e)
			{
			System.err.println(file.toString() + " Error: " + e.toString());
			return false;
			}
		finally { try { stream.close(); } catch (Exception ignore) {}}
		return true;
		}
	}

/**
* Brayot is a thai sentence represented as a Vector<Kamsap>
* and persisted as a csv of the id's of the entries
*
* NOTE: cannot use KamsapList because:
*  a) order is important and
*  b) there may be duplicates!
*/
class Brayot
	extends Vector<Kamsap>
	{
	Brayot() { super(); }

	Brayot(KamsapList master, String csv) {
		super();
		String[] pieces = csv.split(",");
		for (String piece : pieces)
			add(master.find(Integer.parseInt(piece,10)));
		}

	public Brayot copy() {
		Brayot it = new Brayot();
		it.addAll(this);
		return it;
		}

	public String csv() {
		String it = "";
		for (Kamsap k : this) it += "," + k.id();
		return it.substring(1);
		}

	public String thai() {
		String it = "";
		for (Kamsap k : this) it += " " + k.getThai();
		return it.substring(1);
		}

	public String phonetic() {
		String it = "";
		for (Kamsap k : this) it += " " + k.getPhonetic();
		return it.substring(1);
		}

	@Override public String toString() {
		return thai() + " - " + phonetic();
		}
	}

package com.shanebow.web.SiteBuilder.build;
/********************************************************************
* @(#)BuildEvent.java 1.00 20110105
* Copyright © 2011-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* BuildEvent: A semantic event which indicates that a match was found
* in a particular file during the grep operation.
*
* @author Rick Salamone
* @version 1.00
* 20110711 rts created
* 20111018 rts added Actions
* 20120705 rts added ctor that takes file and exception
* 20130116 rts ctor takes Throwable rather than Exception
* 20131018 rts added INFO action & ctor
* 20151022 rts added failed method that specifies the caller
*******************************************************/
import java.io.File;

public class BuildEvent
	{
	static int _RootPathLen;
	/**
	* The types of link that can be encountered.
	*/
	public enum Action { BUILT, SKIPPED, COPIED, FAILED, INFO }
	public BuildEvent built() { return setAction(Action.BUILT); }
	public BuildEvent copied() { return setAction(Action.COPIED); }
	public BuildEvent skipped() { return setAction(Action.SKIPPED); }
	public BuildEvent failed(String aReason)
		{ fMsg = aReason; return setAction(Action.FAILED); }
	public BuildEvent failed(String caller, Exception e)
		{
		// if (firstTime) {e.printStackTrace();firstTime = false;}
		return failed(caller + " " + e.getMessage());
		}

	private final File   fFile;
	private Action fAction;
	private String fMsg;

	public BuildEvent( File aFile, Throwable e )
		{
		fFile = aFile;
		failed(e.getMessage());
System.out.println("BE Throw on file: " + aFile + " " + e);
e.printStackTrace();
		}

	public BuildEvent( File aFile, String aMsg )
		{
		fFile = aFile;
		fAction = Action.INFO;
		fMsg = aMsg;
		}

	public BuildEvent( File aFile )
		{
		fFile = aFile;
		}

	public String formatted()
		{
		String it = "<html><b>" + fFile.getPath().substring(_RootPathLen).replace('\\', '/');
		if ( fMsg != null )
			it += ":</b> <font color=RED>" + fMsg;
		return it;
		}

	private BuildEvent setAction( Action aAction ) {fAction = aAction; return this; }
	public Action getAction() { return fAction; }

	public final File getFile() { return fFile; }
	public final String getMessage() { return fMsg; }
	public final void addMessage(String aMessage)
		{
		if (fMsg == null) fMsg = aMessage;
		else fMsg += "; " + aMessage;
		}

	public String toString()
		{
		return getFile().toString() + ": " + fAction;
		}
	}

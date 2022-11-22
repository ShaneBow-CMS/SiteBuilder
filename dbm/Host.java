package com.shanebow.web.SiteBuilder.dbm;
/********************************************************************
* @(#)Host.java 1.00 20170107
* Copyright © 2017 by Richard T. Salamone, Jr. All rights reserved.
*
* Host: Extends the general ShaneBow Post class to provide calls
* to the host of the site being edited using its configured app dir. 
* specific calls 
*
* @author Rick Salamone
* @version 1.00
* 20170107 rts created
*******************************************************/
import com.shanebow.web.host.*;
import com.shanebow.web.SiteBuilder.build.BuildCriteria;
import com.shanebow.util.SBProperties;
import java.awt.event.*;

public final class Host
	{
	public String _app_dir;
	private final Post fPoster = new Post();
	public final DomainRadio fDomainRadio;

	public Host() {
		fDomainRadio = new DomainRadio();
		reload();
		fDomainRadio.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {reload();}
			});
		}

	public boolean reload()
		{
		String selected = fDomainRadio.getSelected();
		String domain = SBProperties.get(BuildCriteria.PKEY_DOMAIN);
		if (selected.equals("local")) {
			int dotAt = domain.lastIndexOf('.');
			domain = domain.substring(0, ++dotAt) + selected;
			}
else if (domain.toLowerCase().startsWith("clearedto43")) domain = "alasnome.com";
		Post._domain = "http://" + domain + "/";

		_app_dir = SBProperties.get(BuildCriteria.PKEY_DIR_APP);
		if (_app_dir.startsWith("/")) _app_dir = _app_dir.substring(1);
		return true;
		}

	public String appPath(String... pieces) {
		StringBuilder it = new StringBuilder(_app_dir);
		for (String piece : pieces)
			it.append('/').append(piece);
		return it.toString();
		}

	public PostResponse post(String aFilename, String aUTF8Data) {
		return fPoster.send(aFilename, aUTF8Data);
		}

	public void postView(String title, String aPath, String aUTF8Data) {
		PostResponse resp = fPoster.send(aPath, aUTF8Data);
		new DlgPostResponse(title, aPath, aUTF8Data, resp);
		}

	@Override public final String toString() {
		return fPoster._domain + _app_dir;
		}
	}

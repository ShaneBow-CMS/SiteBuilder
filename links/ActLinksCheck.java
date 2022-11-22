package com.shanebow.web.SiteBuilder.links;
/********************************************************************
* @(#)ActLinksCheck.java	1.00 20110718
* Copyright © 2011-2015 by Richard T. Salamone, Jr. All rights reserved.
*
* ActLinksCheck: Launch the spider to check for broken links.
*
* @author Rick Salamone
* @version 2.00
* 20110718 rts created
* 20120711 rts search root now based on build criteria props (was hard coded)
* 20150211 rts added dialog ui and runs in a SwingWorker
*******************************************************/
import static com.shanebow.web.SiteBuilder.build.BuildCriteria.PKEY_DIR_DEST;
import static com.shanebow.web.SiteBuilder.build.BuildCriteria.PKEY_DOMAIN;
import com.shanebow.spider.*;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.layout.LabeledPairPanel;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBMisc;
import com.shanebow.util.SBProperties;
import java.awt.BorderLayout;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public final class ActLinksCheck
	extends SBAction
	{
	private static final String TITLE = "NN Links Check";

	public ActLinksCheck() {
		super( TITLE, 'L', "Check site for broken links", null);
		}

	@Override public void action() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() { new DlgLinksCheck(TITLE); }
			});
		}
	}

class DlgLinksCheck
	extends DlgTaskMonitor
	{
	private JButton btnBegin;
	private JTextField tfStartPage;
	private JTextField tfMaxDepth;
	private JTextArea  theLog;
	private final DlgTaskMonitor fDlg;
	private SwingWorker fWorker;
	private char fType;

	DlgLinksCheck(String aTitle) {
		super(aTitle);
		fDlg = this; // for inside swing worker
		}

	@Override protected void addButtons(JPanel p) {
		fbtnCancel.setVisible(false);
		btnBegin = new SBAction("Begin", 'B', "Start checking links", null) {
			public void action() {
				JPanel btns = (JPanel)btnBegin.getParent();
				btns.remove(btnBegin);
				fbtnCancel.setVisible(true);
				btns.validate();
				checkLinks();
				}
			}.makeButton();
		p.add(btnBegin);
		}

	@Override protected JComponent mainPanel() {
		JPanel it = new JPanel(new BorderLayout());
/***
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Log",      icon, log,  "Execution Trace" );
		tabbedPane.addTab("Failures", icon, tabFailures, "List of Failed URLs" );
***/
		SBProperties props = SBProperties.getInstance();
		String domain = props.getProperty(PKEY_DOMAIN).replace(".com", ".local");
		domain = domain.toLowerCase();
		File targetRoot = props.getFile(PKEY_DIR_DEST);
		boolean isHtml = new File(targetRoot, "index.html").exists();
		String search = "http://" + domain + "/index." + (isHtml? "html" : "php");

		LabeledPairPanel top = new LabeledPairPanel();
		top.addRow("StartPage", tfStartPage = new JTextField(search));
		top.addRow("Max Depth", tfMaxDepth = new JTextField("5"));
		it.add(top, BorderLayout.NORTH);

		theLog = new JTextArea(10,40);
		theLog.setEditable(false);
		it.add(new JScrollPane(theLog), BorderLayout.CENTER);
		return it;
		}

	protected void cancel() { fWorker.cancel(true); }

	private void checkLinks() {
		fWorker = new SwingWorker<Void, Object>() {
			/**
			* The current host, only accept URL's from this host.
			*/
			private String fHost;

			/**
			* The set of all the urls (*.html, *.js, *.css, and images) found on a page
			* The page's URL is the map's key
			*/
			private Map<URL, Set<URL>> fPageLinks = new HashMap<URL, Set<URL>>();

			/**
			* A list of all the problem urls enountered - the broken links summary performs
			* the intersection of this set with with set of links on a page for each page
			*/
			private Set<URL> fBadLinks = new HashSet<URL>();
				private int  fTotalWork, fWorkDone, fErrors, fUploads;

			@Override public Void doInBackground() {
				long startTime = SBDate.timeNow();
				String search = tfStartPage.getText();

		fTotalWork = 100;
		//		log("\nNeed to checkLinks: " + fTotalWork);
				log("Spider search: " + search);

				Spider itsy = null;
				SpiderOptions options  = new SpiderOptions();
				options.maxDepth = Integer.parseInt(tfMaxDepth.getText()); // -1 means unlimited depth

				SpiderHook report = new SpiderHook() {
					@Override public void initialized(Spider spider) {}
					@Override public boolean urlFound(URL url, URL source, URLType type)
						{
			//			if ( !type.equals(URLType.HYPERLINK))
			//				trace("urlFound", url, source, type);
						if ( fHost == null ) // source is null
							{
							fHost = url.getHost();
							return true;
							}
						boolean crawl = fHost.equalsIgnoreCase(url.getHost());
						Set<URL> linkedURLs = fPageLinks.get(source);
						if ( linkedURLs == null ) // this source isn't in the fPageLinks map
							{
							linkedURLs = new LinkedHashSet<URL>();
							fPageLinks.put(source, linkedURLs);
							}
						linkedURLs.add(url);
						return crawl;
						}
					@Override public void urlAdded (URL url, URL source ) {}
					@Override public void urlProcess(URL url, SpiderParseHTML parse)
						throws IOException
							{
//							trace("urlProcess", url, parse.getClass().getSimpleName());
							parse.readAll(); // discards all characters, but causes links to be found
							}
					// following called when processing non-HTML link
					@Override public void urlProcess(URL url, InputStream stream) throws IOException {}
					@Override public void urlError(URL url, String desc, ErrorLevel level )
						{
//						trace("urlError", url, desc, level );
						log("%s Error(%s): %s", url.toString(), level.toString(), desc);
						fBadLinks.add(url);
						}
					};
				try { itsy = new Spider( options, report ); }
				catch ( Exception ex )
					{
					log ( "new Spider(%s,%s) FAILED: %s", options.toString(),
				                     report.toString(), ex.toString());
					}

				itsy.addURL( search ); // starting url
				itsy.process(); // start spidering
				log ( itsy.getStatus()); // log the basic run stats

				// logSummary();
				logBrokenLinks();
		//		publish("\ncheckLinksed " + fUploads + " files (" + fWorkDone + " symbols) in "
		//		      + (SBDate.timeNow() - startTime)/60.0 + " minutes\n");
				return (Void)null;
				}

			public void logSummary()
				{
				log( "******* Links by Page ******");
				for ( URL source : fPageLinks.keySet() )
					{
					Set<URL> linkedURLs = fPageLinks.get(source);
					log( source.toString() + "(" + fPageLinks.size() + ")");
					for ( URL url : linkedURLs )
						log( "  >" + url.toString());
					}
				}

			private void logBrokenLinks()
				{
				int total = 0;
				log( "******* Broken Links by Page ******");
				for ( URL source : fPageLinks.keySet() )
					{
					Set<URL> linkedURLs = fPageLinks.get(source);
					Set<URL> intersection = new HashSet<URL>(linkedURLs);
					intersection.retainAll(fBadLinks);
					if ( !intersection.isEmpty())
						{
						int size = intersection.size();
						total += size;
						log( source.toString() + "(" + size + ")");
						for ( URL bad : intersection )
							log( "  >" + bad.toString());
						}
					}
				if ( total == 0 )
					log( "No broken links found!");
				else
					log( "" + total + " broken links");
				}

			@Override protected void process(List<Object> objs) {
				for ( Object obj : objs )
					if (obj instanceof String)
						theLog.append((String)obj);
					else theLog.append("Error: " + obj.toString());
				fDlg.setProgress(fWorkDone, fTotalWork);
				}

			@Override public void done() {
//				fDlg.promptClose(fErrs.size());
	fDlg.promptClose(0); // fErrs.size());
				fWorker = null;
				}

			private void log (String fmt, Object... args) {
				publish(String.format(fmt,args)+"\n");
				}
			};
		fWorker.execute();
		}
	}

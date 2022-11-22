package com.shanebow.web.SiteBuilder.build;
import java.io.*;

public class FileCopier
	{
	public static void copy(File f1, File f2, boolean append)
		throws FileNotFoundException, IOException
		{
		InputStream in = null;
		OutputStream out = null;
		try
			{
			byte[] buf = new byte[1024];
			in = new FileInputStream(f1);
//			out = new FileOutputStream(f2,append); //For Append the file
			out = new FileOutputStream(f2); //For Overwrite the file

			int len;
			while ((len = in.read(buf)) > 0)
				out.write(buf, 0, len);
			}
		finally
			{
			try { in.close(); } catch(Exception ignore) {}
			try { out.close(); } catch(Exception ignore) {}
			}
		}
	}
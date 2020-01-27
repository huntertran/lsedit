package dms;

import lsedit.LandscapeViewer;
import lsedit.SpecialPath;

/* This code extends LandscapeViewer so that shared-canvas can be displayed
 */
 
public class DmsViewer extends LandscapeViewer {

	public SpecialPath getSpecialPath()
	{
		return new DmsSpecialPath();
	}

	/*
		Returns information about this applet. An applet should override this method to return a String containing information 
		about the author, version, and copyright of the applet. 
	 */

	public String getAppletInfo()
	{
		return "dms\n" + super.getAppletInfo();
	}
}

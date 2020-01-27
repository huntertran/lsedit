package dms;

import lsedit.LandscapeEditorFrame;
import lsedit.SpecialPath;

/* This code extends LandscapeEditorFrame so that JDBC does not need to be made
 * part of an LSEDIT compile -- only a CMDB compile
 */
 
public class DmsEditorFrame extends LandscapeEditorFrame {
	
	public SpecialPath getSpecialPath()
	{
		return new DmsSpecialPath();
	}

	public static void main(String args[]) 
	{
		DmsEditorFrame af = new DmsEditorFrame();

		af.launch(args);
	}
}

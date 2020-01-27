package lsedit;

public class LsLink extends Object {

	public static final int TARGET_TOP	 = 0;		 // Whole browser window	"_top"
	public static final int TARGET_NEW	 = 1;		 // New browser window		new lsedit 
	public static final int TARGET_FRAME = 2;		 // Applet frame			"map"
	public static final int TARGET_LIST	 = 3;		 // Listing box				"list"
	public static final int TARGET_HELP  = 4;		 // Help box				"_blank"
	public static final int TARGET_APP	 = 5;		 // Menu command to app
	public static final int TARGET_ERR   = -1;

	protected static final String targets[] = 
		{	"TARGET_TOP", 
			"TARGET_NEW", 
			"TARGET_FRAME", 
			"TARGET_LIST",
			"TARGET_HELP", 
			"TARGET_APP"
		};

	public static int convertTarget(String tgtStr) 
	{
		for (int i=0; i<targets.length; i++) {
			if (tgtStr.equals(targets[i])) {
				return i;
			}
		}
		return -1;
	}
}


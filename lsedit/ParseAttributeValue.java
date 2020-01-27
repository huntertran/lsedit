package lsedit;

import java.io.StringReader;

/* Check that the attribute value given by the user parses and convert to a more canonical form */

public class ParseAttributeValue extends TA_StreamTokenizer {

	public ParseAttributeValue(String avi) 
	{
		super(new StringReader(avi), "Updated Attribute Value");
	}

	public String result()
	{
		String	avi;
		int		ttype;

		try {
			avi = nextAVI();
			if (avi != null) {
				ttype = nextToken();
				if (ttype != TT_EOF) {
					avi = null;
			}	}
		} catch (Exception e) {
			System.out.println("parseAttributeValue " + e.getMessage());
			avi = null;
		}
		return(avi);
	}
}


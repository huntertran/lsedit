package dms;

import java.util.Vector;

import lsedit.Ta;
import lsedit.Option;
import lsedit.ResultBox;
import lsedit.SpecialPath;

public class DmsSpecialPath implements SpecialPath {

	private DmsReader		m_dmsReader      = null;
	
	public String parseSpecialPath(Ta diagram, ResultBox resultBox, String path)
	{
		BasicReader basicReader = null;
		
		if (path.length() >= 4) {
			if (path.substring(0,4).equals("dms:")) {
				if (m_dmsReader == null) {
					m_dmsReader = new DmsReader();
				}
				basicReader = m_dmsReader;
			}
			if (basicReader != null) {
				return basicReader.parseSpecialPath(diagram, resultBox, path);
		}	}
		return ("Unknown path prefix " + path);
	}
	
	public boolean isSpecialPath(String path)
	{
		if (path.length() >= 4) {
			if (path.substring(0,4).equals("dms:")) {
				return true;
		}	}
		return false;
	}

	public void  specialPathOptions(Option option)
	{
		option.setTraceLifted(false);
		option.setHideEmpty(true);		
		option.setMemberCounts(true);
	}
}


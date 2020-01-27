package lsedit;

import java.awt.Container;
import java.awt.Color;
import java.awt.BorderLayout;

import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.net.URL;

/*
Subject: Solution to how one debugs a Java applet

c:\j2sdk1.4.2_04\bin\appletviewer.exe -debug index.htm

This brings the java applet up in the htm file, while bring the invoker of
this command up inside jdb.  One can then breakpoint the execution al-la
a normal java program.  Worth knowing... Ian
*/

public class LandscapeViewer extends JApplet {

	static String parameter_info[][] = 
	{
		{"test",		"string",	"test applet can display string"},
		{"lsfile",		"url",		"load this ta file"},
		{"init",		"url",		"load this init file"},
		{"layout",		"string",	"default Layout"},
		{"toolabout",	"url",		"URL linked to F11"},
		{"toolhelp",	"url",		"URL linked to F12"},
		{"startEntity",	"string",	"Entity to first show"},
		{"lsfile_bg0",  "url",		"URL of additional loadable TA file"},
		{"lsfile_bg<n>","url",	    "URL of additional loadable TA files"}
	};

	/* Implements the browser version of the editor */

	LandscapeEditorCore	m_ls;

	public SpecialPath getSpecialPath()
	{
		return null;
	}

	// Handle character escaping of the form %23 -> #
	
	public static String decodeURL(String value)
	{
		for (int i = 0; i < value.length(); ++i) {
			if (value.charAt(i) == '%' && i+2 < value.length()) {
				char c1 = value.charAt(i+1);
				if (c1 >= '0' && c1 <= '9') {
					c1 -= '0';
				} else if (c1 >= 'A' && c1 <= 'F') {
					c1 += 10 - 'A';
				} else if (c1 >= 'a' && c1 <= 'z') {
					c1 += 10 - 'a';
				} else {
					continue;
				}
				char c2 = value.charAt(i+2);
				if (c2 >= '0' && c2 <= '9') {
					c2 -= '0';
				} else if (c1 >= 'A' && c1 <= 'F') {
					c2 += 10 - 'A';
				} else if (c2 >= 'a' && c2 <= 'z') {
					c2 += 'A' - 'a';
				} else {
					continue;
				}
				c1 <<= 4;
				c1  += c2;
				value = value.substring(0,i) + c1 + value.substring(i+3);
		}	}
		return value;
	}
	
	// Overloads Applet.getParameter
	public String getParameter(String name)
	{
		URL			url         = getDocumentBase();
		String		queryString = url.getQuery();
		
		if (queryString != null) {
			String[]parameters  = queryString.split("&");
			int		lth = name.length(); 
			int		i, j;
			String	parameter;
			String	id;

			for (i = 0; i < parameters.length; ++i) {
				parameter = parameters[i];
				j         = parameter.indexOf('=');
				if (j >= 0) {
					id    = decodeURL(parameter.substring(0, j));
					if (id.equals(name)) {
						return decodeURL(parameter.substring(j + 1));
		}	}	}	}
		// Get the parameter from the applet parameters
		return super.getParameter(name);
	}
		
	private void createGUI()
	{
		String	msg = null;

		try {
			msg = getParameter("test");

			if (msg == null) {

				int			num;

				m_ls = new LandscapeEditorCore(this, getSpecialPath());

				m_ls.m_lsInit = getParameter("init");
				m_ls.m_lsPath = getParameter("lsfile");
				m_ls.defaultToLayouter(getParameter("layout"));
				m_ls.setForward(getParameter("forward"));

				setJMenuBar(m_ls.genMenu());

				// Obtain the arguments (passed as applet tag params) 


				for (num = 0;;++num) {
					String f = getParameter("lsfile_bg" + num);
					if (f == null || f.length() == 0) {
						break;
					}
//					System.out.println(f);
					m_ls.addLseditHistory(f);
				}
		
				m_ls.m_aboutURL    = getParameter("toolabout");
				m_ls.m_helpURL	   = getParameter("toolhelp");
				m_ls.setStartEntity(getParameter("startEntity"));

				m_ls.init_core(0, 0);
			}
		} catch (Exception e) {
			StackTraceElement[] stack = e.getStackTrace();
			int					i;

			msg = e.getMessage() + "\n\n";
			for (i = stack.length; i > 0; ) {
				msg += stack[--i].toString() + "\n";
			}
		}

		if (msg != null) {

			Container	contentPane;
			int			w, h;

			contentPane = getContentPane();
			contentPane.removeAll();
			w = contentPane.getWidth();
			h = contentPane.getHeight();

			JScrollPane scrollPane =  new JScrollPane();	// Set null to disable scrolling

			JTextArea textArea = new JTextArea(msg);
			textArea.setBackground(Color.pink);
			textArea.setToolTipText(msg);
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setSize(w, h);

			if (scrollPane != null) {
				scrollPane.setSize(w, h);
				scrollPane.setViewportView(textArea);
				contentPane.add(scrollPane,BorderLayout.CENTER);
			} else {
				contentPane.add(textArea,BorderLayout.CENTER);
		}	}
		setVisible(true);
	}

	public void init() 
	{
		if (m_ls == null) {
			try {
				createGUI();
			} catch (Throwable e) {
				String msg;
				
				System.err.println("createGUI didn't successfully complete");
				for (;e != null; e = e.getCause()) {
					msg = e.getMessage();
					if (msg == null) {
						msg = e.toString();
					}
					System.err.println(msg);
		}	}	}
	}

	/*
		Returns information about this applet. An applet should override this method to return a String containing information 
		about the author, version, and copyright of the applet. 
	 */

	public String getAppletInfo()
	{
		return Version.authorsAndCopyright();
	}

	public String[][] getParameterInfo()
	{
		return parameter_info;
	}
}

package lsedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;


class StreamGobbler extends Thread
{
    InputStream		m_is;
	OutputStream	m_os;
    
    StreamGobbler(InputStream is, OutputStream os)
    {
        m_is   = is;
		m_os   = os;
    }
    
    public void run()
    {
		InputStream is  = m_is;
		OutputStream os = m_os;
		int			c;

        try
        {
            while ( (c = is.read()) != -1) {
				os.write(c);
				os.flush();
			}
        } catch (IOException e) {
		}
    }
}

class ViewSource implements Runnable {

	LandscapeEditorCore	m_ls;
	String[]			m_argv;

	protected void message(String msg)
	{
		synchronized(this) {
			System.err.println(Util.toLocaleString() + ": " + msg);
	}	}

	ViewSource(LandscapeEditorCore ls, String[] argv)
	{
		m_ls   = ls;
		m_argv = argv;
	}

	public void run()
	{
		String[]	argv = m_argv;
		String		cmd;
		int			i;

		if (argv[0].equals(">")) {
			String title;
			String message;
			
			if (argv.length > 1) {
				title   = argv[1];
				message = "";
				for (i = 2; i < argv.length; ++i) {
					if (i > 2) {
						message += "\n";
					}
					message += argv[i];
				}
				JOptionPane.showMessageDialog(m_ls.getFrame(), message, title, JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
			}
			return;
		}	

		cmd = "";
		for (i = 0; i < argv.length; ++i) {
			cmd += argv[i] + " ";
		}

		// Execute the command
		message("Executing [ " + cmd + "]");

		try {
			Runtime runtime = Runtime.getRuntime();
			if (runtime == null) {
				message("No runtime available");
			} else {
				Process			process;
				StreamGobbler	in, out, err;

				process = runtime.exec(argv);
				out = new StreamGobbler(process.getInputStream(), System.out);
				new Thread(out).start();
				err = new StreamGobbler(process.getErrorStream(), System.err);
				new Thread(err).start();
				in = new StreamGobbler(System.in, process.getOutputStream());
				new Thread(in).start();
				process.waitFor();
				message("[ " + cmd + " ] Returned " + process.exitValue());
			}
		} catch (Exception error) {
			message("Exception executing [" + cmd + "] " + error.getMessage());
		}
	}
}	

class ExecuteExamples extends JDialog implements ActionListener {

	public final static String[] g_comments = new String[]
	{
/* 1 */	"To run Visual Studio Net jumping to lineno",
/* 2 */	"On Windows to run vi readonly jumping to lineno",
/* 3 */	"To run vi in a new xterm window",
/* 4 */	"To jump to lineno or definition if present",
/* 5 */	"To open up a project",
/* 6 */ "To display information in a dialog box"
	};

	public final static String[] g_commands = new String[]
	{
/* 1 */	"C:/Program Files/Microsoft Visual Studio .NET 2003/Common7/IDE/devenv.exe",
/* 2 */	"cmd.exe",
/* 3 */	"xterm",
/* 4 */	"C:/Program Files/Microsoft Visual Studio .NET 2003/Common7/IDE/devenv.exe",
/* 5 */	"C:/Program Files/Microsoft Visual Studio .NET 2003/Common7/IDE/devenv.exe",
/* 6 */ ">"
	};

	public final static String[] g_parameters = new String[]
	{
/* 1 */	"/command\n" +
		"Edit.Goto [$(lineno)|1]\n" +
		"\"$(file)\"\n",
		
/* 2 */	"/C\n" +
		"start; vi.exe -R [+$(lineno)] $(file)\n",

/* 3 */	"-e\n" +
		"vi\n" +
		"[+$(lineno)]\n" +
		"[\"$(file)\"]\n",

/* 4 */	"/command\n" +
		" [Edit.GoToDefinition $(function) | Edit.Goto [$(lineno)|0]]\n" +
		"\"$(file)\"\n",

/* 5 */	"$(project).sln\n",

/* 6 */ "Information about $(label)\n" +
		"file   = $(file)\n" +
		"lineno = $(lineno)\n"

	};

	static protected final int BUTTON_PREV     = 0;
	static protected final int BUTTON_NEXT     = 1;
	static protected final int BUTTON_SET      = 2;
	static protected final int BUTTON_OK       = 3;

	protected final static String[] m_button_titles =
							{
								"Prev",
								"Next",
								"Set",
								"Ok"
							};

	protected LandscapeEditorCore	m_ls;
	protected ExecuteConfigure		m_executeConfigure;
	protected JLabel				m_comment;
	protected JTextArea				m_parameters;
	protected int					m_index;
	protected JButton[]				m_buttons;


	protected void fill()
	{
		m_comment.setText(g_comments[m_index]);
		m_parameters.setText(g_commands[m_index] + "\n" + g_parameters[m_index]);
	}

	public ExecuteExamples(LandscapeEditorCore ls, ExecuteConfigure executeConfigure)
	{
		super(ls.getFrame(), "Execution Configuration Examples", true);

		Font				font, bold;
		Container			contentPane;
		JScrollPane			scrollPane;
		JLabel				comment;
		JTextArea			textArea;
		JPanel				buttonPanel;
		int					i;
		JButton				button;

		m_ls               = ls;
		m_executeConfigure = executeConfigure;

		font               = FontCache.getDialogFont();
		bold               = font.deriveFont(Font.BOLD);

		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);

		m_comment = comment = new JLabel("", JLabel.LEFT);
		contentPane.add(BorderLayout.NORTH, comment);
		
		m_parameters = textArea = new JTextArea();
		textArea.setBackground(Diagram.boxColor);
		textArea.setFont(Options.getTargetFont(Option.FONT_TEXTBOX_TEXT));
		textArea.setEditable(false);
		textArea.setLineWrap(false);
		textArea.setRows(10);

		m_index = 0;
		fill();

		scrollPane   = new JScrollPane(textArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 

		contentPane.add(BorderLayout.CENTER, scrollPane);


		// --------------
		// Use a FlowLayout to center the button and give it margins.

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

		m_buttons = new JButton[m_button_titles.length];
		for (i = 0; i < m_button_titles.length; ++i) {
			m_buttons[i] = button = new JButton(m_button_titles[i]);
			button.setFont(bold);
			button.addActionListener(this);
			buttonPanel.add(button);
		}
		contentPane.add(BorderLayout.SOUTH,  buttonPanel);

		// Resize the window to the preferred size of its components
		pack();
		setVisible(true);
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object		source;
		int			state, i;

		source = ev.getSource();

		state = -1;
		for (i = 0; i < m_button_titles.length; ++i) {
			if (source == m_buttons[i]) {
				state = i;
				break;
		}	}

		switch (state) {
		case BUTTON_NEXT:
			if (++m_index >= g_comments.length) {
				m_index = 0;
			}
			fill();
			return;
		case BUTTON_PREV:
			if (--m_index < 0) {
				m_index = g_comments.length - 1;
			}
			fill();
			return;
		case BUTTON_SET:
		{
			JComboBox	comboBox = m_executeConfigure.m_program;
			int			items    = comboBox.getItemCount();
			String		command, item;

			command = g_commands[m_index];

			for (i = 0; ; ++i) {
				if (i == items) {
					comboBox.addItem(command);
					break;
				}
				item = (String) comboBox.getItemAt(i);
				if (command.equals(item)) {
					break;
			}	} 
			comboBox.setSelectedIndex(i);
			m_executeConfigure.m_parameters.setText(g_parameters[m_index]);
		}
		case BUTTON_OK:
			break;
		default:
			return;
		}
		setVisible(false);
		return;
	}
}

class ExecuteConfigure extends JDialog implements ActionListener {

	public final static String[] g_mac_programs = new String[]
								{
									">",
									"vi",
									"xterm"
								};

	public final static String[] g_unix_programs = new String[]
								{
									">",
									"vi",
									"pico",
									"emacs",
									"xterm"
								};

	public final static String[] g_windows_programs = new String[]
								{
									">",
									"cmd.exe",
									"notepad.exe",
									"C:/Program Files/Microsoft Visual Studio/Common/MSDev98/Bin/MSDEV.EXE",
									"C:/Program Files/Microsoft Visual Studio .NET 2003/Common7/IDE/devenv.exe"
								};

	static protected final int BUTTON_OK       = 0;
	static protected final int BUTTON_CANCEL   = 1;
	static protected final int BUTTON_CLEAR    = 2;
	static protected final int BUTTON_BROWSE   = 3;
	static protected final int BUTTON_HELP     = 4;
	static protected final int BUTTON_EXAMPLES = 5;

	protected final static String[] m_button_titles =
							{
								"Ok",
								"Cancel",
								"Clear",
								"Browse",
								"Help",
								"Examples"
							};

	protected LandscapeEditorCore	m_ls;
	protected String[]				m_os_programs;
	public    JComboBox				m_program;
	public    JTextArea				m_parameters;
	protected JButton[]				m_buttons;
	protected int					m_index;


	protected final static String[] g_choose_index =
							{
								"h",
								"F4",
								"F5",
								"F6",
								"F7",
								"F8",
								"F9",
								"F10"
							};

	protected JComboBox				m_choose_index;

	
	protected void browse()
	{
		JFileChooser	fileChooser = new JFileChooser();

	
		String	defaultFilename = null;
		String	viewer          = null;
		File	defaultfile, startfile, file;
		int		ret;

		defaultFilename = (String) m_program.getItemAt(0);
		if (defaultFilename != null) {
			defaultfile = new File(defaultFilename);
		} else {
			defaultfile = null;
		}

		fileChooser.setDialogTitle("Identify program to execute");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setApproveButtonText("Select");

		if (defaultfile != null) {
			fileChooser.setSelectedFile(defaultfile);
		}

		ret = fileChooser.showOpenDialog(this);

		if (ret == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			viewer = file.getAbsolutePath();
			m_program.setSelectedIndex(0);
			m_program.setSelectedItem(viewer);
	}	}

	protected void fill(int index)
	{
		JComboBox	comboBox;
		String		command, program, rules;
		String[]	programs;
		int			i;

		m_index = index;
		if (m_index < 0) {
			m_index = 0;
		}
		comboBox = m_program;
		comboBox.removeAllItems();
		
		command = ExecuteAction.m_commands[m_index];
		if (command != null) {
			comboBox.addItem(command);
		}
		programs = m_os_programs;
		for (i = 0; i < programs.length; ++i) {
			program = programs[i];
			if (!program.equals(command)) {
				comboBox.addItem(program);
		}	}
		comboBox.addItem("");
		comboBox.setSelectedIndex(0);

		rules = ExecuteAction.m_rules[m_index];
		if (rules == null) {
			rules = "";
		}
		m_parameters.setText(rules);
	}

	protected static boolean empty(String string)
	{
		int		i, lth;

		lth = string.length();
		for (i = 0; ; ++i) {
			if (i >= lth) {
				return(true);
			}
			switch (string.charAt(i)) {
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				continue;
			}
			return(false);
	}	}

	protected void doOk()
	{
		String	command, rule;
		int		i;

		command = (String) m_program.getSelectedItem();
		if (empty(command)) {
			command = null;
		}

	
		ExecuteAction.m_commands[m_index] = command;

		rule = (String) m_parameters.getText();
		if (empty(rule)) {
			rule = null;
		}
			
		ExecuteAction.m_rules[m_index] = rule;
	}

	public ExecuteConfigure(LandscapeEditorCore ls, int index)
	{
		super(ls.getFrame(), "External Execution Configuration", true);

		Font				font, bold;
		Container			contentPane;
		JComboBox			comboBox;
		JScrollPane			scrollPane;
		JTextArea			textArea;
		JPanel				buttonPanel;
		String[]			programs;
		String				program;
		int					jvm, i;
		JButton				button;
		String				command, title;

		m_ls = ls;

		font         = FontCache.getDialogFont();
		bold         = font.deriveFont(Font.BOLD);

		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		setForeground(ColorCache.get(0,0,0));
		setBackground(ColorCache.get(192,192,192));
		setFont(font);

		jvm = BrowserLauncher.getJVM();

		switch (jvm) {
		case BrowserLauncher.MRJ_2_0:
		case BrowserLauncher.MRJ_2_1:
		case BrowserLauncher.MRJ_3_0:
		case BrowserLauncher.MRJ_3_1:
			m_os_programs = g_mac_programs;
			break;	
		case BrowserLauncher.WINDOWS_NT:
		case BrowserLauncher.WINDOWS_9x:
			m_os_programs = g_windows_programs;
			break;
		default:
			m_os_programs = g_unix_programs;
		}
		
		m_program = comboBox = new JComboBox();
		m_parameters = textArea = new JTextArea();

		fill(index);


		comboBox.setEditable(true);
		comboBox.addActionListener(this);

		contentPane.add(BorderLayout.NORTH, comboBox);

		textArea.setToolTipText("Command parameters - press [help] for details");

		textArea.setBackground(Diagram.boxColor);
		textArea.setFont(Options.getTargetFont(Option.FONT_TEXTBOX_TEXT));
		textArea.setEditable(true);
		textArea.setLineWrap(false);
		textArea.setRows(10);

		scrollPane   = new JScrollPane(textArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 

		contentPane.add(BorderLayout.CENTER, scrollPane);


		// --------------
		// Use a FlowLayout to center the button and give it margins.

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

		m_buttons = new JButton[m_button_titles.length];
		for (i = 0; i < m_button_titles.length; ++i) {
			title = m_button_titles[i];
			m_buttons[i] = button = new JButton(title);
			button.setFont(bold);
			button.addActionListener(this);
			buttonPanel.add(button);
		}

		if (index >= 0) {
			m_choose_index = null;
		} else {
			m_choose_index = new JComboBox(g_choose_index);
			m_choose_index.setFont(bold);
			m_choose_index.addActionListener(this);
			buttonPanel.add(m_choose_index);
		}

		contentPane.add(BorderLayout.SOUTH,  buttonPanel);

		// Resize the window to the preferred size of its components
		pack();
		setVisible(true);
	}

	// ActionListener interface

	public void actionPerformed(ActionEvent ev)
	{
		Object		source;
		int			state, i;

		source = ev.getSource();

		if (source == m_choose_index) {
			i = m_choose_index.getSelectedIndex();
			if (i >= 0 && i != m_index) {
				doOk();
				fill(i);
			}
			return;
		}

		state = -1;
		for (i = 0; i < m_button_titles.length; ++i) {
			if (source == m_buttons[i]) {
				state = i;
				break;
		}	}

		switch (state) {
		case BUTTON_CLEAR:
			m_parameters.setText("");
			return;
		case BUTTON_BROWSE:
			browse();
			return;
		case BUTTON_HELP:
			JOptionPane.showMessageDialog(m_ls.getFrame(), 	
			  "The initial editable combo box allows an arbitrary program to be specified.\n" +
			  "Each line in the subsequent text area identifies a single parameter to be\n" +
			  "passed to the specified program in the specified order.  Within a parameter\n" +
			  "the macro ${name} is replaced by the attribute value of this named attribute.\n" +
			  "To handle optional occurances of attribute values, parameters may also contain\n" +
			  "rules of the form [ rule1 | rule2 | ... ].  This expands to rule1 if all of\n" +
			  "the attribute values (if any) required to form rule1 exist, or to the first\n" +
			  "such rule within the list (if any) that does.  If no such rule exists then\n" +
			  "this material is removed from the parameter.  If a parameter as a consequence\n" +
			  "contains only white space it is ignored during command invocation.\n\n" +
			  "If the command specified is '>' then a dialog box appears showing the expanded\n" +
			  "rules, with the first such rule becoming the title of the dialog box."
			   , "Help", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
			return;
		case BUTTON_EXAMPLES:
			new ExecuteExamples(m_ls, this);
			return;
		case BUTTON_OK:
			doOk();
		case BUTTON_CANCEL:
			break;
		default:
			return;
		}
		setVisible(false);
		return;
	}
}

public class ExecuteAction
{
	public static final	int	COMMANDS = 8;

	private static	String[] m_tags = 
	{
		"execute:command[",
		"execute:parm["
	};

	public static String[]	m_commands = new String[COMMANDS];
	public static String[]	m_rules    = new String[COMMANDS];

	public static void loadIni(String attribute, String value)
	{
		String	tag, rules;
		int		i, at, index;

		for (i = 0; i < m_tags.length; ++i) {
			tag = m_tags[i];
			if (attribute.startsWith(tag)) {
				at    = tag.length();
				index = attribute.charAt(at) - '0';
				if (index < 0 || index > COMMANDS) {
					continue;
				}
				switch(i) {
				case 0:
					m_commands[index] = value;
					m_rules[index]   = "";
					return;
				case 1:
					m_rules[index]  += value + "\n";
					return;
	}	}	}	}

	public static void saveOptions(PrintWriter ps)
	{
		String	string, string1, rules;
		int		index, i, j, at;
	
		for (index = 0; index < COMMANDS; ++index) {
			rules = m_rules[index];
			for (i = 0; i < m_tags.length; ++i) {
				switch (i) {
				case 0:
					string = m_commands[index];
					if (string != null && string.length() > 0) {
						ps.println(m_tags[i] + index + "]=" + string);
					}
					break;
				case 1:
					rules = m_rules[index];
					if (rules != null) {
						for (at = 0;; at = j+1) {
							j = rules.indexOf('\n', at);
							if (j < 0) {
								string = rules.substring(at);
								if (string.length() > 0) {
									ps.println(m_tags[i] + index + "]=" + string);
								}
								break;
							}	
							string = rules.substring(at, j);
							if (string.length() > 0) {
								ps.println(m_tags[i] + index + "]=" + string);
							}
							at = j+1;
					}	}
					break;
				}
		}	}
	}	

	public static void configure(LandscapeEditorCore ls, int index)
	{
		ExecuteConfigure configure = new ExecuteConfigure(ls, index);
		configure.dispose();
	}

	protected static String getValue(LandscapeObject object, String name)
	{
		int			index = object.getLsAttributeOffset(name);
		Object		item;
		String		value = null;
		Attribute	attr;

		if (index >= 0) {
			item  = object.getLsAttributeValueAt(index);
			if (item != null) {
				value = item.toString();
			}
		} else {
			attr  = object.getLsAttribute(name);
			if (attr != null && attr.countValues() == 1) {
				value = attr.parseString();
		}	}
		if (value != null) {
			value  = value.replace('\r', ' ');
			value  = value.replace('\n', ' ');
		}
		return value;
	}	

	protected static LandscapeObject getParent(LandscapeObject object)
	{
		if (object instanceof RelationInstance) {
			RelationInstance ri = (RelationInstance) object;
			
			if (ri.isMarked(RelationInstance.REVERSED_MARK)) {
				return ri.getDst();
			}
			return ri.getSrc();
		}
		if (object instanceof EntityInstance) {
			return ((EntityInstance) object).getOriginalContainedBy();
		}
		return null;
	}

	// Returns expanded string else null

	protected static String expand(LandscapeObject object, String rules, int start, boolean optional) 
	{
		int				i, j, lth, depth, pos, lth1;
		char			c;
		String			ret, name, ret1, value, value1;
		LandscapeObject parent;

		ret = "";
		lth = rules.length();
		for (i = start; i < lth; ++i) {
			c = rules.charAt(i);
			switch (rules.charAt(i)) {
			case '\\':
				if (i < lth-1) {
					c = rules.charAt(++i);
				}
				break;
			case '$':
				if (i > lth-3) {
					break;
				}
				c = rules.charAt(i+1);
				if (c == '{') {
					c = '}';
				} else if (c == '(') {
					c = ')';
				} else {
					break;
				}
				name = null;
				j    = rules.indexOf(c, i+2);
				if (j < 0) {
					break;
				} 
				name  = rules.substring(i+2, j);
				value = getValue(object, name);
				if (value == null) {
					if (optional) {
						return null;
					}
				} else {
					parent = object;
					lth1   = value.length();
					while (lth1 != 0 && value.charAt(0) == '.') {
						parent = getParent(parent);
						pos    = 1;
						if (lth1 > 1 && value.charAt(1) == '.') {
							pos    = 2;
							if (parent != null) {
								parent = getParent(parent);
						}	}
						if (parent == null) {
							break;
						}
						value1 = getValue(parent, name);
						if (value1 == null) {
							continue;
						}
						if (value1.endsWith("/") && value.length() > pos && value.charAt(pos) == '/') {
							++pos;
						}
						value = value1 + value.substring(pos);
						lth1  = value.length();
					}						
					ret += value;
				}
				i    = j;
				continue;
			case '|':
			case ']':
				if (optional) {
					return ret;
				}
				break;
			case '[':

				for (; ; ) {
					++i;
					ret1 = expand(object, rules, i, true);

					for (depth = 1; i < lth; ++i) {
						c = rules.charAt(i);
						switch (c) {
						case '\\':
							if (i < lth-1) {
								++i;
							}
							continue;
						case '|':
							if (depth == 1 && ret1 == null) {
								break;
							}
							continue;
						case '[':
							++depth;
							continue;
						case ']':
							break;
						default:
							continue;
						}
						if (--depth == 0) {
							break;
					}	}
					if (c != '|') {
						break;
				}	}
				if (ret1 != null) {
					ret += ret1;
				} else if (optional) {
					return null;
				}
				continue;
			}
			ret += c;
		}
		return(ret);
	}

	// Eliminate blank lines from parameters and starting whitespace

	protected static String contract(String expanded) 
	{
		int		lth = expanded.length();
		int		i, j, k;
		char	c;
		boolean	whitespace = true;
		String	ret = "";

		for (i = j = 0; ; ++i) {

			if (i >= lth) {
				c = 0;
			} else {
				c = expanded.charAt(i);
			}
			switch (c) {
			case '\n':
			case 0:
				if (!whitespace) {
					ret += expanded.substring(j, i) + '\n';
				}
				if (c == 0) {
					break;
				}
				whitespace = true;
			case ' ':
			case '\t':
			case '\r':
				continue;
			default:
				if (whitespace) {
					j = i;
					whitespace = false;
				}
				continue;
			}
			break;
		}
		return ret;
	}

	// Count the number of parameters

	protected static int count(String expanded) 
	{
		int		i;
		int		lth = expanded.length();
		int		cnt = 0;

		for (i = 0; i < lth; ++i) {
			if (expanded.charAt(i) == '\n') {
				++cnt;
		}	}
		return(cnt);
	}

	protected static void fill(String expanded, String[] parameters) 
	{
		int		i, j, k;
		int		lth = expanded.length();
		int		cnt = 0;
		char	c, c1;

		k = 0;
		for (i = j = 0; i < lth; ++i) {
			c = expanded.charAt(i);
			switch (c) {
			case '\r':
			case '\n':
				parameters[++k] = expanded.substring(j, i);
				if (i < lth - 1) {
					c1 = expanded.charAt(i+1);
					if (c1 != c) {
						switch(c1) {
						case '\r':
						case '\n':
							++i;
				}	}	}
				j = i+1;
				break;
	}	}	}

	public static void onObject(LandscapeEditorCore ls, int index, LandscapeObject object)
	{
		if (ls.isApplet()) {
			ls.error("Applets can't open source files");
			return;
		}
		if (object == null) {
			Diagram diagram = ls.getDiagram();

			if (diagram != null) {
				object = diagram.targetEntity(object);
		}	}

		if (object == null) {
			ls.error("No object specified");
			return;
		}
		if (!(object instanceof LandscapeObject)) {
			ls.error(object + " not a LandscapeObject");
			return;
		} 

		if (index < 0 || index >= COMMANDS) {
			index = 0;
		}
		String	command = m_commands[index];

		if (command == null) {
			configure(ls, index);
			command = m_commands[index];
			if (command == null) {
				return;
		}	}

		String[]	argv;
		String		rules    = m_rules[index];
		String		expanded;
		int			cnt;

		if (rules == null) {
			argv = new String[1];
		} else {
//			System.out.println("   Rules:" + rules);
			expanded = expand(object, rules, 0, false);
//			System.out.println("  Expand:" + expanded);
			expanded = contract(expanded);
//			System.out.println("Contract:" + expanded);
			cnt      = count(expanded);
			argv     = new String[cnt+1];
			if (cnt != 0) {
				fill(expanded, argv);
		}	}
		argv[0]  = command;
//		System.out.println("    Args:" + argv);

			
		ViewSource viewSource = new ViewSource(ls, argv);

		new Thread(viewSource).start();
	}
} 

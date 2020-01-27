package lsedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

// Derived from: http://java.sun.com/products/java-media/2D/forDevelopers/sdk12print.html

/**
 * A simple Pageable class that can
 * split a large drawing canvas over multiple
 * pages.
 *
 * The pages in a canvas are laid out on
 * pages going left to right and then top
 * to bottom.
 */

public class Vista implements Pageable, Printable {

	// Inner classes cant have static declarations

	static final String[] m_names = {"All", "Diagram", "TOC", "Results", "Legend", "Map", "Query", "Attribute"};
	static final String[] m_text  = {"Maximum pages : ", "Maximum across: ", "Maximum down  : "
	};
	public static final int PRINT_ALL       = 0;
	public static final int PRINT_DIAGRAM   = 1;
	public static final int PRINT_TOC       = 2;
	public static final int PRINT_RESULT    = 3;
	public static final int PRINT_LEGEND    = 4;
	public static final int PRINT_MAP       = 5;
	public static final int	PRINT_QUERY     = 6;
	public static final int PRINT_ATTRIBUTE = 7;

	public class PrintWhat extends JDialog implements ActionListener {

		protected LandscapeEditorCore m_ls;
		protected ButtonGroup		m_buttonGroup;
		protected JRadioButton[]	m_radioButtons;
		protected JTextField[]		m_pages;
		protected JButton			m_ok;
		protected JButton			m_cancel;
		
		protected int				m_printing = -1;				// What to print
		protected int				m_print_pages[] = {-2, -2, -2};	// Maximum print pages, Maximum X, Maximum Y
			
		public PrintWhat(LandscapeEditorCore ls)
		{
			super(ls.getFrame(), "Print", true);

			JFrame			frame;
			int				i, cnt;
			JRadioButton	radioButton;
			Container		contentPane;
			JPanel			grid;
			JPanel			panel;
			JLabel			label;
			Font			font, bold;

			font         = FontCache.getDialogFont();
			bold         = font.deriveFont(Font.BOLD);
			
			m_ls          = ls;

			frame = ls.getFrame();
			setLocation(frame.getX()+200, frame.getY()+300);
			setForeground(ColorCache.get(0,0,0));
			setBackground(ColorCache.get(192,192,192));
			setFont(font);

			contentPane = getContentPane();
			
			grid           = new JPanel();
			grid.setLayout(new GridLayout(0,1));

			m_pages = new JTextField[3];
			for (i = 0; i < 3; ++i) {
				panel = new JPanel();
				panel.setLayout(new FlowLayout());
				label      = new JLabel(m_text[i]);
				label.setFont(bold);
				panel.add(label);
				m_pages[i] = new JTextField("", 5);
				m_pages[i].setFont(font);
				panel.add(m_pages[i]);
				grid.add(panel);
			}

			cnt            = m_names.length;
			m_buttonGroup  = new ButtonGroup();
			m_radioButtons = new JRadioButton[cnt];

			for (i = 0; i < cnt; ++i) {
				m_radioButtons[i] = radioButton = new JRadioButton(m_names[i]);
				radioButton.setFont(bold);
				m_buttonGroup.add(radioButton);
				grid.add(radioButton);
			}
			contentPane.add(grid, BorderLayout.CENTER);
			 
			panel = new JPanel();
			panel.setLayout(new FlowLayout());

			m_ok = new JButton("Ok");
			m_ok.setFont(bold);
			panel.add(m_ok);
			m_ok.addActionListener(this);

			m_cancel = new JButton("Cancel");
			m_cancel.setFont(bold);
			panel.add(m_cancel);
			m_cancel.addActionListener(this);

			contentPane.add(panel, BorderLayout.SOUTH);

			// Resize the window to the preferred size of its components

			this.pack();
			setVisible(true);
		}

		public int getPages()
		{
			return(m_print_pages[0]);
		}

		public int getMaxX()
		{
			return(m_print_pages[1]);
		}

		public int getMaxY()
		{
			return(m_print_pages[2]);
		}

		public int getPrinting()
		{
			return(m_printing);
		}

		public JComponent getComponent()
		{
			LandscapeEditorCore	ls;
			JComponent			c;

			ls = m_ls;
			c  = null;

			switch (m_printing) {
			case PRINT_ALL:
				c = ls.getContentPane();
				break;
			case PRINT_DIAGRAM:
				c = ls.getDiagram();
				break;
			case PRINT_TOC:
				c = ls.getTocBox();
				break;
			case PRINT_RESULT:
				c = ls.getResultBox();
				break;
			case PRINT_LEGEND:
				c = ls.getLegendBox();
				break;
			case PRINT_MAP:
				c = ls.getMapBox();
				break;
			case PRINT_QUERY:
				c = ls.getQueryBox();
				break;
			case PRINT_ATTRIBUTE:
				c = ls.getAttributeBox();
				break;
			}
			return(c);
		}

		// ActionListener interface

		public void actionPerformed(ActionEvent ev)
		{
			Object	source;

			// Pop down the window when the button is clicked.
			// System.out.println("event: " + ev);

			source = ev.getSource();

			if (source == m_ok) {
				int				cnt, i;
				JRadioButton	radioButton;
				String			pages;

				for (i = 0; i < 3; ++i) {
					pages = m_pages[i].getText();
					pages = pages.trim();
					if (pages.length() == 0) {
						m_print_pages[i] = -1;
					} else {
						try {
							m_print_pages[i] = Integer.parseInt(pages);
						} catch (Throwable exception) {
							m_print_pages[i] = -2;
				}	}	}

				cnt = m_names.length;
				for (i = 0; i < cnt; ++i) {
					radioButton = m_radioButtons[i];
					if (radioButton.isSelected()) {
						m_printing = i;
						break;
				}	}
				this.setVisible(false);
				return;
			}

			if (source == m_cancel) {
				m_printing = -2;
				this.setVisible(false);
				return;
			}
		}
	}

	public class PrintConfirm extends JDialog implements ActionListener {

		protected JButton			m_ok;
		protected JButton			m_cancel;
		protected boolean			m_isOk = false;	
			
		public PrintConfirm(LandscapeEditorCore ls) 	
		{
			super(ls.getFrame(), "Confirm", true);

			JFrame			frame;
			int				i, copies;
			Container		contentPane;
			JPanel			grid;
			JPanel			panel;
			JLabel			label;
			String			s;
			Font			font, bold;

			font         = FontCache.getDialogFont();
			bold         = font.deriveFont(Font.BOLD);
			
			frame = ls.getFrame();
			setLocation(frame.getX()+200, frame.getY()+300);
			setForeground(ColorCache.get(0,0,0));
			setBackground(ColorCache.get(192,192,192));
			setFont(font);

			contentPane = getContentPane();
			grid        = new JPanel();
			grid.setLayout(new GridLayout(0,1));

			for (i = 0; i < 17; ++i) {
				switch (i) {
				case 0:
					s = "Printing: " + m_names[m_printing];
					break;
				case 1:
				    s = "To: " + m_printerJob.getPrintService();
					break; 
				case 2:
					s = "Orient: ";
					switch (m_Format.getOrientation()) {
					case PageFormat.PORTRAIT:
						s += "Portrait";
						break;
					case PageFormat.LANDSCAPE:
						s += "Landscape";
						break;
					case PageFormat.REVERSE_LANDSCAPE:
						s += "Reverse Landscape";
						break;
					default:
						s += "??";
					}
					break;
				case 3:
					s = "Width: " + (m_Format.getWidth() / 72.0) + " inches";
					break;
				case 4:
					s = "Height: " + (m_Format.getHeight() / 72.0) + " inches";
					break;

				case 5:
					copies = m_printerJob.getCopies();
					if (copies == 1) {
						continue;
					}
					s = "Copies: " + copies;
					break;
				case 6:
					if (m_NumPages == 1) {
						continue;
					}
					s = "Shape: " + m_NumPagesX + " x " + m_NumPagesY + " pages";
					break;
				case 7:
					s = "Pages: " + m_NumPages;
					break;
				case 8:
					s = "Scale: " + m_ScaleX;
					break;
				case 9:
					s = "";
					break;
				case 10:
					s = "Component width: " + m_Component.getWidth() + " pixels";
					break;
				case 11:
					s = "Component height: " + m_Component.getHeight() + " pixels";
					break;
				case 12:
					s = "Imageable x: " + (m_Format.getImageableX() / 72.0) + " inches";
					break;
				case 13:
					s = "Imageable y: " + (m_Format.getImageableY() / 72.0) + " inches";
					break;
				case 14:
					s = "Imageable width: " + (m_Format.getImageableWidth() / 72.0) + " inches";
					break;
				case 15:
					s = "Imageable height: " + (m_Format.getImageableHeight() / 72.0) + " inches";
					break;
				case 16:
					s = "";
					break;
				default:
					s = "????";
				}
				label = new JLabel(s);
				label.setFont(font);
				grid.add(label);
			}

			contentPane.add(grid, BorderLayout.CENTER);
			 
			panel = new JPanel();
			panel.setLayout(new FlowLayout());

			m_ok = new JButton("Ok");
			m_ok.setFont(bold);
			panel.add(m_ok);
			m_ok.addActionListener(this);

			m_cancel = new JButton("Cancel");
			m_cancel.setFont(bold);
			panel.add(m_cancel);
			m_cancel.addActionListener(this);

			contentPane.add(panel, BorderLayout.SOUTH);

			// Resize the window to the preferred size of its components

			this.pack();
			setVisible(true);
		}

		public boolean isOk()
		{
			return(m_isOk);
		}
	
		// ActionListener interface

		public void actionPerformed(ActionEvent ev)
		{
			Object	source;

			// Pop down the window when the button is clicked.
			// System.out.println("event: " + ev);

			source = ev.getSource();

			if (source == m_ok) {
				m_isOk = true;
				this.setVisible(false);
				return;
			}

			if (source == m_cancel) {
				this.setVisible(false);
				return;
			}
		}
	}

	private	int			m_printing;
	private JComponent	m_Component;			// Component to print
	private int			m_NumPagesX;			// Number of pages across
	private int			m_NumPagesY;			// Number of pages down
	private int			m_NumPages;				// Total number of pages
	private int			m_pixels_per_page_x;	
	private int			m_pixels_per_page_y;
	private PrinterJob	m_printerJob;	
	private PageFormat	m_Format;
	private double		m_ScaleX;
	private double		m_ScaleY;

	/**
	 * Create a Pageable that can print a
	 * Swing JComponent over multiple pages.
	 *
	 * @param c The swing JComponent to be printed.
	 *
	 * @param format The size of the pages over which
	 * the componenent will be printed.
	 */
	
	public Vista(LandscapeEditorCore ls) 
	{
		PrintWhat	printWhat;
		JComponent	component;
		int			pages, maxX, maxY;
		double		width, height;

		printWhat  = new PrintWhat(ls);
		m_printing = printWhat.getPrinting();
		component  = printWhat.getComponent();

		if (component == null) {
			ls.doFeedback("No component selected to print");
			return;
		}
		m_Component     = component;
		width           = (double) component.getWidth();
		height          = (double) component.getHeight();
		if (width < 1.0 || height < 1.0) {
			ls.doFeedback("Component has no size");
			return;
		}

		pages = printWhat.getPages();
		maxX  = printWhat.getMaxX();
		maxY  = printWhat.getMaxY();

		switch (pages) {
		case 0:
			return;
		case -2:
			ls.doFeedback("Invalid number of print pages specified");
			return;
		}
		switch (maxX) {
		case 0:
			return;
		case -2:
			ls.doFeedback("Invalid maximum pages across");
			return;
		}
		switch (maxY) {
		case 0:
			return;
		case -2:
			ls.doFeedback("Invalid maximum pages down");
			return;
		}

		m_printerJob = PrinterJob.getPrinterJob();
		m_printerJob.setPageable(this); 
		try { 

			if (m_printerJob.printDialog()) { 
				double		imageableWidth, imageableHeight;
				double		scaleX, scaleY;

				m_Format = m_printerJob.defaultPage();
				m_Format = m_printerJob.pageDialog(m_Format);

				imageableWidth  = m_Format.getImageableWidth();
				imageableHeight = m_Format.getImageableHeight();
				if (imageableWidth < 1.0 || imageableHeight < 1.0) {
					ls.doFeedback("Output pages have no size");
					return;
				}

				scaleX      = imageableWidth  / width;
				scaleY      = imageableHeight / height;
				m_NumPagesX = 1;
				m_NumPagesY = 1;

				if (pages != -1 || maxX != -1 || maxY != -1) {
					// Some constraint on maximum pages to print
					for (;;) {
						if (scaleX <= scaleY) {
							// Currently our constraint is on growing the X

							if (pages == -1 || ((m_NumPagesX + 1) * m_NumPagesY) <= pages) {
								// Can grow X according to max pages
								switch (maxX) {
								case -1:	// No limit on pages across
									++m_NumPagesX;
									scaleX = (m_NumPagesX * imageableWidth) / width;
									continue;
								default:
									if (m_NumPagesX < maxX) {
										++m_NumPagesX;
										scaleX = (m_NumPagesX * imageableWidth) / width;
										continue;
							}	}	}
						} else {
							// Currently our constraint is on growing the Y
							if (pages == -1 || ((m_NumPagesY + 1) * m_NumPagesX) <= pages) {
								// Can grow Y according to max pages
								switch (maxY) {
								case -1:
									++m_NumPagesY;
									scaleY = (m_NumPagesY * imageableHeight) / height;
									continue;
								default:
									if (m_NumPagesY < maxY) {
										++m_NumPagesY;
										scaleY = (m_NumPagesY * imageableHeight) / height;
										continue;
						}	}	}	}
						break;
				}	}
				if (scaleX < scaleY) {
					m_pixels_per_page_x = 1 + ((component.getWidth() - 1) / m_NumPagesX);	// Round up
					m_pixels_per_page_y = (int) (((double) m_pixels_per_page_x) * (imageableHeight / imageableWidth));
					scaleY = scaleX;
				} else {
					m_pixels_per_page_y = 1 + ((component.getHeight() - 1) / m_NumPagesY);
					m_pixels_per_page_x = (int) (((double) m_pixels_per_page_y) * (imageableWidth / imageableHeight));
					scaleX = scaleY;
				}
				m_ScaleX    = scaleX;
				m_ScaleY    = scaleY;
				m_NumPages  = m_NumPagesX * m_NumPagesY;


				PrintConfirm printConfirm;

				printConfirm = new PrintConfirm(ls);
				if (printConfirm.isOk()) {
					ls.doFeedback("Printing " + m_NumPages + " pages (" + m_NumPagesX + " across " + m_NumPagesY + " down)" );
					m_printerJob.print();
				} else {
					ls.doFeedback("Printing cancelled");
			}	}
		} catch (PrinterException e) {
			System.out.println(e.getMessage());
		}
	}

/*
	static void experimental()
	{
		StreamPrintServiceFactory[] factories = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(null,null);
		StreamPrintServiceFactory	factory;
		DocFlavor[]					flavours;
		int	i, cnt;
		int	j, cnt1;

		if (factories == null) {
			cnt = 0;
		} else {
			cnt = factories.length;
		}
		for (i = 0; i < cnt; ++i) {
			factory = factories[i];
			flavours = factory.getSupportedDocFlavors();
			if (flavours == null) {
				cnt1 = 0;
			} else {
				cnt1 = flavours.length;
			}
			System.out.println("----");
			for (j = 0; j < cnt1; ++j) {
				System.out.println("< " + flavours[j]);
			}
			System.out.println("> " + factory.getOutputFormat());
	}	}
*/

	/**
	 * Returns the number of pages over which the canvas
	 * will be drawn.
	 */

	public int getNumberOfPages() 
	{
		if (m_NumPages < 0) {
			return Pageable.UNKNOWN_NUMBER_OF_PAGES;
		}
		return m_NumPages;
	}

	protected PageFormat getPageFormat() 
	{
		return m_Format;
	}
	
	/** 
	 * Returns the PageFormat of the page specified by
	 * pageIndex. For a Vista the PageFormat
	 * is the same for all pages.
	 *
	 * @param pageIndex the zero based index of the page whose
	 * PageFormat is being requested
	 * @return the PageFormat describing the size and
	 * orientation.
	 * @exception IndexOutOfBoundsException
	 * the Pageable  does not contain the requested
	 * page.
	 */

	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException 
	{
		if (pageIndex >= m_NumPages) {
			throw new IndexOutOfBoundsException();
		}
		return getPageFormat();
	}

	/**
	 * Returns the <code>Printable</code> instance responsible for
	 * rendering the page specified by <code>pageIndex</code>.
	 * In a Vista, all of the pages are drawn with the same
	 * Printable. This method however creates
	 * a Printable which calls the canvas's
	 * Printable. This new Printable
	 * is responsible for translating the coordinate system
	 * so that the desired part of the canvas hits the page.
	 *
	 * The Vista's pages cover the canvas by going left to
	 * right and then top to bottom. In order to change this
	 * behavior, override this method.
	 *
	 * @param pageIndex the zero based index of the page whose
	 * Printable is being requested
	 * @return the Printable that renders the page.
	 * @exception IndexOutOfBoundsException
	 * the Pageable does not contain the requested
	 * page.
	 */

	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException 
	{
		if (pageIndex < 0 || pageIndex >= m_NumPages) {
			throw new IndexOutOfBoundsException();
		}
		return this;
	}

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException 
	{
		if (pageIndex >= 0 && pageIndex < m_NumPages) {
			int xPage, yPage, xOffset, yOffset, xLeft, yLeft;

			yPage   = pageIndex / m_NumPagesX;
			xPage   = pageIndex - yPage * m_NumPagesX;
			xOffset = xPage * m_pixels_per_page_x;
			yOffset = yPage * m_pixels_per_page_y;
			xLeft   = m_Component.getWidth() - xOffset;
			if (xLeft > m_pixels_per_page_x) {
				xLeft = m_pixels_per_page_x;
			}
			yLeft   = m_Component.getHeight() - yOffset;
			if (yLeft > m_pixels_per_page_y) {
				yLeft = m_pixels_per_page_y;
			}

			Graphics2D g2 = (Graphics2D) graphics;
			
			/* Move the origin from the corner of the Paper to the corner
			 * of the imageable area.
			 */
//			g2.setClip(xOffset, yOffset, xLeft, yLeft);
			g2.translate(m_Format.getImageableX() - xOffset, m_Format.getImageableY() - yOffset);
			g2.scale(m_ScaleX, m_ScaleY);
			boolean wasBuffered = m_Component.isDoubleBuffered();
			m_Component.setDoubleBuffered(false);
			m_Component.paint(g2);
			m_Component.setDoubleBuffered(wasBuffered);

/*
			if (m_NumPages > 1) {
				graphics.setColor(Color.BLACK);
				graphics.drawString("Page " + pageIndex, 0, 0);
			} 
*/
			return Printable.PAGE_EXISTS;
		}
		return Printable.NO_SUCH_PAGE;
	}
}


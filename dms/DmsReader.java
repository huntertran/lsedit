package dms;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lsedit.Attribute;
import lsedit.Ta;
import lsedit.Diagram;
import lsedit.LandscapeObject;
import lsedit.EntityClass;
import lsedit.RelationClass;
import lsedit.EntityInstance;
import lsedit.RelationInstance;
import lsedit.ResultBox;
import lsedit.SpecialPath;

/* This code extends LandscapeEditorFrame so that Shared-Canvas does not need
 * to be made part of an LSEDIT compile -- only a dms compile
 *
 * Note that since it uses a socket we will have to sign any java applet
 *
 * http://docs.oracle.com/javase/1.4.2/docs/tooldocs/solaris/keytool.html
 *
 * 1. Create the jar file for dms
 * 2. keytool -genkey
 * 3. keytool -selfcert
 * 4. Keytool -list
 * 4. jarsigner -sigfile sigfile dms.jar mykey
 * 5. jar t < dms.jar

 * Do all this on the machine where the applet will be run
 */
 
public class DmsReader extends BasicReader {
	
	static XMLReader		m_xmlReader  = null;
	static DmsHandler		m_dmsHandler = null;
					
	private boolean
	extractURL(String parameters)
	{
		URL					url;
		URLConnection		conn;
		InputStream			is;
		InputStreamReader	reader;


		m_rdf = parameters;

		try {
     		url    = new URL(parameters);
		} catch (java.net.MalformedURLException e) {
			System.out.println(parameters);
			reportException(e);
			return false;
		}

		try {
     		conn   = url.openConnection();
			// Have to do things the hard way - Mark Pattons Shared-Canvas requires we request application/xml
			// So we must work from the connection level to set this request property
		
			conn.addRequestProperty("Accept", "application/xml");
			conn.addRequestProperty("Accept-Charset", "utf-8");

	     	is     = conn.getInputStream();
		} catch (IOException e) {
			reportException(e);
			return false;
		}

		try {
   			reader = new InputStreamReader(is, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			reportException(e);
			return false;
		}

		InputSource			input  = new InputSource(reader);

		try {
			m_xmlReader.parse(input);
		} catch (IOException e) {
			reportException(e);
			System.out.println("");
			return false;
		} catch (SAXException e) {
			reportException(e);
			System.out.println("");
			return false;
		}
		return true;
	}

    private boolean
	extract(Ta diagram, ResultBox resultBox, String parameters)
    {
		if (m_xmlReader == null) {
			DmsHandler handler = new DmsHandler(this);

			m_dmsHandler = handler;
			
			try {
				m_xmlReader  = XMLReaderFactory.createXMLReader();
			} catch (SAXException e) {
				reportException(e);
				return false;
			}	
			m_xmlReader.setContentHandler(handler);
			m_xmlReader.setErrorHandler(handler);
		}

		System.out.println("Reading " + parameters);
		resultBox.addText(parameters);
		resultBox.showAll();
		if (extractURL(parameters)) {
			System.out.println("**SUCCESS**");
			return true;
		}
		return false;
	}

	public String
	parseSpecialPath(Ta diagram, ResultBox resultBox, String path)
	{
		String parameters = path.substring(4);
		String msg        = null;
		String contains[] = {"contain", "describes","aggregates","creator"};
		Vector			temp;
		RelationClass	rc;
		RelationClass[] containsClasses;
		int				i;
		
		m_diagram   = (Diagram) diagram;
		m_resultBox = resultBox;
		
		setTitle("Loading DSM");
		
		if (parameters.equals("")) {
			// parameters = "http://rosetest.library.jhu.edu/m3";
			parameters = "http://www.shared-canvas.org/impl/demo1/res/Manifest.xml";

		}
        if (!extract(diagram, resultBox, parameters)) {
			msg = "Load failed!!";
			done(msg);
		} else {
			done("Loaded dsm (" + parameters + ")");
			msg = null;
		}
		return msg;
	}
}

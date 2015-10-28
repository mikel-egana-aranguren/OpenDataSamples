package euskadi.opendata.test.procurements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import euskadi.opendata.test.base.TestOpenDataBase;
import r01f.util.types.Strings;

abstract class TestProcurementsBase
	   extends TestOpenDataBase{
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static final DocumentBuilder _docBuilder;
	static {
		try {
			_docBuilder = DocumentBuilderFactory.newInstance()
												.newDocumentBuilder();
		} catch(ParserConfigurationException parserEx) {
			throw new IllegalStateException(parserEx);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Downloads a procurement data file and extracts the value and title
	 * @param contentName
	 * @param dataFileUrl
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	protected static Procurement _procurementDataFor(final String contentName,
											  		 final String dataFileUrl) throws MalformedURLException,IOException,
											  								   		  SAXException,XPathExpressionException {
		Document xmlDocument = _docBuilder.parse(new URL(dataFileUrl).openStream());
		XPath xpath = XPathFactory.newInstance().newXPath();
		String title = xpath.compile("/record/item[@name='contratacion']/value/item[@name='contratacion_titulo_contrato']/value").evaluate(xmlDocument);
		String value = xpath.compile("/record/item[@name='contratacion']/value/item[@name='contratacion_presupuesto_contrato_cab']/value").evaluate(xmlDocument);
					
		return new Procurement(contentName,
							   title,
							   Double.valueOf(_normalizeValue(value))
											  .doubleValue()); 			
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
	@Accessors(prefix="_")
	protected static class Procurement {
		@Getter private final String _contentName;
		@Getter private final String _title;
		@Getter private final double _value;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected static String _normalizeValue(final String value) {
		if (Strings.isNullOrEmpty(value)) return "0";
		return value.replace(".","")
					.replace(",",".");
	}
	protected static String _formatValue(final double value) {
	    // Conversor
	    DecimalFormat df = new DecimalFormat("###,###.##");
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(',');
		dfs.setMonetaryDecimalSeparator(',');
		dfs.setGroupingSeparator('.');
		df.setDecimalFormatSymbols(dfs);
		
		return df.format(value);
	}
}

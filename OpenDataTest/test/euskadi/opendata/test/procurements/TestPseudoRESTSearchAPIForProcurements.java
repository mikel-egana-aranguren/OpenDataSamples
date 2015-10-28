package euskadi.opendata.test.procurements;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import r01f.util.types.Strings;

import com.google.common.collect.Maps;

/**
 * Test the pseudo REST API for procurements
 */
public class TestPseudoRESTSearchAPIForProcurements 
	 extends TestProcurementsMaxMinBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static final String XPATH_ITEMS = "/searchSession/searchResultsBySource/searchSourceResults/results/item";
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	public static void main(String[] args) {
		try {
			Map<String,String> procurementDataUrls = null;
			
			// [1]: Use the search engine pseudo-api to get the url for every procurement data
			String searchUrl = "http://opendata.euskadi.eus/r01htSearchResultWAR/r01hPresentationXML.jsp?" +
										"r01kQry=tC:euskadi;tF:procedimientos_administrativos;tT:anuncio_contratacion;" +
											     "m:documentCreateDate.BETWEEN.01/01/2015,31/12/2015,documentLanguage.EQ.es;" + 
												 "o:contratacion_fecha_de_publicacion_documento.DESC;" + 
												 "pp:r01PageSize.10&r01kPgCmd=next&r01kSrchSrcId=contenidos.inter&r01kTgtPg=";
			
			
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
																	.newDocumentBuilder();
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			int currPage = 1;
			int totalPages = -1; 
			int totalResults = -1;
			do {
				// [a]: Load the XML from opendata and parse it
				String pageUrlStr = searchUrl + currPage;
				System.out.println(">>> Page: " + currPage + ": " + pageUrlStr);
				URL pageUrl = new URL(pageUrlStr);
				Document xmlDocument = docBuilder.parse(pageUrl.openStream());
				
				// [b]: If it's the first iteration, get the total number of pages
				if (totalPages == -1) {
					totalPages = Integer.valueOf(xpath.compile("/searchSession/searchResultsBySource/searchSourceResults/@numberOfPages")
													  .evaluate(xmlDocument))
										.intValue();
					totalResults = Integer.valueOf(xpath.compile("/searchSession/searchResultsBySource/searchSourceResults/@numberOfResults")
													  .evaluate(xmlDocument))
										.intValue();
					// create the list
					procurementDataUrls = Maps.newHashMapWithExpectedSize(totalResults);		
				}
				
				// [c]: get some data from the xml
				NodeList itemNodes =  (NodeList)xpath.compile(XPATH_ITEMS)
												 	 .evaluate(xmlDocument,XPathConstants.NODESET);
				System.out.println("\t" + itemNodes.getLength() + " elements");
				for (int j = 0; j < itemNodes.getLength(); j++) {
					Node itemNode = itemNodes.item(j);
					String contentName = xpath.compile(XPATH_ITEMS + "["+ (j+1) + "]/contentName")
											  .evaluate(itemNode,XPathConstants.STRING).toString();
					String documentWorkAreaRelativePath = xpath.compile(XPATH_ITEMS + "["+ (j+1) + "]/documentWorkAreaRelativePath")
															   .evaluate(itemNode,XPathConstants.STRING).toString();
					String documentLanguage = xpath.compile(XPATH_ITEMS + "["+ (j+1) + "]/documentLanguage")
												   .evaluate(itemNode,XPathConstants.STRING).toString();
					// ie: <documentDataFilesGeneratedFilesDocumentRelativePaths>
					//			<datafileOid.r01dpd015027db563d1db322099a64610112776a1><![CDATA[anuncio_contratacion;main:es_arch_expjaso5095.html]]></datafileOid.r01dpd015027db563d1db322099a64610112776a1>
					//		</documentDataFilesGeneratedFilesDocumentRelativePaths>
					NodeList dfNodes = (NodeList)xpath.compile(XPATH_ITEMS + "["+ (j+1) + "]/documentDataFilesGeneratedFilesDocumentRelativePaths/child::*")
													  .evaluate(itemNode,XPathConstants.NODESET);
					for (int k = 0; k < dfNodes.getLength(); k++) {
						Node currNode = dfNodes.item(k);
						if (currNode.getNodeType() == Node.ELEMENT_NODE) {
							String datafileOid = currNode.getNodeName();
							String procurementDataUrl = Strings.of("http://opendata.euskadi.eus/contenidos/{}/data/{}_{}")
															   .customizeWith(documentWorkAreaRelativePath,documentLanguage,datafileOid)
															   .asString();
							System.out.println("\t-" + j + " > "+ procurementDataUrl);
							procurementDataUrls.put(contentName,procurementDataUrl);
						}
					}
				}
				// next page
				currPage = currPage + 1;
			} while(currPage <= totalPages);
			
			
			// [2]: Download every procurement data file, parse it and get it's value 
			_computeMinAndMax(procurementDataUrls);
			
			
		} catch (IOException e) {
			e.printStackTrace(System.out);
		} catch (ParserConfigurationException e) {
			e.printStackTrace(System.out);
		} catch (SAXException e) {
			e.printStackTrace(System.out);
		} catch (XPathExpressionException e) {
			e.printStackTrace(System.out);
		}

	}
}

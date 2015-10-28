package euskadi.opendata.test.datasets;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import euskadi.opendata.test.base.TestOpenDataPseudoRESTSearchAPIBase;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.httpclient.HttpClient;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xml.XMLUtils;



/**
 * Simple Test for the OpenData SearchEngine without using the api
 * Wraps the SearchEngine PSEUDO-REST-API using a JAVA API
 * Testing the rest url: http://www.euskadi.net/r01hSearchResultWar/r01hPresentationXML.jsp?
 * 										r01kTgtPg=1&
 * 										r01kSrchSrcId=contenidos.inter&
 * 										r01kQry=tC:euskadi;tF:opendata;
 * 												pp:r01NavBarBlockSize.9,r01PageSize.10
 */
public class TestPseudoRESTSearchAPIForDataSets 
	 extends TestOpenDataPseudoRESTSearchAPIBase {
///////////////////////////////////////////////////////////////////////////////////////////////////
//	main
///////////////////////////////////////////////////////////////////////////////////////////////////	
	public static void main(String[] args) {
		try {
			int currPage = 1;
			int totalPages = -1; 
			
			do {
				System.out.println("\n\n\n[Pagina: " + currPage + "]=======================================================================");
				
				// [1] Construir la URL con la query PSEUDO-REST
				String qry = _buildQuery("contenidos.inter",
										 1,
										 "euskadi","opendata");
				System.out.println("[1] Query: " + qry);
				
				// [2] Obtener el XML de la sesión de búsqueda
				String sessionXML = HttpClient.forUrl(qry)
										      .GET()
										      		.loadAsString();
				System.out.println("[2] SearchEngineSession: " + sessionXML);
				
				// [3] Parsear el XML de la sessión de búsqueda para obtener los items
				SessionData<DataSetData> sessionData = _parseSearchSessionXML(sessionXML);
				if (sessionData != null) {
					System.out.println("[3] ParsedSeachEngineSession: " + sessionData.debugInfo());
					if (CollectionUtils.hasData(sessionData.getItems())) {
						for (DataSetData item : sessionData.getItems()) {
							System.out.println(Strings.of("\n\n\n________________________________________________________________________________________________\n{}\n\n\n")	
													  .customizeWith(item.debugInfo()));
						}
					}
				}
				totalPages = sessionData.getNumberOfResults();
				currPage = currPage + 1;
			} while(currPage <= totalPages);
			
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
		}
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
//	BUILD QUERY
///////////////////////////////////////////////////////////////////////////////////////////////////	
	private static String _buildQuery(String repo,int targetPage,
									  String cluster,String family) {
		// http://www.euskadi.net/r01hSearchResultWar/r01hPresentationXML.jsp?r01kTgtPg=1&r01kSrchSrcId=contenidos.inter&r01kQry=tC:euskadi;tF:opendata;pp:r01NavBarBlockSize.9,r01PageSize.10
		// http://www.euskadi.net/r01hSearchResultWar/r01hPresentationXML.jsp?r01kTgtPg=1&r01kSrchSrcId=contenidos.inter&r01kQry=tC:euskadi;tF:opendata;pp:r01PageSize.1000
		String outQry = Strings.of("http://opendata.euskadi.net/r01hSearchResultWar/r01hPresentationXML.jsp?" +
																	"r01kTgtPg={}" + "&" + 
																	"r01kSrchSrcId={}" + "&" +
																	"r01kQry=tC:{};tF:{};" +  
																	"pp:r01NavBarBlockSize.9,r01PageSize.10")
							   .customizeWith(targetPage,
									   	 	  repo,
									   	 	  cluster,family)
				 			   .asString();
		return outQry;
				
	}
	/**
	 * Procesar el XML de la sesión de búsqueda
	 * @param sessionXML el xml de la sessión 
	 */
	private static SessionData<DataSetData> _parseSearchSessionXML(final String sessionXML) throws SAXException,
																				   	  			   XPathExpressionException {
		// [1] - Parsear el XML devuelto
		Document xml = XMLUtils.parse(sessionXML);
		
		// [2] - Create a session object 
		SessionData<DataSetData> outSessionData = TestOpenDataPseudoRESTSearchAPIBase.createSessionData(xml.getDocumentElement());
		
		// [3] - download the file that contains the data-set specific meta-data
		//		 and parse it
		NodeList itemNodes = XMLUtils.nodeListByXPath(xml.getDocumentElement(),
													  "/searchSession/searchResultsBySource/searchSourceResults/results/item");
		Set<DataSetData> items = null;
		if (itemNodes != null && itemNodes.getLength() > 0) {
			items = new HashSet<DataSetData>(itemNodes.getLength());
			for (int i=0; i<itemNodes.getLength(); i++) {
				items.add(_parseSearchSessionResultItem(itemNodes.item(i)));
			}
		}
		outSessionData.setItems(items);
		
		// [3] - Devolver
		return outSessionData;
	}
	/**
	 * Procesar un item de resultado de la sesión de búsqueda
	 * @param itemNode
	 * @return los datos del item
	 */
	private static DataSetData _parseSearchSessionResultItem(final Node itemNode) throws XPathExpressionException {
		DataSetData outItem = new DataSetData();
		
		// Algunos metaDatos comunes
		// -------------------------
		String contentName = XMLUtils.stringByXPath(itemNode,
													"contentName/text()");
		String langVersionWorkAreaRelativePath = XMLUtils.stringByXPath(itemNode,
																		"documentWorkAreaRelativePath");
		String langVersionLanguage = XMLUtils.stringByXPath(itemNode,
														    "documentLanguage");
		String langVersionName = XMLUtils.stringByXPath(itemNode,
														"documentName");
		// Algunos meta-datos relevantes dependiendo del tipo de contenido
		// ----------------------------------------------------------------
		String dataFileUrl = TestOpenDataPseudoRESTSearchAPIBase.getContentTypeDependentMetaDataFileUrl(itemNode);
		String dataFileXML = null;
		try {
			dataFileXML = HttpClient.forUrl(dataFileUrl)
						  		 	    .GET()
						                .loadAsString();
			dataFileXML = dataFileXML.trim();
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
		}
		
		// Componer el objeto que encapsula los datos
		outItem.setContentName(contentName);
		outItem.setLang(langVersionLanguage);
		outItem.setLangVersionName(langVersionName);
		outItem.setDataFileXML(dataFileXML);
//		outItem.setReusableAssets(assets);
		return outItem;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
//	ITEM FOR THE RETURNED DATA
///////////////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	static class DataSetData 
	  implements Debuggable {
		@Getter @Setter private String _contentName;
		@Getter @Setter private String _lang;
		@Getter @Setter private String _langVersionName;
		@Getter @Setter private Map<String,Path> _reusableAssets;
		@Getter @Setter private String _dataFileXML;
		
		@Override
		public String debugInfo() {
			return Strings.create().add("     Content Name: ").addLine(_contentName)
								   .add("Lang Version Name: ").add("(").add(_lang).add(")").addLine(_langVersionName)
								   .add("  Reusable Assets: ").addLine((_reusableAssets != null ? Integer.toString(_reusableAssets.size()) : ""))
								   .add("     DataFile XML: ").add(_dataFileXML)
								   .asString();
		}
	}
}

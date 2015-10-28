package euskadi.opendata.test.servicios;

import java.util.HashMap;
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
 * Testing the rest url: http://opendata.euskadi.eus/r01hSearchResultWar/r01hPresentationXML.jsp?
 * 										r01kTgtPg=1&
 * 										r01kSrchSrcId=contenidos.inter&
 * 										r01kQry=tC:euskadi;tF:procedimientos_administrativos;tT:ayuda_subvencion;
 * 												m:procedureStatus.EQ.16;
 * 												o:createDate.ASC;
 * 												cA:label1;
 * 												pp:r01NavBarBlockSize.9,r01PageSize.10
 */
public class TestPseudoRESTSearchAPIForServicios 
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
	//			String qry = _buildQuery("contenidos.inter",
	//									 1,
	//									 "euskadi","procedimientos_administrativos","ayuda_subvencion", 
	//									 "16",
	//									 "cA:label1");
				String qry = _buildQuery("contenidos.inter",
										 1,
										 "euskadi","procedimientos_administrativos","ayuda_subvencion");
				System.out.println("[1] Query: " + qry);
				
				// [2] Obtener el XML de la sesión de búsqueda
				String sessionXML = HttpClient.forUrl(qry)
										      .GET()
										      .loadAsString();
				System.out.println("[2] SearchEngineSession: " + sessionXML);
				
				// [3] Parsear el XML de la sessión de búsqueda para obtener los items
				SessionData<ServiceData> sessionData = _parseSearchSessionXML(sessionXML);
				if (sessionData != null) {
					System.out.println("[3] ParsedSeachEngineSession: " + sessionData.debugInfo());
					if (CollectionUtils.hasData(sessionData.getItems())) {
						for (ServiceData item : sessionData.getItems()) {
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
	/**
	 * Builds a searchQuery
	 * @return the SearchQuery
	 */
	private static String _buildQuery(String repo,int targetPage,
									  String cluster,String family,String type,
									  String procedureStatus,
									  String labelToFilter) {
		// http://www.euskadi.net/r01hSearchResultWar/r01hPresentationXML.jsp?r01kTgtPg=1&r01kSrchSrcId=contenidos.inter&r01kQry=tC:euskadi;tF:procedimientos_administrativos;tT:ayuda_subvencion;m:procedureStatus.EQ.16;o:createDate.ASC;cA:label1;pp:r01NavBarBlockSize.9,r01PageSize.10
		String outQry = Strings.of("http://opendata.euskadi.eus/r01hSearchResultWar/r01hPresentationXML.jsp?" +
																	"r01kTgtPg={}" + "&" + 
																	"r01kSrchSrcId={}" + "&" +
																	"r01kQry=tC:{};tF:{};tT:{};" + 
																			"m:procedureStatus.EQ.{};o:createDate.ASC;" + 
																			"cA:{};" + 
																	"pp:r01NavBarBlockSize.9,r01PageSize.10")
							   .customizeWith(targetPage,
									   	 	  repo,
									   	 	  cluster,family,type,
									   	 	  procedureStatus,
									   	 	  labelToFilter)
				 			   .asString();
		return outQry;
				
	}
	private static String _buildQuery(String repo,int targetPage,
									  String cluster,String family,String type) {
		// http://www.euskadi.net/r01hSearchResultWar/r01hPresentationXML.jsp?r01kTgtPg=1&r01kSrchSrcId=contenidos.inter&r01kQry=tC:euskadi;tF:procedimientos_administrativos;tT:ayuda_subvencion;o:procedureStartDate.DESC;pp:r01NavBarBlockSize.9,r01PageSize.10
		// http://www.euskadi.net/r01hSearchResultWar/r01hPresentationXML.jsp?r01kTgtPg=1&r01kSrchSrcId=contenidos.inter&r01kQry=tC:euskadi;tF:procedimientos_administrativos;tT:ayuda_subvencion;m:documentLanguage.EQ.es;o:procedureStartDate.DESC;pp:r01PageSize.1000
		String outQry = Strings.of("http://opendata.euskadi.eus/r01hSearchResultWar/r01hPresentationXML.jsp?" +
																	"r01kTgtPg={}" + "&" + 
																	"r01kSrchSrcId={}" + "&" +
																	"r01kQry=tC:{};tF:{};tT:{};" + 
																			"o:procedureStartDate.DESC;" + 
																	"pp:r01NavBarBlockSize.9,r01PageSize.10")
							   .customizeWith(targetPage,
									   	 	  repo,
									   	 	  cluster,family,type)
				 			   .asString();
		return outQry;
				
	}
	/**
	 * Procesar el XML de la sesión de búsqueda
	 * @param sessionXML el xml de la sessión 
	 */
	private static SessionData<ServiceData> _parseSearchSessionXML(final String sessionXML) throws SAXException,
																				   	  			   XPathExpressionException {
		// [1] - Parsear el XML devuelto
		Document xml = XMLUtils.parse(sessionXML);
		
		// [2] - Create a session object 
		SessionData<ServiceData> outSessionData = TestOpenDataPseudoRESTSearchAPIBase.createSessionData(xml.getDocumentElement());
		
		// [3] - download the file that contains the data-set specific meta-data
		//		 and parse it
		NodeList itemNodes = XMLUtils.nodeListByXPath(xml.getDocumentElement(),
													  "/searchSession/searchResultsBySource/searchSourceResults/results/item");
		Set<ServiceData> items = null;
		if (itemNodes != null && itemNodes.getLength() > 0) {
			items = new HashSet<ServiceData>(itemNodes.getLength());
			for (int i=0; i<itemNodes.getLength(); i++) {
				items.add(_parseSearchSessionResultItem(itemNodes.item(i)));
			}
		}
		outSessionData.setItems(items);
		
		// [4] - Devolver
		return outSessionData;
	}
	/**
	 * Procesar un item de resultado de la sesión de búsqueda
	 * @param itemNode
	 * @return los datos del item
	 */
	private static ServiceData _parseSearchSessionResultItem(final Node itemNode) throws XPathExpressionException {
		ServiceData outItem = new ServiceData();
		
		// Algunos metaDatos comunes
		// -------------------------
		String contentName = XMLUtils.stringByXPath(itemNode,
													"contentName/text()");
		String langVersionLanguage = XMLUtils.stringByXPath(itemNode,
														    "documentLanguage");
		String langVersionName = XMLUtils.stringByXPath(itemNode,
														"documentName");
		// Algunos meta-datos relevantes dependientes del tipo de contenido
		// pero que son devueltos por el buscador
		// ----------------------------------------------------------------
		String procStatus = XMLUtils.stringByXPath(itemNode,
												   "documentMetaData/procedureStatus/text()");		
		// MetaDatos específicos disponibles en un XML que hay que descargar de euskadi.net
		// --------------------------------------------------------------------------------
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
		
		// Assets reusables (ZIP con el contenido)
		// En el item se relaciona el nombre del asset con su ruta a partir de la raíz del contenido
		//	<contentRispDocumentsInfo>
		//		<zipThin><![CDATA[/opendata/ayudas_gabon_thin.zip,0,]]></zipThin>
		//		<zip><![CDATA[/opendata/ayudas_gabon.zip,0,]]></zip>
		//		<xml><![CDATA[/ayudas_gabon-idxContent.xml,12952,]]></xml>
		//	</contentRispDocumentsInfo>
		// ---------------------------------------
		Map<String,Path> assets = null;
		NodeList assetsNodes = XMLUtils.nodeListByXPath(itemNode,
														"contentRispDocumentsInfo/*");
		if (assetsNodes != null && assetsNodes.getLength() > 0) {
			assets = new HashMap<String,Path>(assetsNodes.getLength());
			for (int i=0; i<assetsNodes.getLength(); i++) {
				Node assetNode = assetsNodes.item(i);
				String assetId = assetNode.getNodeName();
				String assetPath = assetNode.getTextContent();
				assets.put(assetId,Path.of(assetPath));
			}
		}
		
		// Componer el objeto que encapsula los datos
		outItem.setContentName(contentName);
		outItem.setLang(langVersionLanguage);
		outItem.setLangVersionName(langVersionName);
		outItem.setProcedureStatus(procStatus);
		outItem.setDataFileXML(dataFileXML);
		outItem.setReusableAssets(assets);
		return outItem;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
//	ITEM FOR THE RETURNED DATA
///////////////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	static class ServiceData 
	  implements Debuggable {
		@Getter @Setter private String _contentName;
		@Getter @Setter private String _lang;
		@Getter @Setter private String _langVersionName;
		@Getter @Setter private String _procedureStatus;
		@Getter @Setter private Map<String,Path> _reusableAssets;
		@Getter @Setter private String _dataFileXML;
		
		@Override
		public String debugInfo() {
			return Strings.create().add("     Content Name: ").addLine(_contentName)
								   .add("Lang Version Name: ").add("(").add(_lang).add(")").addLine(_langVersionName)
								   .add(" Procedure status: ").addLine(_procedureStatus)
								   .add("  Reusable Assets: ").addLine((_reusableAssets != null ? Integer.toString(_reusableAssets.size()) : ""))
								   .add("     DataFile XML: ").add(_dataFileXML)
								   .asString();
		}
	}
}

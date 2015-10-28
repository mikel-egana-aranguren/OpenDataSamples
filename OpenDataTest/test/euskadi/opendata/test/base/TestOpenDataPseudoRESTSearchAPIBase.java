package euskadi.opendata.test.base;

import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import euskadi.opendata.test.base.TestOpenDataPseudoRESTSearchAPIBase.SessionData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.util.types.Strings;
import r01f.xml.XMLUtils;

public abstract class TestOpenDataPseudoRESTSearchAPIBase {
///////////////////////////////////////////////////////////////////////////////////////////////////
//	
///////////////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	protected static class SessionData<T>
	            implements Debuggable {
		@Getter @Setter private int _numberOfPages;
		@Getter @Setter private int _numberOfResults;
		@Getter @Setter private Set<T> _items;
		
		@Override
		public String debugInfo() {
			return Strings.of("Sesión de búsqueda: {} resultados en {} páginas")
						  .customizeWith(_numberOfResults,_numberOfPages)
						  .asString();
		}
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
//	
///////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a SessionData object from the raw XML
	 * @param sessionNode
	 * @return
	 * @throws XPathExpressionException
	 */
	public static <T> SessionData<T> createSessionData(final Node sessionNode) throws XPathExpressionException {
		Number numPages = XMLUtils.numberByXPath(sessionNode,
												 "/searchSession/searchResultsBySource/searchSourceResults/@numberOfPages");
		Number totalResults = XMLUtils.numberByXPath(sessionNode,
													 "/searchSession/searchResultsBySource/searchSourceResults/@numberOfResults");
		SessionData<T> outSessionData = new SessionData<T>();
		outSessionData.setNumberOfPages(numPages.intValue());
		outSessionData.setNumberOfResults(totalResults.intValue());
		return outSessionData;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
//	
///////////////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Extracts the path of the XML file that contains content-type dependent metadata
	 * (the data file)
	 * Buscar el datafile (esta parte NO estaba pensada para ser utilizad en OpenData, pero 
	 * es prácticamente la única opción que hay WTF!!!!)
	 * en el xml del item se relacionan los oids de los datafiles (que es lo que se necesita) con el fichero HTML
	 * generado
	 *  Ej:	<documentDataFilesGeneratedFilesDocumentRelativePaths>
	 *			<r01dpd013a780b8c401e41497a285409dc514c34e>
	 *				<![CDATA[ procedimiento_ayuda_v2;main:abandono_2010.html ]]>
	 *			</r01dpd013a780b8c401e41497a285409dc514c34e>
	 *		</documentDataFilesGeneratedFilesDocumentRelativePaths>
	 * (se necesita el oid: r01dpd013a780b8c401e41497a285409dc514c34e)
	 * @param itemNode
	 * @return
	 * @throws XPathExpressionException 
	 */
	public static String getContentTypeDependentMetaDataFileUrl(final Node itemNode) throws XPathExpressionException {
		String langVersionWorkAreaRelativePath = XMLUtils.stringByXPath(itemNode,
																		"documentWorkAreaRelativePath");
		String langVersionLanguage = XMLUtils.stringByXPath(itemNode,
														    "documentLanguage");		
		
		NodeList dataFileNodes = XMLUtils.nodeListByXPath(itemNode,
												      	  "documentDataFilesGeneratedFilesDocumentRelativePaths/*");
		String dataFileURL = null;
		if (dataFileNodes != null && dataFileNodes.getLength() > 0) {
			String dataFileOid = dataFileNodes.item(0)	// solo se coge el primero
										  	  .getNodeName();	
			dataFileURL = Strings.customized("http://www.euskadi.net/contenidos/{}/data/{}_{}",
											 langVersionWorkAreaRelativePath,
											 langVersionLanguage,dataFileOid.replace("datafileOid.",""));
		}
		return dataFileURL;
	}
}

package euskadi.opendata.test.datasets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import r01f.model.metadata.IndexableFieldID;
import r01f.model.search.query.EqualsQueryClause;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xml.XMLUtils;
import r01m.model.content.R01MContent;
import r01m.model.content.search.R01MSearchFilterForContent;
import r01m.model.content.search.R01MSearchResultsPresentation;
import r01m.model.oids.R01MStructuresOIDs.R01MStructureLabelOID;
import r01m.model.oids.R01MTypoOIDs.R01MTypoClusterID;
import r01m.model.oids.R01MTypoOIDs.R01MTypoFamilyID;
import r01m.model.oids.R01MTypoOIDs.R01MTypoTypeID;
import r01m.model.search.R01MSearchQueryOrderByMetaData;
import r01m.model.search.R01MSearchQueryOrderByMetaData.R01MSearchQueryOrderBy;

import com.ejie.r01m.objects.searchengine.results.R01MSearchResultItem;
import com.ejie.r01m.objects.searchengine.session.R01MSearchSession;
import com.google.common.collect.Maps;

import euskadi.opendata.model.OpenDataDataSet;
import euskadi.opendata.test.base.TestOpenDataSearchAPIBase;



/**
 * Simple Test for the OpenData SearchEngineAPI
 * Wraps the SearchEngine PSEUDO-REST-API using a JAVA API
 * Testing the rest url: http://www.euskadi.net/r01hSearchResultWar/r01hPresentationXML.jsp?r01kTgtPg=1&r01kSrchSrcId=contenidos.inter&r01kQry=tC:euskadi;tF:procedimientos_administrativos;tT:ayuda_subvencion;m:procedureStatus.EQ.16;o:createDate.ASC;cA:label1;pp:r01NavBarBlockSize.9,r01PageSize.10
 */
@Slf4j
public class TestSearchAPIForDataSets
	 extends TestOpenDataSearchAPIBase {
	
///////////////////////////////////////////////////////////////////////////////////////////////////
//	main
///////////////////////////////////////////////////////////////////////////////////////////////////	
	public static void main(String[] args) throws IOException {
		
		final FileWriter okWriter = new FileWriter(new File("d:/temp_dev/opendata/opendata.csv"));			// fichero al que escribir los datos
		final FileWriter errWriter = new FileWriter(new File("d:/temp_dev/opendata/opendata_err.log"));		// fichero al que escribir los datos
		try {
			okWriter.write(OpenDataDataSet.csvHEAD());	// write the header
					
			// [1] Construir la query
			R01MSearchFilterForContent qry = _buildQuery();
			log.debug("[SearchQuery]: " + qry.toCriteriaString());
			
			// [2] Inicializar la sesión de búsqueda
			R01MSearchSession session = R01MSearchSession.forQueryCustomizingResultsPresentation(qry,
																								 R01MSearchResultsPresentation.create()
																								 							  .withPageSizeOf(10));
			log.debug("[Resultados de la búsqueda: {} resultados en {} páginas de {} items (max) cada una",
					   session.getPager().getItemCount(),
					   session.getPager().getPageCount(),
					   session.getPager().getPageItems());
			
			// [3] Recoger resultados y paginar
			R01MSearchResultItem[] resultItems = session.getCurrentPageSearchResults();
			while (resultItems != null) {
				// Añadir a la colección
				if (CollectionUtils.hasData(resultItems)) {
					for (R01MSearchResultItem item : resultItems) {
						// transformar el result item en data set
						try {
							OpenDataDataSet itemData = _resultItemToData(item);
													
							_writeRow(okWriter,itemData);
						} catch(Throwable th) {							
							_writeError(errWriter,th);
						}
					}
				}				
				// Ir a la siguiente página de resultados
				resultItems = session.goToNextPage();
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
		} finally {
			try {
				okWriter.close();
				errWriter.close();
			} catch(IOException ioEx) {
				ioEx.printStackTrace(System.out);
			}
		}
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
//	BUILD QUERY
///////////////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Builds a searchQuery
	 * @return the SearchQuery
	 */
	private static R01MSearchFilterForContent _buildQuery() {
		R01MSearchFilterForContent qry = R01MSearchFilterForContent.createFor(R01MContent.class)
																	 .typedInAnyOfTheseClusters(R01MTypoClusterID.forId("euskadi"))
																	 .typedInAnyOfTheseFamilies(R01MTypoFamilyID.forId("opendata"))
																	 .typedInAnyOfTheseTypes(R01MTypoTypeID.forId("opendata"))
																	 .mustHaveStructureLabels(R01MStructureLabelOID.forIds("r01e00000ff26d46212a470b818464daec45c59fa","default"))
																	 .mustMeetThisMetaDataCondition(EqualsQueryClause.forField(IndexableFieldID.forId("documentLanguage"))
																			 										 .of("es"))
																	 .publishedItemsOnly()
																	 .orderedBy(R01MSearchQueryOrderByMetaData.forMetaData("createDate")
																			 								  .ordered(R01MSearchQueryOrderBy.ASCENDING));

		return qry;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Transformar los objetos {@link R01MSearchResultItem} devueltos por el buscador 
	 * en objetos {@link OpenDataDataSet}
	 * 		- MetaDatos comunes como el nombre del contenido y versión idiomática
	 * 		- MetaDatos específicos: XML del tipo de contenido
	 * @param items los items a procesar
	 * @return
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	private static OpenDataDataSet _resultItemToData(final R01MSearchResultItem item) throws IOException,
																							 SAXException, 
																							 XPathExpressionException {
		OpenDataDataSet outData = new OpenDataDataSet();
		// Algunos metaDatos comunes
		// -------------------------
		outData.setTypo(item.getContentTypology().getTypeOid());
		outData.setContentName(item.getContentName());
		outData.setLang(item.getDocumentLanguage());
		outData.setLangVersionName(Strings.of(item.getDocumentName())
										  .removeNewlinesOrCarriageRetuns()
										  .asString());
		outData.setUrlFicha("http://opendata.euskadi.net/w79-contdata/" + item.getDocumentLanguage() + "/" + 
							item.getDocumentWorkAreaRelativePath() + "/" + 
							item.getDocumentMainDataFileDefaultGeneratedFileDocumentRelativePath());
		
		// Catalogaciones
		// --------------
		Map<String,Map<String,String>> temasSubTemas = _labelsAtTemasSubTemas(item);
		if (CollectionUtils.hasData(temasSubTemas)) {
			if (temasSubTemas.containsKey("temas")) outData.setTemas(temasSubTemas.get("temas"));
			if (temasSubTemas.containsKey("subTemas")) outData.setSubTemas(temasSubTemas.get("subTemas"));
		} 
		if (outData.getTemas() == null) outData.setTemas(new HashMap<String,String>(0));
		if (outData.getSubTemas() == null) outData.setSubTemas(new HashMap<String,String>());
		
		// Algunos meta-datos relevantes dependientes del tipo de contenido
		// pero que son devueltos por el buscador
		// ----------------------------------------------------------------
		Map<String,?> indexedMD = item.getDocumentMetaData();
		if (CollectionUtils.hasData(indexedMD)) {
			if (indexedMD.get("OpendataURLHTML") != null) outData.setUrlFicha2(indexedMD.get("OpendataURLHTML").toString());
			if (indexedMD.get("statisticDiffusionDate") != null) outData.setFechaDifusion(indexedMD.get("statisticDiffusionDate").toString());
			if (indexedMD.get("OpendataReleaseDate") != null) outData.setFechaCreacion(indexedMD.get("OpendataReleaseDate").toString());
			if (indexedMD.get("OpendataLabels") != null) outData.setTags(indexedMD.get("OpendataLabels").toString());
			if (indexedMD.get("OpendataFormats") != null) outData.setFormats(indexedMD.get("OpendataFormats").toString());
			if (indexedMD.get("statisticOfficiality") != null) outData.setOficialidadEstadistica(indexedMD.get("statisticOfficiality").toString());
			if (indexedMD.get("OpendataSource") != null) outData.setSource(indexedMD.get("OpendataSource").toString());
			if (indexedMD.get("statisticRegularity") != null) outData.setPeriodicidad(indexedMD.get("statisticRegularity").toString());
			if (indexedMD.get("OpendataDatasets") != null) outData.setFiles(indexedMD.get("OpendataDatasets").toString());
		}
		
		// MetaDatos específicos disponibles en un XML que hay que descargar de euskadi.net
		// --------------------------------------------------------------------------------
			String dataFileXML = _downloadDataFileXML(item);
			if (dataFileXML != null) {
//				itemData.setDataFileXML(dataFileXML);
				
				Document xmlDoc = XMLUtils.parse(Strings.of(dataFileXML)
														.asInputStream());
				String periodoInicio = XMLUtils.stringByXPath(xmlDoc,"//opendata/detalle_datos/periodo_inicio");
				String periodoFin = XMLUtils.stringByXPath(xmlDoc,"//opendata/detalle_datos/periodo_fin");
				String descripcion = XMLUtils.stringByXPath(xmlDoc,"//opendata/datos_generales/descripcion");
				String tema = XMLUtils.stringByXPath(xmlDoc,"//opendata/detalle_datos/tema");
				
				if (periodoInicio != null) outData.setPeriodoInicio(periodoInicio);
				if (periodoFin != null) outData.setPeriodoFin(periodoFin);
				if (descripcion != null) outData.setDescription(Strings.of(descripcion)
																		.removeNewlinesOrCarriageRetuns()
																		.asString());
				if (tema != null) outData.setTema(Strings.of(tema)
														  .removeNewlinesOrCarriageRetuns()
														  .asString());
			}
			
		// Assets reusables (ZIP con el contenido)
		// ---------------------------------------
		Map<String,Path> assets = item.getReusableAssets();
		if (CollectionUtils.hasData(assets)) outData.setReusableAssets(assets);
		
		// Debug:
//				System.out.println("\n\n\n________________________________________________________________________________________________\n" + 
//								   itemData.debugInfo());
		return outData;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static Map<String,Map<String,String>> _labelsAtTemasSubTemas(final R01MSearchResultItem item) {
		Map<String,Map<String,String>> outCats = null; 
		if (CollectionUtils.hasData(item.getStructureCatalogs())) {
			// -- Temas
			Map<String,String> temas = _labelsInTemaSubTemaStructure(item.getStructureCatalogs(),
														  			 "1");			// temas
			// -- Subtemas
			Map<String,String> subTemas = _labelsInTemaSubTemaStructure(item.getStructureCatalogs(),
					   										 			"2");		// subtemas
			if (CollectionUtils.hasData(temas) || CollectionUtils.hasData(subTemas)) {
				int size = CollectionUtils.hasData(temas) && CollectionUtils.hasData(subTemas) ? 2 : 1;				
				outCats = Maps.newHashMapWithExpectedSize(size);
				
				if (CollectionUtils.hasData(temas)) outCats.put("temas",temas);
				if (CollectionUtils.hasData(subTemas)) outCats.put("subTemas",subTemas);
			}

		}
		return outCats == null ? new HashMap<String,Map<String,String>>(0)
							   : outCats;
	}
}

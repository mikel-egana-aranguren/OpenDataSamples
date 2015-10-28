package euskadi.opendata.test.servicios;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import r01f.model.metadata.IndexableFieldID;
import r01f.model.search.query.EqualsQueryClause;
import r01f.types.Path;
import r01f.util.types.collections.CollectionUtils;
import r01m.model.content.R01MContent;
import r01m.model.content.search.R01MSearchFilterForContent;
import r01m.model.oids.R01MStructuresOIDs.R01MStructureLabelOID;
import r01m.model.oids.R01MTypoOIDs.R01MTypoClusterID;
import r01m.model.oids.R01MTypoOIDs.R01MTypoFamilyID;
import r01m.model.oids.R01MTypoOIDs.R01MTypoTypeID;
import r01m.model.search.R01MSearchQueryOrderByMetaData;
import r01m.model.search.R01MSearchQueryOrderByMetaData.R01MSearchQueryOrderBy;
import rx.Observer;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func1;

import com.ejie.r01m.objects.searchengine.results.R01MSearchResultItem;
import com.ejie.r01m.objects.searchengine.session.R01MSearchSession;

import euskadi.opendata.model.ServiceData;
import euskadi.opendata.test.base.TestOpenDataSearchAPIBase;



/**
 * Simple Test for the OpenData SearchEngineAPI
 * Wraps the SearchEngine PSEUDO-REST-API using a JAVA API
 * Testing the rest url: http://www.euskadi.net/r01hSearchResultWar/r01hPresentationXML.jsp?r01kTgtPg=1&r01kSrchSrcId=contenidos.inter&r01kQry=tC:euskadi;tF:procedimientos_administrativos;tT:ayuda_subvencion;m:procedureStatus.EQ.16;o:createDate.ASC;cA:label1;pp:r01NavBarBlockSize.9,r01PageSize.10
 */
@Slf4j
public class TestSearchAPIForServicios 
	 extends TestOpenDataSearchAPIBase {
///////////////////////////////////////////////////////////////////////////////////////////////////
//	main
///////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) throws IOException {
		
		final FileWriter okWriter = new FileWriter(new File("d:/temp_dev/opendata/servicios.csv"));			// fichero al que escribir los datos
		final FileWriter errWriter = new FileWriter(new File("d:/temp_dev/opendata/servicios_err.log"));	// fichero al que escribir los datos
		try {
			okWriter.write(ServiceData.csvHEAD());	// write the header
			
			// [1] Construir la query
			R01MSearchFilterForContent qry = _buildQuery();
			
			log.debug("[SearchQuery]: " + qry.toCriteriaString());
			
			
			// [2] Inicializar la sesión
			R01MSearchSession.resultItemStreamForQuery(qry)
							 .map(new Func1<R01MSearchResultItem,ServiceData>() {
											@Override
											public ServiceData call(final R01MSearchResultItem item) {
												try {
													return _resultItemToServiceData(item);
												} catch(IOException ioEx) {
													throw OnErrorThrowable.from(ioEx);
												}
											}
							        })
							 .subscribe(new Observer<ServiceData>() {
												@Override
												public void onCompleted() {
													System.out.println("---->End!");
												}
												@Override
												public void onNext(final ServiceData serviceData) {
													_writeRow(okWriter,
															  serviceData);
												}
												@Override
												public void onError(final Throwable th) {
													_writeError(errWriter,
															    th);
												}
							  			});
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
											 .typedInAnyOfTheseFamilies(R01MTypoFamilyID.forId("procedimientos_administrativos"))
											 .typedInAnyOfTheseTypes(R01MTypoTypeID.forId("ayuda_subvencion"))
											 .mustMeetThisMetaDataCondition(EqualsQueryClause.forField(IndexableFieldID.forId("procedureStatus"))
										 													 .of(16))
											 .mustHaveStructureLabels(R01MStructureLabelOID.forIds("r01e00000ff26d46212a470b818464daec45c59fa","default"))
											 .orderedBy(R01MSearchQueryOrderByMetaData.forMetaData("createDate")
													 								  .ordered(R01MSearchQueryOrderBy.ASCENDING));

		return qry;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Procesar los resultados de búsqueda: acceder a algunos datos:
	 * 		- MetaDatos comunes como el nombre del contenido y versión idiomática
	 * 		- MetaDatos específicos: XML del tipo de contenido
	 * @param items los items a procesar
	 * @return
	 */
	private static ServiceData _resultItemToServiceData(final R01MSearchResultItem item) throws IOException {
		ServiceData itemData = new ServiceData();
		
		// Algunos metaDatos comunes
		// -------------------------
		itemData.setContentName(item.getContentName());
		itemData.setLang(item.getDocumentLanguage());
		itemData.setLangVersionName(item.getDocumentName());
		
		// Algunos meta-datos relevantes dependientes del tipo de contenido
		// pero que son devueltos por el buscador
		// ----------------------------------------------------------------
		Map<String,?> indexedMD = item.getDocumentMetaData();
		itemData.setProcedureStatus(indexedMD != null ? indexedMD.get("procedureStatus").toString() : null);
		
		// MetaDatos específicos disponibles en un XML que hay que descargar de euskadi.net
		// --------------------------------------------------------------------------------
		String dataFileXML = _downloadDataFileXML(item);
		if (dataFileXML != null) itemData.setDataFileXML(dataFileXML);
		
		// Assets reusables (ZIP con el contenido)
		// ---------------------------------------
		Map<String,Path> assets = item.getReusableAssets();
		if (CollectionUtils.hasData(assets)) itemData.setReusableAssets(assets);
		
		return itemData;
	}
}

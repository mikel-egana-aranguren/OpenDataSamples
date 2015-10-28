package euskadi.opendata.test.meteo;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import r01f.debug.Debuggable;
import r01f.httpclient.HttpClient;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xml.XMLUtils;





/**
 * Simple Test for OpenData data consuming using METEO DATA parsing data the hard way
 * 
 * http://opendata.euskadi.eus/w79-contdata/es/contenidos/ds_meteorologicos/forecast_ds_2013/es_dataset/ficha.html
 * 		- Contenido: http://opendata.euskadi.eus/contenidos/prevision_tiempo/met_forecast/es_today/web.html
 * 		- XML Descriptor: http://opendata.euskadi.eus/contenidos/prevision_tiempo/met_forecast_zone/met_forecast_zone-idxContent.xml
 * 		- ZIP OpenData: http://opendata.euskadi.net/contenidos/prevision_tiempo/met_forecast/opendata/met_forecast_thin.zip
 * 	 	- Datos Reutilizables: 
 * 			- Por ciudadaes: http://opendata.euskadi.eus/contenidos/prevision_tiempo/met_forecast/opendata/met_forecast.xml
 * 			- Tendencias: http://opendata.euskadi.eus/contenidos/tendencias/met_tendency/opendata/met_tendency.xml
 * 			- Por comarcas: http://opendata.euskadi.eus/contenidos/prevision_tiempo/met_forecast_zone/opendata/met_forecast_zone.xml
 * 			- DataFile: http://www.euskadi.eus/contenidos/prevision_tiempo/met_forecast/es_today/data/es_r01dpd0131f5f95ec01aaace4b381c1d5ec19b199
 */
@Slf4j
public class TestOpenDataMeteoRaw {
///////////////////////////////////////////////////////////////////////////////////////////////////
//	main
///////////////////////////////////////////////////////////////////////////////////////////////////	
	public static void main(String[] args) {
		try {
			// [1] - Descargar el XML con el DataSet
			String todayForeCastXML = "http://opendata.euskadi.eus/contenidos/prevision_tiempo/met_forecast/opendata/met_forecast.xml";
			System.out.println("[1] DataURL: " + todayForeCastXML);
			String xmlData = HttpClient.forUrl(todayForeCastXML)
									   .GET()
									   .loadAsString();
			System.out.println("[2] Data XML: " + xmlData);			
			
			// [2] - Parsear el XML y obtener los datos 
			Map<String,TodayForeCast> cityData = _parseData(xmlData);
			if (CollectionUtils.hasData(cityData)) {
				for (TodayForeCast cityForeCast : cityData.values()) {
					System.out.println(Strings.of("\n\n___________________________\n{}")
											  .customizeWith(cityForeCast.debugInfo()));
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
		}
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
//	EXTRACION DE DATOS
///////////////////////////////////////////////////////////////////////////////////////////////////
	private static Map<String,TodayForeCast> _parseData(final String xml) throws SAXException,
																		  		 XPathExpressionException {
		// [1] - Parsear el XML devuelto
		Document xmlDoc = XMLUtils.parse(xml);
		
		// [2] - Ir obteniendo datos utilizando XPath
		NodeList cityNodes = XMLUtils.nodeListByXPath(xmlDoc.getDocumentElement(),
												      "/weatherForecast/forecasts/forecast[@forecastDay='today']/cityForecastDataList/cityForecastData");
		Map<String,TodayForeCast> outCities = null;
		if (cityNodes != null && cityNodes.getLength() > 0) {
			outCities = new HashMap<String,TodayForeCast>(cityNodes.getLength());
			for (int i=0; i<cityNodes.getLength(); i++) {
				Node cityNode = cityNodes.item(i);
				String cityName = XMLUtils.stringByXPath(cityNode,
														 "@cityName");
				String tempMax = XMLUtils.stringByXPath(cityNode,
														 "tempMax");
				String tempMin = XMLUtils.stringByXPath(cityNode,
														 "tempMin");
				String description_es = XMLUtils.stringByXPath(cityNode,
															   "symbol/descriptions/es");
				String description_eu = XMLUtils.stringByXPath(cityNode,
															   "symbol/descriptions/eu");
				
				TodayForeCast cityData = new TodayForeCast();
				cityData.setCity(cityName);
				cityData.setTempMax(tempMax);
				cityData.setTempMin(tempMin);
				cityData.setDescriptionEs(description_es);
				cityData.setDescriptionEu(description_eu);
				
				outCities.put(cityName,cityData);
			}
		} else {
			log.error("NO data");
		}
		return outCities;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
//	DATA
///////////////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	static class TodayForeCast 
	  implements Debuggable {
		@Getter @Setter private String _city;
		@Getter @Setter private String _tempMax;
		@Getter @Setter private String _tempMin;
		@Getter @Setter private String _descriptionEs;
		@Getter @Setter private String _descriptionEu;
		
		@Override
		public CharSequence debugInfo() {
			return Strings.of("Ciudad: {} > rango de temperaturas {}-{} > {}")
						  .customizeWith(_city,_tempMax,_tempMin,_descriptionEs);
		}
		
		
		
	}
}

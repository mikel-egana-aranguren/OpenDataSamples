package euskadi.opendata.test.meteo;

import java.io.InputStream;

import euskadi.opendata.model.meteo.Forecast;
import r01f.httpclient.HttpClient;
import r01f.marshalling.Marshaller;
import r01f.marshalling.simple.SimpleMarshallerBuilder;

/**
 * Simple Test for OpenData data consuming using METEO DATA parsing data using XML-Objects mapping (XO marshalling)
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
public class TestOpenDataMeteoMarshalling 
	 extends TestOpenDataMeteoBase {
	
	private static final String DATA_URL = "http://opendata.euskadi.net/contenidos/prevision_tiempo/met_forecast/opendata/met_forecast.xml";
	
	public static void main(String[] args) {
		try {
			// load the data xml using an http connection
			InputStream xmlIs = HttpClient.forUrl(DATA_URL)
					  					  .GET()
					  					  .getResponse()
					  					  .loadAsStream();
			// marshall the xml to java objects
			Marshaller marshaller = SimpleMarshallerBuilder.createForPackages(Forecast.class.getPackage().getName())
														   .getForSingleUse();
			Forecast forecast = marshaller.beanFromXml(xmlIs);
			
			// print debug info
			_printDebugInfo(forecast);

			
		} catch(Throwable th) {
			th.printStackTrace(System.out);
		}
	}
}

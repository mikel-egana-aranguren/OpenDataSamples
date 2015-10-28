package euskadi.opendata.test.meteo;

import com.google.inject.Guice;
import com.google.inject.Injector;

import euskadi.opendata.internal.meteo.MeteoOpenDataBootstrapGuiceModule;
import euskadi.opendata.model.meteo.Forecast;
import euskadi.opendata.service.meteo.WeatherForecastService;

public class TestOpenDataMeteoService
	 extends TestOpenDataMeteoBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		try {
			// get the guice injector
			Injector injector = Guice.createInjector(new MeteoOpenDataBootstrapGuiceModule());
			
			// get the weather service
			WeatherForecastService meteoService = injector.getInstance(WeatherForecastService.class);
			Forecast forecast = meteoService.forecast();
			
			// print debug info
			_printDebugInfo(forecast);
		} catch(Throwable th) {
			th.printStackTrace(System.out);
		}
	}
}

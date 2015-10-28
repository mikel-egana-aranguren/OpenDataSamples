package euskadi.opendata.service.meteo;

import java.io.InputStream;

import javax.inject.Singleton;

import euskadi.opendata.model.meteo.City;
import euskadi.opendata.model.meteo.Day;
import euskadi.opendata.model.meteo.Forecast;
import euskadi.opendata.model.meteo.ForecastForDay;
import euskadi.opendata.model.meteo.ForecastForLocation;
import euskadi.opendata.model.meteo.ForecastForLocationSummary;
import lombok.Cleanup;
import r01f.httpclient.HttpClient;
import r01f.locale.Language;
import r01f.marshalling.Marshaller;
import r01f.marshalling.simple.SimpleMarshallerBuilder;
import r01f.patterns.Memoized;
import r01f.types.Path;

@Singleton
public class WeatherForecastServiceImpl 
  implements WeatherForecastService {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final String DATA_URL = "http://opendata.euskadi.eus/contenidos/prevision_tiempo/met_forecast/opendata/met_forecast.xml";
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A memoized forecast
	 */
	private Memoized<Forecast> _forecast = new Memoized<Forecast>() {
													@Override @SuppressWarnings("resource")
													protected Forecast supply() {
														try {
															// load the data xml using an http connection
															@Cleanup InputStream xmlIs = HttpClient.forUrl(DATA_URL)
																	  					  		   .GET()
																	  					  		   .getResponse()
																	  					  		   .loadAsStream();
															
															// marshall the xml to java objects
															Marshaller marshaller = SimpleMarshallerBuilder.createForPackages(Forecast.class.getPackage().getName())
																											.getForSingleUse();
															Forecast outForecast = marshaller.beanFromXml(xmlIs);
															return outForecast;
														} catch (Throwable th) {
															th.printStackTrace(System.out);
														}
														return null;
													}
										   };
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Forecast forecast() {
		return _forecast.get();
	}
	@Override
	public ForecastForLocation forecastFor(final City city,
										   final Day day) {
		if (city == null || day == null) throw new IllegalArgumentException("Either the city code or the day are NOT valid");
		
		// Get the forecast for the given day
		ForecastForDay dayForecast = _forecastAt(day);
		
		// Get the city forecast
		ForecastForLocation locationForecast = dayForecast.getForecastForCity(city);
		if (locationForecast == null) throw new IllegalArgumentException("There's NO forecast for " + city);
		
		return locationForecast;
	}
	@Override
	public String summaryAt(final Day day,final Language lang) {
		if (day == null || lang == null) throw new IllegalArgumentException("Either the day or the language are NOT valid");
		
		// Get the forecast for the given day
		ForecastForDay dayForecast = _forecastAt(day);
		
		return dayForecast.getDescriptionIn(lang);
	}
	@Override
	public ForecastForLocationSummary forecastSumaryFor(final City city,
														final Day day,
														final Language lang) {
		if (city == null || day == null || lang == null) throw new IllegalArgumentException("Either the city code or the day or the language are NOT valid");
		
		// Get the forecast for the given day
		ForecastForDay dayForecast = _forecastAt(day);
		
		// Get the forecast for the given location at the given day
		ForecastForLocation locForecast = this.forecastFor(city,
														   day);

		// ... mix both
		ForecastForLocationSummary outForecast = new ForecastForLocationSummary();
		outForecast.setCity(locForecast.getCity());
		outForecast.setDate(dayForecast.getDate());
		outForecast.setDay(day);
		outForecast.setSummary(dayForecast.getDescriptionIn(lang));
		outForecast.setTempMin(locForecast.getTempMin());
		outForecast.setTempMax(locForecast.getTempMax());
		if (locForecast.getSymbol() != null) {
			outForecast.setSymbolPath(Path.of(locForecast.getSymbol().getImagePath()));
			outForecast.setSymbolDescription(locForecast.getSymbol().getDescriptionIn(lang));
		}
		return outForecast;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the forecast at a given day
	 * @param day
	 * @return
	 */
	private ForecastForDay _forecastAt(final Day day) {
		// Get the forecast
		Forecast forecast = _forecast.get();
		if (forecast == null) throw new IllegalStateException("Could NOT load the forecast from OpenData euskadi!!!");
		
		// Get the day forecast
		ForecastForDay dayForecast = forecast.getDayForecasts(day);
		if (dayForecast == null) throw new IllegalArgumentException("There's NO forecast for " + day);
		
		return dayForecast;
	}

}

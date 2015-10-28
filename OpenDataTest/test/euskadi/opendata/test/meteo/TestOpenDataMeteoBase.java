package euskadi.opendata.test.meteo;

import euskadi.opendata.model.meteo.City;
import euskadi.opendata.model.meteo.Day;
import euskadi.opendata.model.meteo.Forecast;
import r01f.locale.Language;
import r01f.util.types.Strings;

abstract class TestOpenDataMeteoBase {
	/**
	 * Simply prints some debug info
	 * @param forecast
	 */
	protected static void _printDebugInfo(final Forecast forecast) {
		// some debug
		String dbg = Strings.of("The forecast for {} {} is:\n" +
								"\t{}\n" +  
								"\t-Temperature: {}\n" +
								"\t-    Resumen: {}")
							 .customizeWith(City.BILBAO,Day.TODAY,
									 		forecast.getDayForecasts(Day.TODAY).getDescriptionIn(Language.SPANISH),
									 		forecast.getDayForecasts(Day.TODAY).getForecastForCity(City.BILBAO).getTemperatureRange(),
									 		forecast.getDayForecasts(Day.TODAY).getForecastForCity(City.BILBAO).getSymbol().getDescriptionIn(Language.SPANISH))
							 .asString();
		System.out.println(dbg);		
	}
}

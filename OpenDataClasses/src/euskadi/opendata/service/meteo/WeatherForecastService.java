package euskadi.opendata.service.meteo;

import euskadi.opendata.model.meteo.City;
import euskadi.opendata.model.meteo.Day;
import euskadi.opendata.model.meteo.Forecast;
import euskadi.opendata.model.meteo.ForecastForDay;
import euskadi.opendata.model.meteo.ForecastForLocation;
import euskadi.opendata.model.meteo.ForecastForLocationSummary;
import r01f.locale.Language;

public interface WeatherForecastService {
	/**
	 * @return the full weather forecast
	 */
	public Forecast forecast();
	/**
	 * Returns the forecast for a location on a given day
	 * @param city
	 * @param day
	 * @return
	 */
	public ForecastForLocation forecastFor(final City city,
										   final Day day);
	/**
	 * @return the forecast summary at a given day
	 */
	public String summaryAt(final Day day,final Language lang);
	/**
	 * Returns a "mix" of the {@link ForecastForLocation} and the {@link ForecastForDay} summary
	 * @param city
	 * @param day
	 * @return
	 */
	public ForecastForLocationSummary forecastSumaryFor(final City city,
														final Day day,
														final Language lang);
}

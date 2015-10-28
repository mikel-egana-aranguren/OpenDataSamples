package euskadi.opendata.model.meteo;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.locale.Language;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.marshalling.annotations.XmlDateFormat;
import r01f.types.Path;
import r01f.util.types.collections.CollectionUtils;

@XmlRootElement(name="weatherForecast")
@Accessors(prefix="_")
public class Forecast {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The date the prediction was made
	 */
	@XmlAttribute(name="doneDate") @XmlDateFormat("dd/MM/yyyy")
	@Getter @Setter private Date _date;
	/**
	 * The forecast for TODAY, TOMORROW and NEXT days
	 */
	@XmlElementWrapper(name="forecasts") @XmlElement(name="forecast")
	@Getter @Setter private Map<String,ForecastForDay> _forecasts;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the forecast for a given day: TODAY, TOMORROW, NEXT days
	 * @param dayStr
	 * @return
	 */
	public ForecastForDay getDayForecasts(final String dayStr) {
		ForecastForDay outForecast = CollectionUtils.hasData(_forecasts) ? _forecasts.get(dayStr)
																	  	 : null;
		return outForecast;
	}
	/**
	 * Returns the forecast for a given day: TODAY, TOMORROW, NEXT days
	 * @param day
	 * @return
	 */
	public ForecastForDay getDayForecasts(final Day day) {
		return this.getDayForecasts(day.getCode());
	}
}

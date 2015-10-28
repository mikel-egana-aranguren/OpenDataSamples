package euskadi.opendata.model.meteo;

import java.util.Collection;
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
import r01f.marshalling.annotations.OidField;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.marshalling.annotations.XmlDateFormat;
import r01f.types.Path;
import r01f.util.types.collections.CollectionUtils;

/**
 * A forecast for today, tomorrow or next
 */
@XmlRootElement(name="forecast")
@Accessors(prefix="_")
public class ForecastForDay {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The id of the prediction date
	 */
	@XmlAttribute(name="forecastDay") @OidField 
	@Getter @Setter private String _dateId;
	/**
	 * The date the prediction refers to
	 */
	@XmlAttribute(name="forecastDate") @XmlDateFormat("dd/MM/yyyy")
	@Getter @Setter private Date _date;
	/**
	 * A textual description of the forecast
	 */
	@XmlElement(name="forecastDateText")
	@Getter @Setter private String _foreCastDateText;
	/**
	 * The path of the map 
	 */
	@XmlElement(name="imageMap") @XmlCDATA
	@Getter @Setter private Path _mapImage;
	/**
	 * The forecast summary 
	 */
	@XmlElementWrapper(name="description") @XmlCDATA
	@Getter @Setter private Map<Language,String> _descriptions;
	/**
	 * Map symbol list
	 */
	@XmlElementWrapper(name="mapSymbolList")
	@Getter @Setter private Collection<ForecastForDayMapSymbol> _mapSymbols;
	/**
	 * A list of cities with their forecast 
	 */
	@XmlElementWrapper(name="cityForecastDataList") @XmlElement(name="cityForecastData")
	@Getter @Setter private Map<String,ForecastForLocation> _locations;
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the forecast for a location given it's code
	 * @param cityCode
	 * @return
	 */
	public ForecastForLocation getForecastForCityCode(final String cityCode) {
		ForecastForLocation outForecast = CollectionUtils.hasData(_locations) ? _locations.get(cityCode)
																			  : null;
		return outForecast;
	}
	/**
	 * Returns the forecast for a given location 
	 * @param city
	 * @return
	 */
	public ForecastForLocation getForecastForCity(final City city) {
		return this.getForecastForCityCode(city.getCode());
	}
	/**
	 * The description in a given language
	 * @param lang
	 * @return
	 */
	public String getDescriptionIn(final Language lang) {
		return CollectionUtils.hasData(_descriptions) ? _descriptions.get(lang)
													  : null;
	}
}

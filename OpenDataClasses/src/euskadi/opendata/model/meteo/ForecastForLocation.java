package euskadi.opendata.model.meteo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.marshalling.annotations.OidField;

import com.google.common.collect.Range;

/**
 * A forecast for a location
 */
@XmlRootElement(name="cityForecastData")
@Accessors(prefix="_")
public class ForecastForLocation {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The code for the city
	 */
	@XmlAttribute(name="cityCode") @OidField
	@Getter @Setter private String _locCode;
	/**
	 * The city name 
	 */
	@XmlAttribute(name="cityName")
	@Getter @Setter private String _locName;
	/**
	 * The max temperature 
	 */
	@XmlElement(name="tempMax")
	@Getter @Setter private int _tempMax;
	/**
	 * The min temperature
	 */
	@XmlElement(name="tempMin")
	@Getter @Setter private int _tempMin;
	/**
	 * The prediction icon (a cloud, a sun, rain, etc) 
	 */
	@XmlElement(name="symbol")
	@Getter @Setter private ForecastForLocationSymbol _symbol;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return a {@link Range} with the temperature
	 */
	public Range<Integer> getTemperatureRange() {
		return Range.closed(_tempMin,_tempMax);
	}
	/**
	 * @return the {@link City}
	 */
	public City getCity() {
		return City.fromCode(_locCode);
	}
}

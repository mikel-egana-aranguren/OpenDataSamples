package euskadi.opendata.model.meteo;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.marshalling.annotations.XmlDateFormat;
import r01f.model.ModelObject;
import r01f.types.Path;

import com.google.common.collect.Range;

/**
 * A forecast for a location
 */
@XmlRootElement(name="forecastForLocation")
@Accessors(prefix="_")
public class ForecastForLocationSummary 
  implements ModelObject {

	private static final long serialVersionUID = -4025297819421505910L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The day
	 */
	@XmlAttribute(name="day")
	@Getter @Setter private Day _day;
	/**
	 * The Date
	 */
	@XmlAttribute(name="date") @XmlDateFormat("dd/MM/yyyy")
	@Getter @Setter private Date _date;
	/**
	 * The city
	 */
	@XmlAttribute(name="city")
	@Getter @Setter private City _city;
	/**
	 * A prediction summary
	 */
	@XmlElement(name="summary") @XmlCDATA
	@Getter @Setter private String _summary;
	/**
	 * The max temperature 
	 */
	@XmlAttribute(name="tempMax")
	@Getter @Setter private int _tempMax;
	/**
	 * The min temperature
	 */
	@XmlAttribute(name="tempMin")
	@Getter @Setter private int _tempMin;
	/**
	 * The prediction icon (a cloud, a sun, rain, etc) 
	 */
	@XmlElement(name="symbolPath")
	@Getter @Setter private Path _symbolPath;
	/**
	 * The prediction icon description
	 */
	@XmlElement(name="symbolDescription") @XmlCDATA
	@Getter @Setter private String _symbolDescription;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return a {@link Range} with the temperature
	 */
	public Range<Integer> getTemperatureRange() {
		return Range.closed(_tempMin,_tempMax);
	}
}
 
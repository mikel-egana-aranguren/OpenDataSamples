package euskadi.opendata.model.meteo;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.locale.Language;
import r01f.util.types.collections.CollectionUtils;

/**
 * A forecast symbol
 */
@XmlRootElement(name="symbol")
@Accessors(prefix="_")
public class ForecastForLocationSymbol {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The absolute path from opendata.euskadi.net where the forecast icon is located
	 */
	@XmlElement(name="symbolImage")
	@Getter @Setter private String _imagePath;
	/**
	 * The icon description 
	 */
	@XmlElementWrapper(name="descriptions")
	@Getter @Setter private Map<Language,String> _descriptions;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The icon description in a given language
	 * @param lang
	 * @return
	 */
	public String getDescriptionIn(final Language lang) {
		return CollectionUtils.hasData(_descriptions) ? _descriptions.get(lang)
													  : null;
	}
}

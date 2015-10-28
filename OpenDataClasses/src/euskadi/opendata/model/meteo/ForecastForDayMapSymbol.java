package euskadi.opendata.model.meteo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.types.Path;

@XmlRootElement(name="mapSymbol")
@Accessors(prefix="_")
public class ForecastForDayMapSymbol {
	@XmlElement(name="positionX")
	@Getter @Setter private double _positionX;
	
	@XmlElement(name="positionY")
	@Getter @Setter private double _positionY;
	
	@XmlElement(name="width")
	@Getter @Setter private float _width;
	
	@XmlElement(name="heigth")
	@Getter @Setter private float _height;
	
	@XmlElement(name="symbolImage")
	@Getter @Setter private Path _imagePath;
}

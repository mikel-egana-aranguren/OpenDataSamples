package euskadi.opendata.meteo.rest.resources;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import euskadi.opendata.model.meteo.City;
import euskadi.opendata.model.meteo.Day;
import euskadi.opendata.model.meteo.ForecastForLocationSummary;
import euskadi.opendata.service.meteo.WeatherForecastService;
import lombok.experimental.Accessors;
import r01f.locale.Language;
import r01f.locale.Languages;

@Path("weatherforecasts")
@Singleton
@Accessors(prefix="_")
public class MeteoOpenDataRESTResource {
/////////////////////////////////////////////////////////////////////////////////////////
//  INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	private WeatherForecastService _weatherService;
	
//	@Inject @ModelObjectsMarshaller
//	private Marshaller _marshaller;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  INJECTED STATUS PER REQUEST
/////////////////////////////////////////////////////////////////////////////////////////
	@Context
	private HttpServletRequest _req;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  WEATHER FORECAST FOR A CITY
// 	Ej: http://localhost:8080/OpenDataMeteoWar/weatherforecasts/2/today?lang=SPANISH
/////////////////////////////////////////////////////////////////////////////////////////
	@GET @Path("{cityCode}/{when}") 
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_XHTML_XML})	
	public Response forecastForCityInXML(@PathParam("cityCode") final String cityCode,
										 @PathParam("when") 	final String when,
										 @QueryParam("lang")    final String lang,
										 @QueryParam("format")	final String format) {
		if (cityCode == null || when == null) throw new IllegalArgumentException("Either the city code or the day are NOT valid");
		if (lang != null && !Languages.canBe(lang)) throw new IllegalArgumentException("The language " + lang + " is NOT a valid language");
		

		// Get the city and day
		City city = City.fromCode(cityCode);
		Day day = Day.fromCode(when);
		Language theLang = lang == null ? Language.DEFAULT
										: Language.fromName(lang);
		MediaType mediaType = (format != null && format.equals("xml")) ? MediaType.APPLICATION_XML_TYPE
																	   : MediaType.APPLICATION_XHTML_XML_TYPE;
		
		// Get the forecast
		ForecastForLocationSummary locSummary = _weatherService.forecastSumaryFor(city,
																				  day,
																				  theLang);
		
		// Build a response
		Object entity = null;
		if (mediaType == MediaType.APPLICATION_XML_TYPE) {
			entity = locSummary;					// the entity will be converted to XML at the ResponseTypeMapper
		} else {
			entity = _forecastToHTML(locSummary);	// the entity is converted to HTML here
		}
		Response outResponse = Response.ok()
									   .entity(entity)
									   .type(mediaType)
									   .build();
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static String _forecastToHTML(final ForecastForLocationSummary forecast) {
		String symbolPath = "http://opendata.euskadi.net" + forecast.getSymbolPath().asAbsoluteString();
		
		StringBuilder outHTML = new StringBuilder(1000);
		outHTML.append("<div class='forecast'>\n")
			   .append("\t<p class='lead'>").append(forecast.getSummary()).append("</p>\n")
			   .append("\t<img src='").append(symbolPath).append("' alt='").append(forecast.getSymbolDescription()).append("'/>\n")
			   .append("\t<span class='temp'>").append(forecast.getTempMin()).append(" - ").append(forecast.getTempMax()).append("</span>\n")
			   .append("</div>");
		return outHTML.toString();
	}
}

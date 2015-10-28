package euskadi.opendata.meteo.internal;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.core.Application;

import lombok.NoArgsConstructor;
import euskadi.opendata.meteo.rest.resources.MeteoOpenDataRESTResource;
import euskadi.opendata.meteo.rest.resources.MeteoOpenDataResponseMappers.MeteoOpendataModelObjectResponseTypeMapper;
import euskadi.opendata.meteo.rest.resources.MeteoOpenDataResponseMappers.MeteoOpendataUncaughtExceptionMapper;


/**
 * Rest app referenced at {@link R01NIRESTJerseyServletGuiceModule} (Guice is in use)
 * in order to load the REST resources
 * 
 * <pre>
 * NOTE:	If Guice was not used, the REST App should be defined in WEB-INF/web.xml
 * </pre>	
 */
@Singleton
@NoArgsConstructor
public class MeteoOpenDataRESTApp 
     extends Application {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////

	
///////////////////////////////////////////////////////////////////////////////
// METHODS
///////////////////////////////////////////////////////////////////////////////	
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		
		// rest Resources 
		s.add(MeteoOpenDataRESTResource.class);
		
		// Request type mappers: transforms Java->XML for REST methods returned types
		s.add(MeteoOpendataModelObjectResponseTypeMapper.class);
	
		// Exception Mappers
		s.add(MeteoOpendataUncaughtExceptionMapper.class);
		
		return s;
	}
}

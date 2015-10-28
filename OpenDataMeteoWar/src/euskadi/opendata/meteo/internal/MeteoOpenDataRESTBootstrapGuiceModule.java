package euskadi.opendata.meteo.internal;

import java.util.HashMap;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

@Slf4j
@EqualsAndHashCode(callSuper=true)		// This is important for guice modules
public class MeteoOpenDataRESTBootstrapGuiceModule 
     extends JerseyServletModule {

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureServlets() {
		log.warn("[START]=======REST Bootstraping ======");
		
		// Route all requests through GuiceContainer
		// IMPORTANT!
		//		If this property is NOT defined, GUICE will try to crate REST resource instances
		//		for every @Path annotated types defined at the injector
		Map<String,String> params = new HashMap<String,String>();
		params.put("javax.ws.rs.Application",
				   MeteoOpenDataRESTApp.class.getName());
		
		serve("/*").with(GuiceContainer.class,
						 params);
		
		log.warn("  [END]=======REST Bootstraping ======");
	}
}

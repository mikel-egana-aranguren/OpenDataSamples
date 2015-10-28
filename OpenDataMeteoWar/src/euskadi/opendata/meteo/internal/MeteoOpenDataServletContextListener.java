package euskadi.opendata.meteo.internal;

import javax.servlet.ServletContextListener;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import euskadi.opendata.internal.meteo.MeteoOpenDataBootstrapGuiceModule;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link ServletContextListener} in charge of:
 */
@Slf4j
public class MeteoOpenDataServletContextListener
	 extends GuiceServletContextListener {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected Injector getInjector() {
		log.warn("CREATING GUICE Injector.............");
		return Guice.createInjector(new MeteoOpenDataBootstrapGuiceModule(),
									new MeteoOpenDataRESTResourcesGuiceModule(),
									new MeteoOpenDataRESTBootstrapGuiceModule());	
	}
}

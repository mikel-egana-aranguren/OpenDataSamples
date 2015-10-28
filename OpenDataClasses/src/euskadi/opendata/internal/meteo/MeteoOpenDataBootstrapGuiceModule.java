package euskadi.opendata.internal.meteo;

import javax.inject.Singleton;

import r01f.marshalling.Marshaller;
import r01f.marshalling.Marshaller.MarshallerMappingsSearch;
import r01f.marshalling.simple.SimpleMarshallerBuilder;
import r01f.model.annotations.ModelObjectsMarshaller;

import com.google.inject.Binder;
import com.google.inject.Module;

import euskadi.opendata.model.meteo.Forecast;
import euskadi.opendata.service.meteo.WeatherForecastService;
import euskadi.opendata.service.meteo.WeatherForecastServiceImpl;

public class MeteoOpenDataBootstrapGuiceModule
  implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(final Binder binder) {	
		// do the marshaller bindings
		_bindModelObjectsMarshaller(binder);
		
		// do the service bindings
		binder.bind(WeatherForecastService.class).to(WeatherForecastServiceImpl.class)
			  .in(Singleton.class);
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	COMMON BINDINGS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * bindings for the marshaller
	 */
	private static void _bindModelObjectsMarshaller(final Binder binder) {
		// Create the model objects marshaller
		Marshaller marshaller = SimpleMarshallerBuilder.createForPackages(MarshallerMappingsSearch.inPackages(Forecast.class.getPackage().getName()))	
											 		   .getForMultipleUse();
		// Bind this instance to the model object's marshaller
		binder.bind(Marshaller.class).annotatedWith(ModelObjectsMarshaller.class)
									 .toInstance(marshaller);
	}
}

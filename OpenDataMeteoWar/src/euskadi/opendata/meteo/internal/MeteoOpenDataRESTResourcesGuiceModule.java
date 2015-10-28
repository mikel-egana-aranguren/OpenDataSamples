package euskadi.opendata.meteo.internal;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Module;

import euskadi.opendata.meteo.rest.resources.MeteoOpenDataRESTResource;

public class MeteoOpenDataRESTResourcesGuiceModule 
  implements Module {

	@Override
	public void configure(final Binder binder) {
		// do REST app & resources bindings
		binder.bind(MeteoOpenDataRESTApp.class)
			  .in(Singleton.class);
		binder.bind(MeteoOpenDataRESTResource.class)
			  .in(Singleton.class);
		
	}

}

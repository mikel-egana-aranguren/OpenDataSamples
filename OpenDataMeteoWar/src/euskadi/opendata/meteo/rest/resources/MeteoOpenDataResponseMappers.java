package euskadi.opendata.meteo.rest.resources;

import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

import r01f.marshalling.Marshaller;
import r01f.model.ModelObject;
import r01f.model.annotations.ModelObjectsMarshaller;
import r01f.rest.RESTExceptionMappers.RESTUncaughtExceptionMapper;
import r01f.rest.RESTResponseTypeMappersForModelObjects.ModelObjectResponseTypeMapperBase;

public class MeteoOpenDataResponseMappers {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * MessageBodyWriter for all {@link ModelObject}s
	 */
	@Provider
	public static class MeteoOpendataModelObjectResponseTypeMapper 
			    extends ModelObjectResponseTypeMapperBase<ModelObject> {
		@Inject
		public MeteoOpendataModelObjectResponseTypeMapper(@ModelObjectsMarshaller final Marshaller marshaller) {
			super(marshaller);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Provider
	public static class MeteoOpendataUncaughtExceptionMapper 
			    extends RESTUncaughtExceptionMapper {
		/* nothing */
	}
}

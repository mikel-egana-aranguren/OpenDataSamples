package r01f.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.marshalling.Marshaller;
import r01f.model.ModelObject;
import r01f.model.jobs.EnqueuedJob;
import r01f.persistence.index.IndexManagementCommand;
import r01f.reflection.ReflectionUtils;
import r01f.rest.RESTRequestTypeMappersForBasicTypes.XMLMarshalledObjectRequestTypeMapper;
import r01f.util.types.Strings;

/**
 * Type mappers for user types received as POST payload
 */
@Slf4j
public class RESTRequestTypeMappersForModelObjects {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Provider
	@Accessors(prefix="_")
	public static class ModelObjectRequestTypeMapperBase<M extends ModelObject> 
		  	    extends XMLMarshalledObjectRequestTypeMapper<M> {
		
		@Getter private final Marshaller _objectsMarshaller;
		
		public ModelObjectRequestTypeMapperBase(final Marshaller marshaller) {
			super(ModelObject.class);
			_objectsMarshaller = marshaller;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	OID
/////////////////////////////////////////////////////////////////////////////////////////
	@Provider
	public static class OIDRequestTypeMapperBase<O extends OID> 
		  	 implements MessageBodyReader<O> {
		
		private final Marshaller _marshaller;
		
		@Inject
		public OIDRequestTypeMapperBase(final Marshaller marshaller) {
			_marshaller = marshaller;
		}
		
		@Override
		public boolean isReadable(final Class<?> type,final Type genericType,
								  final Annotation[] annotations,
								  final MediaType mediaType) {
			// every application/xml received params are transformed to java in this type
			return mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)
				&& ReflectionUtils.isImplementing(type,OID.class);
		}
		@Override
		public O readFrom(final Class<O> type,final Type genericType,
						  final Annotation[] annotations,
						  final MediaType mediaType,
						  final MultivaluedMap<String,String> httpHeaders,
						  final InputStream entityStream) throws IOException,
							   								     WebApplicationException {
			log.trace("reading {} type",type.getName());
			// xml -> java
			String oidStr = Strings.of(entityStream)
								   .asString();
			O outObj = null;
			if (Strings.isNOTNullOrEmpty(oidStr)) {
				// invoke oid.forId(...) method
				outObj = _marshaller.<O>beanFromXml(oidStr);
			}
			return outObj;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	IndexManagementCommand & EnqueuedJob
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * MessageBodyReader for all {@link IndexManagementCommand}s
	 */
	@Provider
	@Accessors(prefix="_")
	public static abstract class IndexManagementCommandRequestTypeMapperBase 
		     			 extends XMLMarshalledObjectRequestTypeMapper<IndexManagementCommand> {
		
		@Getter private final Marshaller _objectsMarshaller;
		
		public IndexManagementCommandRequestTypeMapperBase(final Marshaller marshaller) {
			super(IndexManagementCommand.class);
			_objectsMarshaller = marshaller;
		}
	}
	/**
	 * MessageBodyReader for all {@link EnqueuedJob}s
	 */
	@Provider
	@Accessors(prefix="_")
	public static abstract class EnqueuedJobRequestTypeMapperBase 
		     			 extends XMLMarshalledObjectRequestTypeMapper<EnqueuedJob> {
		
		@Getter private final Marshaller _objectsMarshaller;
		
		public EnqueuedJobRequestTypeMapperBase(final Marshaller marshaller) {
			super(EnqueuedJob.class);
			_objectsMarshaller = marshaller;
		}
	}
}

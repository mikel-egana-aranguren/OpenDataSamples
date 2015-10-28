package r01f.services.client.servicesproxy.rest;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.httpclient.HttpResponse;
import r01f.marshalling.Marshaller;
import r01f.model.PersistableModelObject;
import r01f.persistence.CRUDResult;
import r01f.persistence.PersistenceRequestedOperation;
import r01f.services.client.servicesproxy.rest.RESTServicesPathBuilders.ServicesRESTResourcePathBuilderForModelObject;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.types.weburl.SerializedURL;
import r01f.usercontext.UserContext;

@Accessors(prefix="_")
@Slf4j
public abstract class RESTCRUDServicesProxyBase<O extends OID,M extends PersistableModelObject<O>>
              extends RESTServicesForModelObjectProxyBase<O,M>
           implements CRUDServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Maps a REST response to a CRUDResult
	 */
	protected final RESTResponseToCRUDResultMapperForModelObject<O,M> _responseToCRUDResultMapper;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	public <P extends ServicesRESTResourcePathBuilderForModelObject<O>>
		   RESTCRUDServicesProxyBase(final Marshaller marshaller,
									 final Class<M> modelObjectType,
									 final P servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  modelObjectType,
			  servicesRESTResourceUrlPathBuilder);
		_responseToCRUDResultMapper = new RESTResponseToCRUDResultMapperForModelObject<O,M>(marshaller,modelObjectType);
	}
	protected <P extends ServicesRESTResourcePathBuilderForModelObject<O>>
			  RESTCRUDServicesProxyBase(final Marshaller marshaller,
									    final Class<M> modelObjectType,
									    final P servicesRESTResourceUrlPathBuilder,
									    final RESTResponseToCRUDResultMapperForModelObject<O,M> responseToCrudResultMapper) {
		super(marshaller,
			  modelObjectType,
			  servicesRESTResourceUrlPathBuilder);
		_responseToCRUDResultMapper = responseToCrudResultMapper;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected RESTResponseToCRUDResultMapperForModelObject<O,M> getResponseToCRUDResultMapperForModelObject() {
		return _responseToCRUDResultMapper;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> load(final UserContext userContext,
			   	  			  final O oid) {
		// do the http call
		SerializedURL restResourceUrl = this.composePersistenceURIFor(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
															   			  .pathOfEntity(oid));
		String ctxXml = _marshaller.xmlFromBean(userContext);
		HttpResponse httpResponse = DelegateForRawREST.GET(restResourceUrl,
										 				   ctxXml);
		// map the response
		CRUDResult<M> outResponse = this.getResponseToCRUDResultMapperForModelObject()
												.mapHttpResponseForEntity(userContext,
															 			  PersistenceRequestedOperation.LOAD,
															  			  restResourceUrl,httpResponse)
															  .identifiedOnErrorBy(oid);
		// check that the received entity is the expected one
		if (outResponse.hasSucceeded()) _checkReceivedEntity(oid,outResponse.getOrThrow());
		
		// log & return
		_logResponse(restResourceUrl,outResponse);
		return outResponse;
	}
	@Override
	public CRUDResult<M> create(final UserContext userContext,
								final M entity) {
		// do the http call
		SerializedURL restResourceUrl = this.composePersistenceURIFor(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
															   			  .pathOfAllEntities());	//   .pathOfEntity(entity.getOid())); 	// _resourcePathForRecord(record,PersistenceRequestedOperation.CREATE);
		String ctxXml = _marshaller.xmlFromBean(userContext); 		
		String entityXml = _marshaller.xmlFromBean(entity);
		HttpResponse httpResponse = DelegateForRawREST.POST(restResourceUrl,
										  					ctxXml,
										  					entityXml);
		// map the response
					
		CRUDResult<M> outResponse = this.getResponseToCRUDResultMapperForModelObject()
												.mapHttpResponseForEntity(userContext,
															              PersistenceRequestedOperation.CREATE,
															  			  entity,
															  			  restResourceUrl,httpResponse);
		// check that the received entity is the expected one just if requested entity oid is not null .
		// It could be possible on creating a new entity not to send an entity oid
		if (outResponse.hasSucceeded() && entity.getOid() != null) _checkReceivedEntity(entity.getOid(),outResponse.getOrThrow());
		
		// log & return
		_logResponse(restResourceUrl,outResponse);
		return outResponse;
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> update(final UserContext userContext,
								final M entity) {
		// do the http call
		SerializedURL restResourceUrl = this.composePersistenceURIFor(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
													 		   			  .pathOfEntity(entity.getOid())); 	// _resourcePathForRecord(record,PersistenceRequestedOperation.UPDATE);
		String ctxXml = _marshaller.xmlFromBean(userContext); 	
		String entityXml = _marshaller.xmlFromBean(entity);
		HttpResponse httpResponse = DelegateForRawREST.PUT(restResourceUrl,
										 				   ctxXml,
										 				   entityXml);
		// map the response
		CRUDResult<M> outResponse = this.getResponseToCRUDResultMapperForModelObject()
												.mapHttpResponseForEntity(userContext,
																 		  PersistenceRequestedOperation.UPDATE,
															  			  entity,
															  			  restResourceUrl,httpResponse);
		// check that the received entity is the expected one 
		if (outResponse.hasSucceeded() ) _checkReceivedEntity(entity.getOid(),outResponse.getOrThrow());
		
		// log & return
		_logResponse(restResourceUrl,outResponse);
		return outResponse;
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> delete(final UserContext userContext,
							    final O oid) {
		// do the http call
		SerializedURL restResourceUrl = this.composePersistenceURIFor(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
															   			  .pathOfEntity(oid));
		String ctxXml = _marshaller.xmlFromBean(userContext);
		HttpResponse httpResponse = DelegateForRawREST.DELETE(restResourceUrl,
															  ctxXml);
		// map the response
		CRUDResult<M> outResponse = this.getResponseToCRUDResultMapperForModelObject()
												.mapHttpResponseForEntity(userContext,
											  				 			  PersistenceRequestedOperation.DELETE,
											  				 			  restResourceUrl,httpResponse)
															  .identifiedOnErrorBy(oid);
		// check that the received entity is the expected one
		if (outResponse.hasSucceeded()) _checkReceivedEntity(oid,outResponse.getOrThrow());
		
		// log & return
		_logResponse(restResourceUrl,outResponse);
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _checkReceivedEntity(final O requestedOid,final M receivedEntity) {
		// check that the received type is the expected one
//		if (receivedEntity.getClass() != _modelObjectType) throw new IllegalStateException(Throwables.message("The client REST proxy received type ({}) is NO the expected one {}",
//																							  	 			  receivedEntity.getClass(),_modelObjectType));
		// Check that it's about the same entity by comparing the received entity oid with the expected one
		if (!receivedEntity.getOid().equals(requestedOid)) throw new IllegalStateException(Throwables.message("The client REST proxy received entity has NOT the same oid as the expected one (recived={}, expected={})",
																									 	 	  receivedEntity.getOid(),requestedOid));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  LOG
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _logResponse(final SerializedURL restResourceUrl,
						   	    final CRUDResult<M> opResult) {
		if (opResult.hasSucceeded()) {
			log.info("Successful REST {} operation at resource path={} on entity with oid={}",opResult.getRequestedOperationName(),restResourceUrl,opResult.getOrThrow().getOid());
		}
		else if (opResult.asError().wasBecauseClientCouldNotConnectToServer()) {			// as(CRUDError.class)
			log.error("Client cannot connect to REST endpoint {}",restResourceUrl);
		} else if (!opResult.asError().wasBecauseAClientError()) {							// as(CRUDError.class)
			log.error("REST: On requesting the {} operation, the REST resource with path={} returned a persistence error code={}",
					  opResult.getRequestedOperationName(),restResourceUrl,opResult.asError().getErrorType().getCode());					
			log.debug("[ERROR]: {}",opResult.getDetailedMessage());
		} else {
			log.debug("Client error on requesting the {} operation, the REST resource with path={} returned: {}",
					  opResult.getRequestedOperationName(),restResourceUrl,opResult.getDetailedMessage());
		}
	}
}

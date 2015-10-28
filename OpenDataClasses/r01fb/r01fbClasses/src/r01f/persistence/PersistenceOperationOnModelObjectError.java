package r01f.persistence;

import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Maps;


@Accessors(prefix="_")
abstract class PersistenceOperationOnModelObjectError<T>
       extends PersistenceOperationExecError<T>
    implements PersistenceOperationError,
  			   PersistenceOperationOnModelObjectResult<T>,
    		   Debuggable{
/////////////////////////////////////////////////////////////////////////////////////////
//  SERIALIZABLE DATA
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The type of the entity subject of the requested operation 
	 */
	@XmlAttribute(name="type")
	@Getter @Setter protected Class<? extends PersistableModelObject<? extends OID>> _modelObjectType;
	/**
	 * The requested operation
	 */
	@XmlAttribute(name="requestedOperation")
	@Getter @Setter protected PersistenceRequestedOperation _requestedOperation;
	/**
	 * Some data about the requested operation target entity such as it's oid
	 */
	@XmlElementWrapper(name="requestedOperationTarget")
	@Getter @Setter protected Map<String,String> _requestedOperationTargetEntityIdInfo;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public PersistenceOperationOnModelObjectError() {
		// nothing
	}
	PersistenceOperationOnModelObjectError(final Class<? extends PersistableModelObject<? extends OID>> entityType,
		 				 				   final PersistenceRequestedOperation requestedOp,
		 				 				   final Throwable th) {
		_modelObjectType = entityType;
		_requestedOperation = requestedOp;
		_requestedOperationName = requestedOp.name();
		_error = th;		
		if (th != null) {
			_errorMessage = th.getMessage();
			_errorDebug = Throwables.getStackTraceAsString(th);
			if (th instanceof PersistenceException) {
				PersistenceException persistEx = (PersistenceException)th; 
				_errorType = persistEx.getPersistenceErrorType();
			} else {
				_errorType = PersistenceErrorType.SERVER_ERROR;		// a server error by default
				
			}
		}
	}
	PersistenceOperationOnModelObjectError(final Class<? extends PersistableModelObject<? extends OID>> entityType,
						 				   final PersistenceRequestedOperation requestedOp,
						 				   final PersistenceErrorType errCode) {
		this(entityType,
			 requestedOp,
			 (Throwable)null);		// no exception
		_errorDebug = null;
		_errorType = errCode;
	}
	PersistenceOperationOnModelObjectError(final Class<? extends PersistableModelObject<? extends OID>> entityType,
						 				   final PersistenceRequestedOperation requestedOp,
						 				   final String errMsg,final PersistenceErrorType errCode) {
		this(entityType,
			 requestedOp,
			 (Throwable)null);		// no exception
		_errorMessage = errMsg;
		_errorDebug = null;
		_errorType = errCode;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public PersistencePerformedOperation getPerformedOperation() {
		return null;	// no performed operation
	}
	protected void addTargetEntityIdInfo(final String field,final String value) {
		if (_requestedOperationTargetEntityIdInfo == null) _requestedOperationTargetEntityIdInfo = Maps.newHashMap();
		_requestedOperationTargetEntityIdInfo.put(field,value);
	}
	/**
	 * @return any info about the target entity such as it's oid 
	 */
	protected String getTargetEntityIdInfo() {
		String outIdInfo = null;
		if (CollectionUtils.hasData(_requestedOperationTargetEntityIdInfo)) {
			StringBuilder sb = new StringBuilder();
			for (Iterator<Map.Entry<String,String>> meIt =_requestedOperationTargetEntityIdInfo.entrySet().iterator(); meIt.hasNext(); ) {
				Map.Entry<String,String> me = meIt.next();
				sb.append(me.getKey())
				  .append("=")
				  .append(me.getValue());
				if (meIt.hasNext()) sb.append(", ");
			}
			outIdInfo = sb.toString();
		}
		return outIdInfo != null ? outIdInfo : "unknown";
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  REASON
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return true if it was an error due to the client sending a version number that does NOT match the db-stored one (see Optimistic Locking)
	 */
	public boolean wasBecauseAnOptimisticLockingError() {
		return _errorType == PersistenceErrorType.OPTIMISTIC_LOCKING_ERROR;
	}
	/**
	 * @return true if it was a client bad request due to the requested entity was NOT found
	 */
	public boolean wasBecauseClientRequestedEntityWasNOTFound() {
		return _errorType == PersistenceErrorType.ENTITY_NOT_FOUND;
	}
	/**
	 * @return true if it was a client bad request due to the requested entity already exists and a create operation was issued
	 */
	public boolean wasBecauseClientRequestedEntityAlreadyExists() {
		return _errorType == PersistenceErrorType.ENTITY_ALREADY_EXISTS;
	}
	/**
	 * @return true if it was a client bad request due to a required related entity was NOT found
	 */
	public boolean wasBecauseClientRequestedEntityRequiredRelatedEntityNOTFound() {
		return _errorType == PersistenceErrorType.RELATED_REQUIRED_ENTITY_NOT_FOUND;
	}
	/**
	 * @return true if it was a client bad request due to some validation error in the entity
	 */
	public boolean wasBecauseClientRequestedEntityValidationErrors() {
		return _errorType == PersistenceErrorType.ENTITY_NOT_VALID;
	}
	/**
	 * @return true if it was because the entity's persisted status is NOT valid 
	 */
	public boolean wasBecauseClientRequestedEntityWasInAnIllegalStatus() {
		return _errorType == PersistenceErrorType.ILLEGAL_STATUS;
	}
}
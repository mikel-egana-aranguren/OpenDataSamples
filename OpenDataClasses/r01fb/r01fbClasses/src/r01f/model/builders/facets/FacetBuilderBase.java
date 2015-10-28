package r01f.model.builders.facets;

import lombok.experimental.Accessors;
import r01f.patterns.IsBuilder;

/**
 * Base type for all content model object's builders
 */
@Accessors(prefix="_")
public abstract class FacetBuilderBase<CONTAINER_TYPE,
									   T> 
		   implements IsBuilder { 
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Parent type containing this facet builder
	 */
	protected final CONTAINER_TYPE _parentType;
	/**
	 * Model object
	 */
	protected final T _modelObject;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public FacetBuilderBase(final CONTAINER_TYPE parentBuilder,
							final T modelObject) {
		_parentType = parentBuilder;
		_modelObject = modelObject;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected T getModelObject() {
		return _modelObject;
	}
	public CONTAINER_TYPE finish() {
		return _parentType;
	}
}
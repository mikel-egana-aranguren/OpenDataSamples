package r01f.model.metadata;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import r01f.exceptions.Throwables;
import r01f.guids.CommonOIDs.AppCode;
import r01f.model.ModelObject;
import r01f.model.annotations.ModelObjectData;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItemForModelObject;
import r01f.reflection.ReflectionUtils;
import r01f.services.client.internal.ServicesClientBootstrapGuiceModule;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;


/**
 * MetaData that describes a {@link ModelObject} and it's associated {@link SearchFilter}
 * or {@link SearchResultItemForModelObject} if the {@link ModelObject} is searchable
 */
@GwtIncompatible("ModelObjectMetaDataBuilder NOT usable in GWT")
@Accessors(prefix="_")
@NoArgsConstructor(access=AccessLevel.PRIVATE)
@Slf4j
public class ModelObjectTypeMetaDataBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  STATIC STATE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Stores the model object metadata cache
	 */
	private static final ConcurrentMap<Class<? extends ModelObject>,ModelObjectTypeMetaData> META_DATA_CACHE = Maps.newConcurrentMap();
	
	/**
	 * Inits the META_DATA_CACHE for every model object at the provided app code
	 * This method is called at the CLIENT bootstrapping (see {@link ServicesClientBootstrapGuiceModule})
	 * @param appCode
	 */
	public static void init(final AppCode appCode) {
		log.info("Finding model objects for {} appCode at {}.model.*",appCode,appCode);
		
		// Find every type annotated with ModelObjectData
		String modelObjPackage = Strings.of("{}.model")
									  	.customizeWith(appCode)
									  	.asString();
		List<URL> modelObjTypesUrl = new ArrayList<URL>();
		modelObjTypesUrl.addAll(ClasspathHelper.forPackage(modelObjPackage));	// xxx.model.*
		Reflections ref = new Reflections(new ConfigurationBuilder()					
													.setUrls(modelObjTypesUrl)	
													.setScanners(new SubTypesScanner(),
																 new TypeAnnotationsScanner()));
		Set<Class<?>> modelObjTypes = ref.getTypesAnnotatedWith(ModelObjectData.class);
		
		// For every found type, look at the @ModelObjectData annotation and load the ModelObjectMetaData
		if (CollectionUtils.hasData(modelObjTypes)) {
			for (Class<?> modelObjType : modelObjTypes) {
				if (ReflectionUtils.isAbstract(modelObjType)) continue;		// skip base types
				
				// check that the supposed model object really is a model object
				if (!ReflectionUtils.isImplementing(modelObjType,
												    ModelObject.class)) throw new IllegalStateException(Throwables.message("@{} annotation can only be user on {} types, please check {} type",
																									    ModelObjectData.class.getSimpleName(),ModelObject.class.getSimpleName(),modelObjType));
				// Cast to model object
				@SuppressWarnings("unchecked")
				Class<? extends ModelObject> theModelObjType = (Class<? extends ModelObject>)modelObjType;
				
				// Find the data in @ModelObjectData annotation
				ModelObjectData modelObjectAnnotData = _findModelObjectMetaDataAnnot(theModelObjType);
				if (modelObjectAnnotData == null) throw new IllegalStateException(Strings.customized("The {} model object type or a type in it's hierarchy (super types or implemented interfaces) MUST be annotated with {} specifying the model object's metadata",
																	     							 theModelObjType,ModelObjectData.class));
				
				log.info("\t>{} : {}",theModelObjType,modelObjectAnnotData.value());
				ModelObjectTypeMetaData newMetaData = ReflectionUtils.createInstanceOf(modelObjectAnnotData.value());
				
				// Check that the model object meta-data refers to this model object
				if (newMetaData.getType() != theModelObjType) {
					if (ReflectionUtils.isImplementing(theModelObjType,newMetaData.getType())) {
						// this is acceptable
					} else {
						throw new IllegalStateException(Throwables.message("The model object metadata {} is supposed to set the metadata of a {} type BUT it's for a {} type",
																		   modelObjectAnnotData.value().getSimpleName(),theModelObjType,newMetaData.getType()));

					}
				}
				// Put the new model object's metadata in the cache
				META_DATA_CACHE.putIfAbsent(theModelObjType,newMetaData);
			}
		}
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public static <MD extends ModelObjectTypeMetaData> MD createFor(final String modelObjectTypeAsString) {
		Class<? extends ModelObject> modelObjType = ReflectionUtils.typeFromClassName(modelObjectTypeAsString);
		ModelObjectTypeMetaData outMetaData = ModelObjectTypeMetaDataBuilder.createFor(modelObjType);
		return (MD)outMetaData;
	}
	@SuppressWarnings("unchecked")
	public static <M extends ModelObject,MD extends ModelObjectTypeMetaData> MD createFor(final Class<M> modelObjectType) {
		// Check the cache
		ModelObjectTypeMetaData outMetaData = META_DATA_CACHE.get(modelObjectType);
		if (outMetaData == null) {
			// usually it does NOT have to enter this... every model object MUST have to be found at the init() method
			log.warn("There was NO {} at the cache for {}",ModelObjectTypeMetaData.class.getSimpleName(),modelObjectType);
			
			// Find the data in @ModelObjectData annotation
			ModelObjectData modelObjectAnnotData = _findModelObjectMetaDataAnnot(modelObjectType);
			if (modelObjectAnnotData == null) throw new IllegalStateException(Strings.customized("The {} model object type or a type in it's hierarchy (super types or implemented interfaces) MUST be annotated with {} specifying the model object's metadata",
																     							 modelObjectType,ModelObjectData.class));
			
			log.warn("Loading modelObject metadata from {}",modelObjectAnnotData.value());
			ModelObjectTypeMetaData newMetaData = ReflectionUtils.createInstanceOf(modelObjectAnnotData.value());
			
			// Put the new model object's metadata in the cache
			outMetaData = META_DATA_CACHE.putIfAbsent(modelObjectType,newMetaData);
			if (outMetaData == null) outMetaData = newMetaData;		
		}
		return (MD)outMetaData;
	}
	private static ModelObjectData _findModelObjectMetaDataAnnot(final Class<?> type) {
		// recursively finds a ModelObjectData annotation:
		//		- If the object is NOT annotated with ModelObjectData annotation, all it's directly implemented interfaces are checked
		//		- ... if the ModelObjectData annotation is NOT found, it's super-type is checked recursively
		
		// [1]-Check if the object is annotated
		ModelObjectData modelObjAnnotatedMetaData = ReflectionUtils.typeAnnotation(type,
													 							   ModelObjectData.class);
		if (modelObjAnnotatedMetaData != null) return modelObjAnnotatedMetaData;
		
		// [2]-Check all object's directly declared interfaces and their super-interfaces
		ModelObjectData interfaceAnnotatedMetaData = null;
		Class<?>[] ifcs = type.getInterfaces();
		for (Class<?> ifc : ifcs) {
			interfaceAnnotatedMetaData = ReflectionUtils.typeAnnotation(ifc,
														 			    ModelObjectData.class);
			if (interfaceAnnotatedMetaData != null) break;
			Class<?>[] superIfcs = ifc.getInterfaces();
			if (superIfcs != null) {
				for (Class<?> superIfc : superIfcs) {
					interfaceAnnotatedMetaData = _findModelObjectMetaDataAnnot(superIfc);
					if (interfaceAnnotatedMetaData != null) break;
				}
			}
			if (interfaceAnnotatedMetaData != null) break;
		}
		
		// [3]-Check the object's super-type
		ModelObjectData superTypeAnnotatedMetaData = null;
		Class<?> superType = type.getSuperclass();
		if (superType != null && superType != Object.class) superTypeAnnotatedMetaData = _findModelObjectMetaDataAnnot(superType);
		
		if (superTypeAnnotatedMetaData != null && interfaceAnnotatedMetaData != null
		 && superTypeAnnotatedMetaData != interfaceAnnotatedMetaData) {
			throw new IllegalStateException(type.toString());
//			return interfaceAnnotatedMetaData;
		} else if (interfaceAnnotatedMetaData != null) {
			return interfaceAnnotatedMetaData;
		} else if (superTypeAnnotatedMetaData != null) {
			return superTypeAnnotatedMetaData;
		} else {
			return null;
		}
	}
	@SuppressWarnings("unchecked")
	static Set<ModelObjectTypeMetaData> facetTypesMetaDataFor(final Class<? extends ModelObject> type) {
		// Find in the type hierarchy all types annotated with ModelObjectData
		Set<Class<?>> facetTypes = org.reflections.ReflectionUtils.getAllSuperTypes(type,
																					new Predicate<Class<?>>() {
																							@Override
																							public boolean apply(final Class<?> superType) {
																								return type != superType	//  beware to not include the type!
																									&& ReflectionUtils.typeAnnotation(superType,
																																	  ModelObjectData.class) != null;
																							}
																					});
		// Transform the Set<Class<? extends ModelObject>> to Set<ModelObjectTypeMetaData>
		return FluentIterable.from(facetTypes)
							 .transform(new Function<Class<?>,ModelObjectTypeMetaData>() {
												@Override
												public ModelObjectTypeMetaData apply(final Class<?> typeWithMetaData) {
													return ModelObjectTypeMetaDataBuilder.createFor((Class<? extends ModelObject>)typeWithMetaData);
												}
							 			})
							 .toSet();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Using the model object's type code, gets the metadata for a PREVIOUSLY CACHED model object 
	 * @param modelObjTypeCode
	 * @return
	 */
	public static ModelObjectTypeMetaData createFor(final long modelObjTypeCode) {
		ModelObjectTypeMetaData outMetaData = null;
		for (ModelObjectTypeMetaData md : META_DATA_CACHE.values()) {
			if (md.getTypeCode() == modelObjTypeCode) {
				outMetaData = md;
				break;
			}
		}
		return outMetaData;
	}
}

package r01f.aspects.core.dirtytrack;


import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.aspects.core.util.ObjectsHierarchyModifier;
import r01f.aspects.core.util.ObjectsHierarchyModifier.StateModifierFunction;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.NotDirtyStateTrackable;
import r01f.generics.TypeRef;
import r01f.reflection.ReflectionUtils;
import r01f.types.annotations.CompositionRelated;
import r01f.types.dirtytrack.ChangesTrackedCollection;
import r01f.types.dirtytrack.ChangesTrackedList;
import r01f.types.dirtytrack.ChangesTrackedMap;
import r01f.types.dirtytrack.ChangesTrackedSet;
import r01f.types.dirtytrack.interfaces.ChangesTrackableCollection;
import r01f.types.dirtytrack.interfaces.ChangesTrackableMap;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Predicate;

@Slf4j
class DirtyStateTrackingUtils {
	/**
	 * Se encarga de transformar un miembro tipo Collection/List/Set a un objeto que lo "envuelve" llevando
	 * cuenta de los cambios que se produzcan en la colecci�n subyacente 
	 * @param trckContainer el objeto que contiene el miembro 
	 * @param mapField el miembro tipo map
	 * @param col la colecci�n (list, set o collection)
	 * @return la colecci�n envuelta en un objeto que lleva cuenta de los cambios que se produzcan en la misma
	 */
	static <K,V,T extends Map<K,V>> Object wrapMapToTrackable(final DirtyStateTrackable trckContainer,
															  final Field mapField,
															  final T map) {
    	// Si se dan las siguientes condiciones:
    	//		a.- El objeto padre es trackable y se est�n siguiendo los cambios 
    	//		b.- la colecci�n NO es un mapa ChangesTrackedMapInTrackableObject
    	// dar el cambiazo por uno trackable
		Object outMap = null;
    	if (trckContainer.getTrackingStatus().isThisDirtyTracking() && !(map instanceof ChangesTrackableMap)) {
    		// Asegurarse primero de que el field es de tipo Map (el interface y NO un tipo concreto)
    		boolean isMap = Map.class.isAssignableFrom(mapField.getType());
    		if (!isMap) throw new IllegalArgumentException("The Map field " + mapField.getDeclaringClass().getName() + "." + mapField.getName() + " (" + mapField.getType() + ") type is NOT a java.util.Map (the interface), so it cannot be converted to a ChangesTrackableMap.");
    		
    		// Cambiar la instancia de Map por una ChangesTrackableMap
    		ChangesTrackableMap<K,V> changesTracked = _wrapMap(map);
    		changesTracked.getTrackingStatus().setThisDirtyTracking(trckContainer.getTrackingStatus().isThisDirtyTracking());		// pasarle el estado de tracking al nuevo mapa
    		ReflectionUtils.setFieldValue(trckContainer,mapField,changesTracked,false);
    		outMap = changesTracked;
    	} else if (!trckContainer.getTrackingStatus().isThisDirtyTracking() && map instanceof ChangesTrackableMap) {
    		// NO se est� haciendo tracking, pero se hab�a dado el cambiazo... simplemente devolver el mapa
    		outMap = map;
    	} else {
    		// Se est� haciendo tracking y ya se ha dado el cambiazo
    		outMap = map;
    	}
    	return outMap;
	}
	/**
	 * Se encarga de transformar un miembro tipo Collection/List/Set a un objeto que lo "envuelve" llevando
	 * cuenta de los cambios que se produzcan en la colecci�n subyacente 
	 * @param trckContainer el objeto que contiene el miembro 
	 * @param colField el miembro
	 * @param col la colecci�n (list, set o collection)
	 * @return la colecci�n envuelta en un objeto que lleva cuenta de los cambios que se produzcan en la misma
	 */
	static <V,T extends Collection<V>> Object wrapCollectionToTrackable(final DirtyStateTrackable trckContainer,
																		final Field colField,
																		final T col) {
    	// Si se dan las siguientes condiciones:
    	//		a.- El objeto padre es trackable y se est�n siguiendo los cambios 
    	//		b.- la colecci�n NO es un mapa ChangesTrackedCollectionInTrackableObject
    	// dar el cambiazo por uno trackable
		Object outCol = null;
    	if (trckContainer.getTrackingStatus().isThisDirtyTracking() && !(col instanceof ChangesTrackableCollection)) {
    		// Se est� haciendo tracking pero la colecci�n a�n NO se ha envuelto
    		// Asegurarse primero de que el field es de tipo Map (el interface y NO un tipo concreto)
    		boolean isCollectionOrSet = Collection.class.isAssignableFrom(colField.getType()) || 
    									Set.class.isAssignableFrom(colField.getType()) ||
    									List.class.isAssignableFrom(colField.getType());
    		if (!isCollectionOrSet) throw new IllegalArgumentException("The Collection/List/Set field " + colField.getDeclaringClass().getName() + "." + colField.getName() + " (" + colField.getType() + ") type is NOT a java.util.Collection/java.util.List/java.util.Set (the interface), so it cannot be converted to a ChangesTrackableCollection.");
    		
    		// Cambiar la instancia de Collection por una ChangesTrackableCollection
    		ChangesTrackableCollection<V> changesTracked = _wrapCollection(col);
    		changesTracked.getTrackingStatus().setThisDirtyTracking(trckContainer.getTrackingStatus().isThisDirtyTracking());		// pasarle el estado de tracking a la nueva colecci�n
    		ReflectionUtils.setFieldValue(trckContainer,colField,changesTracked,false);
    		outCol = changesTracked;
    	} else if (!trckContainer.getTrackingStatus().isThisDirtyTracking() && col instanceof ChangesTrackableCollection) {
    		// NO se est� haciendo tracking: devolver la colecci�n en su estado original
    		outCol = col;
    	} else {
    		// Se est� haciendo tracking y ya se ha dado el cambiazo
    		outCol = col;
    	}
    	return outCol;
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  WRAPPING DE MAPAS / COLECCIONES (V1)
//	Funciona correctamente para Mapas/Sets/List "simples", es decir:
//	public class MyObj {
//		private Map<String,String> _myMap;	<-- Es posible dar el "cambiazo" de _myMap por
//	}											un LazyMap ya que ambos son Map
/////////////////////////////////////////////////////////////////////////////////////////
	private static <K,V,T extends Map<K,V>> ChangesTrackedMap<K,V> _wrapMap(final T map) {
		ChangesTrackedMap<K,V> outWrappedMap = new ChangesTrackedMap<K,V>(map);
		return outWrappedMap;
	}	
	private static <V,T extends Collection<V>> ChangesTrackedCollection<V> _wrapCollection(final T col) {
		ChangesTrackedCollection<V> outWrappedCol = null;
		if (col instanceof List) {
			outWrappedCol = new ChangesTrackedList<V>(col);
		} else if (col instanceof Set) {
			outWrappedCol = new ChangesTrackedSet<V>(col);
		} else {
			outWrappedCol = new ChangesTrackedCollection<V>(col);
		}
		return outWrappedCol;
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//	Utilidad para saber si un objeto est� sucio, para lo cual se recorre todos sus 
// 	miembros y comprueba si est�n sucios
/////////////////////////////////////////////////////////////////////////////////////////
	public static boolean isThisObjectDirty(final DirtyStateTrackable trck) {
		return _isObjectDirty(trck,false);
	}
	public static boolean isObjectDirty(final DirtyStateTrackable trck) {
		return _isObjectDirty(trck,true);
	}
	private static boolean _isObjectDirty(final DirtyStateTrackable trck,final boolean checkDependants) {
		String dirtyMemberName = null;
		boolean outDirty = trck.getTrackingStatus().isThisDirty();
		if (outDirty) dirtyMemberName = "some not complex member"; 
		if (!outDirty) {
			// Check the child DirtyStateTrackable objects
			// BEWARE: [1] an infinite loop could be started if the child object mantains a reference to the father object
			//		   [2] sometimes the field is NOT declared with a DirtyStateTrackable type
			//			   @ConverToDirtyStateTrackable
			//			   public class SomeTrackableType {
			//					private MyType _myType;		<--- MyType is NOT a DirtyStateTrackable subtype... so it's NOT detected as a DirtyStateTrackable object
			//			   }
			//			   The solution is to annotate the field with @ConvertToDirtyStateTrackable 
			//			   @ConverToDirtyStateTrackable
			//			   public class SomeTrackableType {
			//					@ConvertToDirtyStateTrackable	<-- now MyType is recognized as a DirtyStateTrackable instance
			//					private MyType _myType;		
			//			   }
			Field[] trackableFields = ReflectionUtils.fieldsMatching(trck.getClass(),
																   	 new Predicate<Field>() {
																			@Override
																			public boolean apply(final Field f) {
																				return ReflectionUtils.isSubClassOf(f.getType(),DirtyStateTrackable.class)	// DirtyStateTrackable fields
																					   ||
																					   f.isAnnotationPresent(ConvertToDirtyStateTrackable.class);			// or fields annotated with @ConvertToDirtyStateTrackable
																			}
				
																	 });
			if (!CollectionUtils.isNullOrEmpty(trackableFields)) {
				for (Field f : trackableFields) {
					if (f.isAnnotationPresent(NotDirtyStateTrackable.class)) continue;					// do not check @NotDirtyStateTrackable annotated members (for example to avoid infinete loops in reference to the father object)
					if (!checkDependants && !f.isAnnotationPresent(CompositionRelated.class)) continue;	// if only THIS object is being checked, do NOT check complex members NOT annotated with @CompositionRelated
					
					DirtyStateTrackable trackable = ReflectionUtils.fieldValue(trck,f,false);
					if (trackable == null) continue;
					//log.trace("\t-dirtyChecking of complex field {} {}.{}",trck.getClass().getName(),f.getName(),trackable.getClass().getName());
					if (trackable.isDirty()) {
						dirtyMemberName = f.getName();
						outDirty = true;
						break;
					}
				}
			}
		}
		if (!outDirty) {
			// Comprobar las colecciones tipo Map
			Field[] colFields = ReflectionUtils.fieldsOfType(trck.getClass(),Map.class);
			if (!CollectionUtils.isNullOrEmpty(colFields)) {
				for (Field f : colFields) {
					if (f.isAnnotationPresent(NotDirtyStateTrackable.class)) continue;					// pasar de los campos anotados con @NotDirtyStateTrackable (sino se mete en un bucle)
					if (!checkDependants && !f.isAnnotationPresent(CompositionRelated.class))	continue;	// si se mira SOLO este objeto pasar de los miembros complejos que NO est�n anotados con @CompositionRelated
					
					Object mapInstance = ReflectionUtils.fieldValue(trck,f,false);		// Obtener la instancia del miembro para ver si es ChangesTrackable ya que de la definici�n del miembro
					if (mapInstance == null) continue;									// solo se puede saber si es Map
					//log.trace("\t-dirtyChecking of map field {} {}.{}",trck.getClass().getName(),f.getName(),mapInstance.getClass().getName());
					if (ReflectionUtils.isImplementing(mapInstance.getClass(),ChangesTrackableMap.class) 
					 && ((ChangesTrackableMap<?,?>)mapInstance).isDirty()) {
						dirtyMemberName = f.getName();
						outDirty = true;
						break;
					}
				}
			}
		}
		if (!outDirty) {
			// Comprobar las colecciones tipo Collection
			Field[] colFields = ReflectionUtils.fieldsOfType(trck.getClass(),Collection.class);
			if (!CollectionUtils.isNullOrEmpty(colFields)) {
				for (Field f : colFields) {
					if (f.isAnnotationPresent(NotDirtyStateTrackable.class)) continue;						// pasar de los campos anotados con @NotDirtyStateTrackable (sino se mete en un bucle)
					if (!checkDependants && !f.isAnnotationPresent(CompositionRelated.class))	continue;	// si se mira SOLO este objeto pasar de los miembros complejos que NO est�n anotados con @CompositionRelated
					
					Object colInstance = ReflectionUtils.fieldValue(trck,f,false);		// Obtener la instancia del miembro para ver si es ChangesTrackable ya que de la definici�n del miembro
					if (colInstance == null) continue;									// solo se puede saber si es Collection
					//log.trace("\t-dirtyChecking of collection field {} {}.{}",trck.getClass().getName(),f.getName(),colInstance.getClass().getName());
					if (ReflectionUtils.isImplementing(colInstance.getClass(),ChangesTrackableCollection.class) 
					 && ((ChangesTrackableCollection<?>)colInstance).isDirty()) {
						dirtyMemberName = f.getName();
						outDirty = true;
						break;
					}
				}
			}
		}
		if (outDirty) log.debug("/DirtyTracking/: an instance of {} is detected to be dirty ({})",
						trck.getClass().getName(),dirtyMemberName);
		return outDirty;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	Utilidad para cambiar el estado de monitorizaci�n de cambios una jerarqu�a de objetos.
//	Utiliza reflection para recorrer la jerarqu�a de objetos y cambiar el estado de 
//	aquellos que son DirtyStateTrackable
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Predicado para excluir algunos fields en los m�todos que utilizan changeObjectHierarchyState
	 * para por ejemplo comenzar a controlar cambios en el estado o ver si el objeto est� sucio
	 */
	static final Predicate<Field> _fieldAcceptCriteria = new Predicate<Field>() {
																@Override
																public boolean apply(final Field f) {
																	if (f.getDeclaringClass().getPackage().getName().startsWith("java")) return false;	
																	if (f.getDeclaringClass().getPackage().getName().startsWith("com.google")) return false;
																	if (f.getName().startsWith("ajc$")) return false; 
																	if (f.getName().startsWith("_tracking")) return false;
																	return true;
																}
														};
	/**
	 * Modifies an object's status
	 */
	static class DirtyStatusModifier {
		@NoArgsConstructor
		private static class DirtyStatusResetFunction 
			      implements StateModifierFunction<DirtyStateTrackable> {
			
			@Override
			public void changeState(final DirtyStateTrackable obj) {
				obj.getTrackingStatus().setThisDirty(false);	// The object is NOT dirty
				obj.getTrackingStatus().setThisNew(false);		// The object is NOT new
			}
		}
		/**
		 * Establece el estado dirty de un objeto y opcionalmente de sus descendientes
		 * @param trackableObj el objeto trackable
		 * @param changeAlsoChilds true si hay que cambiar el estado de sus descendientes
		 */
		public static void resetDirtyStatus(final DirtyStateTrackable trackableObj,
										  	final boolean changeAlsoChilds) {
			ObjectsHierarchyModifier.<DirtyStateTrackable>changeObjectHierarchyState(trackableObj,new TypeRef<DirtyStateTrackable>() {/* nothing */},
																		   			 new DirtyStatusResetFunction(),
																		   			 changeAlsoChilds,
																		   			 _fieldAcceptCriteria);
		}
	}
	static class DirtyTrackingStatusModifier {
		private static class DirtyTrackingStatusModifierFunction 
		          implements StateModifierFunction<DirtyStateTrackable> {
			
			private final boolean _track;
			private 	  boolean _checkIfOldValueChanges;
			
			public DirtyTrackingStatusModifierFunction(final boolean track) {
				_track = track;
			}
			public DirtyTrackingStatusModifierFunction(final boolean track,
													   final boolean checkIfOldValueChanges) {
				_track = track;
				_checkIfOldValueChanges = checkIfOldValueChanges;
			}
			@Override
			public void changeState(final DirtyStateTrackable trck) {
				trck.getTrackingStatus()
					.setThisDirtyTracking(_track);
				if (trck instanceof ChangesTrackableCollection) {
					((ChangesTrackableCollection<?>)trck).getChangesTracker()
														 .startTrackingChangesInState(true,_checkIfOldValueChanges);
				} else if (trck instanceof ChangesTrackableMap) {
					((ChangesTrackableMap<?,?>)trck).getChangesTracker().startTrackingChangesInState(true,_checkIfOldValueChanges);
				} 
				if (_track) trck.getTrackingStatus()
								.setThisCheckIfValueChanges(_checkIfOldValueChanges);		// SOLO se cambia cuando se empieza a trackear
			}																				// as� NO tiene efecto cuando se llama desde stopTrackingChangesInState
		}
		/**
		 * Cambia el estado de monitorizaci�n de un objeto y sus objetos dependientes
		 * @param trackableObj el objeto DirtyStateTrackable
		 * @param checkIfOldValueChanges Establece c�mo se comprueba si un miembro ha cambiado
	 	 *									- _trckCheckIfValueChanges=true --> al cambiar un field, se comprueba si el nuevo valor es igual o no al valor anterior
		 *									  (el joinpoint es en before fieldSet) 
		 *									- _trckCheckIfValueChanges=false -> al cambiar un field, NO se comprueba si el nuevo valor es igual o no al anterior
		 *									  simplemente al hacer un set de un campo se considera un cambio, independientemente
		 *								      de que el nuevo valor sea igual o no al anterior
		 *									  (el joinpiont es en after fieldSet)
		 */
		public static void startTrackingChangesInState(final DirtyStateTrackable trackableObj,
													   final boolean checkIfOldValueChanges,
													   final boolean startTrackingInChilds) {
			ObjectsHierarchyModifier.<DirtyStateTrackable>changeObjectHierarchyState(trackableObj,new TypeRef<DirtyStateTrackable>() {/* nothing */},
																		   			 new DirtyTrackingStatusModifierFunction(true,checkIfOldValueChanges),
																		   			 startTrackingInChilds,
																		   			 _fieldAcceptCriteria);
		}
		/**
		 * Descongela un objeto y sus objetos dependientes
		 * @param freezableObj el objeto freezable
		 */
		public static void stopTrackingChangesInState(DirtyStateTrackable trackableObj,
													  final boolean stopTrackingInChilds) {
			ObjectsHierarchyModifier.<DirtyStateTrackable>changeObjectHierarchyState(trackableObj,new TypeRef<DirtyStateTrackable>() {/* nothing */},
																		   			 new DirtyTrackingStatusModifierFunction(false),
																		   			 stopTrackingInChilds,
																		   			 _fieldAcceptCriteria);
		}
	}
}

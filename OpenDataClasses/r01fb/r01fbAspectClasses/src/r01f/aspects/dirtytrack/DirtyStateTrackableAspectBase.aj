package r01f.aspects.dirtytrack;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.reflect.FieldSignature;

import r01f.aspects.core.dirtytrack.DirtyTrackingStatusImpl;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.NotDirtyStateTrackable;

/**
 * Base aspect for the dirty tracking behabiour
 * Here the pointcuts are defined and reused at the aspects
 * @param <D> type extending {@link DirtyStateTrackable}
 */
privileged public abstract aspect DirtyStateTrackableAspectBase<D extends DirtyStateTrackable> {
	
//	pointcut simpleMarshallerMappingsFromAnnotationsLoaderMethodExecution() : execution(* SimpleMarshallerMappingsFromAnnotationsLoader.*(..));
//	pointcut notInsimpleMarshallerMappingsFromAnnotationsLoaderFlow() : !cflow(simpleMarshallerMappingsFromAnnotationsLoaderMethodExecution()) && !within(r01f.aspects.dirtytrack.*);
	
/////////////////////////////////////////////////////////////////////////////////////////
//  PointCuts reutilizables
/////////////////////////////////////////////////////////////////////////////////////////	
	/** 
	 * {@link DirtyStateTrackable} object creation
	 */
	pointcut newDirtyStateTrackableObjectCreation() : 
					   execution((DirtyStateTrackable+).new(..)) 	// DirtyStateTrackable implementing object constructor
					&& !within(r01f.types.dirtytrack..*);			// for types NOT in r01f.types.. package
	/**
	 * {@link DirtyStateTrackable} modification
	 * @param obj {@link DirtyStateTrackable} implementing object
	 * @param newVal the new value to be set into the member
	 */
	pointcut fieldSetInDirtyStateTrackableObj(D trck,Object newVal) : 
							(set(!static !final * DirtyStateTrackable+.*) 					// any non static non final DirtyStateTrackable-type member...
								&& !set(* *._tracking*)										// ... that it's NOT either _trackingStatus or _trackingMapChangesTracker members 
			 					&& !set(@NotDirtyStateTrackable * DirtyStateTrackable.*)	// ... nor annotated with NotDirtyStateTrackable
			 				)
							&& !within(r01f.types.dirtytrack..*)	// to be used on types NOT in package r01f.types..
							&& target(trck)							// the object containing the memenber
							&& args(newVal);						// the new member value
/////////////////////////////////////////////////////////////////////////////////////////
//	DirtyStateTrackable OBJECTS CREATION
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Advice after: this is executed AFTER a new {@link DirtyStateTrackable} object creation 
	 */
	after (D trck) returning : 
				newDirtyStateTrackableObjectCreation()
			 && this(trck) {
		trck.getTrackingStatus().setThisNew(true);	// El objeto es nuevo...
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CAPTURA DE LAS MODIFICACIONES SOBRE LOS OBJETOS SIMPLES
//  QUE IMPLEMENTAN DirtyStateTrackable
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Advice after: se ejecuta ANTES de establecer el valor de un miembro en un objeto DirtyStateTrackable
	 */
	before(D trck,Object newValue) : 
				fieldSetInDirtyStateTrackableObj(trck,newValue) {
		// Obtener el field que se ha modificado
		FieldSignature fs = (FieldSignature)thisJoinPointStaticPart.getSignature();
		Field field = fs.getField();
		
		// ejecutar la l�gica del aspecto
		DirtyTrackingStatusImpl._beforeSetMember(trck,field,newValue); 
	}
	/**
	 * Advice after: se ejecuta DESPUES de establecer el valor de un miembro en un objeto DirtyStateTrackable
	 */
	after(D trck,Object newValue) : 
				fieldSetInDirtyStateTrackableObj(trck,newValue) {
		// Obtener el field que se ha modificado
		FieldSignature fs = (FieldSignature)thisJoinPointStaticPart.getSignature();
		Field field = fs.getField();
		
		// ejecutar la l�gica del aspecto
		DirtyTrackingStatusImpl._afterSetMember(trck,field,newValue);
	}

/////////////////////////////////////////////////////////////////////////////////////////
//	ASIGNACI�N DE COLECCIONES (Map / Collection)
// 	Cuando se intenta asignar un miembro tipo Map o Collection, se "pega" el cambiazo
// 	por otro que "envuelve" al original y que lleva la cuenta de las modificaciones
// 	y adem�s es cap/az de "informar" al objeto que contiene el Map o Collection de que se
//  ha producido una modificaci�n en el mapa para que establezca su estado a dirty
//	NOTA: Los advices se podr�an definir como 
// 				Map around() : get(Map+ DirtyStateTrackable+.*) 
// 		  pero da un ERROR cuando la colecci�n es un objeto que extiende de una colecci�n
// 		  Ej: 
// 				public class MyObj extends HashMap.
//	
//
// IMPORTANTE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// Hay que tener en cuenta que hay dos formas de invocar un m�todo mutator de un mapa o colecci�n 
// que es un miembro (field) de un objeto Container:
// 		public class Container {
// 			public Map _mapField;
// 		}
// CASO 1: Desde FUERA del objeto que contiene el miembro colecci�n (map o collection):
// 			public class OtherObj {
// 				public void method() { 
// 					container.getMapField().put(..) <-- el pointcut est� en el objeto/m�todo que hace la llamada a put(..) en el Map,
// 				}										es decir en el objeto OtherObj y NO en el objeto container que contiene el Map 
// 			}											
// CASO 2: Desde FUERA del objeto que contiene el miembro colecci�n (map o collection):
// 			public class OtherObj {
// 				public void method() {
// 					container.putIntoMapField(..) <-- el m�todo putIntoMapField del objeto container es el que 
// 				}								 	  hace la llamada al m�todo put(..) del Map as� que ah� est� el pointcut
// 			} 
// Esto implica que en un solo pointcut NO se puede tener acceso al objeto que contiene el objeto colecci�n (map / collection), 
// unicamente se puede obtener el objeto colecci�n y los argumentos del m�todo mutator
// 
// Esta implementaci�n es compleja, as� que se ha optado por capturar los accesos a los miembros tipo Map y devolver un wrapper
// del mapa que "lleva" el control de los cambios y es capaz de informar al objeto que contiene el mapa de que hay cambios en 
// el mapa.
/////////////////////////////////////////////////////////////////////////////////////////
    Object around(D trck) : 
    			get((!DirtyStateTrackable && Map+) DirtyStateTrackable+.*)	// que NO sea de un tipo que est� en el paquete r01f.types..
    		 && !within(r01f.types.dirtytrack..*)	// que NO sea de un tipo que est� en el paquete r01f.types..
    		 && target(trck) {						// el objeto que tiene el miembro
		// Obtener el mapa 
    	Map theMap = (Map)proceed(trck);			// obtener el mapa subyacente
    	
    	// Obtener el field tipo Map que se est� accediendo
		FieldSignature fs = (FieldSignature)thisJoinPointStaticPart.getSignature();
		Field f = fs.getField();
    	
		// ejecutar la l�gica del aspecto
    	return DirtyTrackingStatusImpl._arroundMapFieldGet(trck,f,theMap);
    }
    Object around(D trck) : 
    			get(Collection+ D+.*) 				// acceso a un miembro tipo Collection de un tipo DirtyStateTrackable
    		 && !within(r01f.types.dirtytrack..*)	// que NO sea de un tipo que est� en el paquete r01f.types..
    		 && target(trck) {						// el objeto que tiene el miembro				
		// Obtener la colecci�n
    	Collection theCol = (Collection)proceed(trck);	// Obtener la colecci�n subyacente
    	
    	// Obtener el field tipo Collection que se est� accediendo
		FieldSignature fs = (FieldSignature)thisJoinPointStaticPart.getSignature();
		Field field = fs.getField();
		
		// ejecutar la l�gica del aspecto
		return DirtyTrackingStatusImpl._arroundCollectionFieldGet(trck,field,theCol);
    }
    Object around(D trck) : 
    			get(List+ D+.*) 						// acceso a un miembro tipo List de un tipo DirtyStateTrackable
    		 && !within(r01f.types.dirtytrack..*) 		// que NO sea de un tipo que est� en el paquete r01f.types..
    		 && target(trck) {							// el objeto que tiene el miembro	
		// Obtener la Lista
    	List theList = (List)proceed(trck);	// obtener la lista subyacente
    	
    	// Obtener el field tipo List que se est� accediendo
		FieldSignature fs = (FieldSignature)thisJoinPointStaticPart.getSignature();
		Field field = fs.getField();
		
		// ejecutar la l�gica del aspecto
		return DirtyTrackingStatusImpl._arroundCollectionFieldGet(trck,field,theList);
    }
    Object around(D trck) :
    			get(Set+ D+.*) 				// acceso a un miembro tipo Set de un tipo DirtyStateTrackable
    		 && !within(r01f.types.dirtytrack..*) 	// que NO sea de un tipo que est� en el paquete r01f.types..
    		 && target(trck) {						// el objeto que tiene el miembro
		// Obtener el Set
    	Set theSet = (Set)proceed(trck);	// Obtener el conjunto subyacente
    	
    	// Obtener el field tipo Set que se est� accediendo
		FieldSignature fs = (FieldSignature)thisJoinPointStaticPart.getSignature();
		Field field = fs.getField();
    	
		// ejecutar la l�gica del aspecto
		return DirtyTrackingStatusImpl._arroundCollectionFieldGet(trck,field,theSet);
    }
}

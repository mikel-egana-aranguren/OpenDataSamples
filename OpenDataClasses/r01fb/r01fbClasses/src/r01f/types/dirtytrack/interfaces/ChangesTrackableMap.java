package r01f.types.dirtytrack.interfaces;

import java.util.Map;
import java.util.Set;

import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.types.dirtytrack.internal.CollectionChangesTracker;


/**
 * Interfaz que verifican los mapas que llevan control de los cambios
 * <pre>
 * IMPORTANTE!!	NO implementa el interfaz Map ya que de otra forma NO se puede reutilizar en el aspecto ConvertToDirtyStateTrackableAspect
 * 				para a�adir el interfaz ChangesTrackableMap a cualquier tipo que extienda de Map<K,V>; el weaver NO permite a�adir un 
 * 				interfaz a una clase que esta ya tiene (en este caso java.util.Map)
 * 					Cannot declare parent ChangesTrackableMap onto type XXX since it already has java.util.Map<K,V> in its hierarchy
 * </pre> 
 * @param <K>
 * @param <V>
 */
public interface ChangesTrackableMap<K,V> 
	     extends DirtyStateTrackable {
	/**
	 * @return el objeto {@link CollectionChangesTracker} que lleva el control de los cambios en el mapa
	 */
	public CollectionChangesTracker<K> getChangesTracker();
///////////////////////////////////////////////////////////////////////////////////////////////////
//	OBTENCI�N DE LAS CLAVES 
///////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Devuelve las claves de las entradas nuevas que se han a�adido a la colecci�n original
	 * @return las claves de las entradas nuevas
	 */
	public Set<K> newKeys();
	/**
	 * Devuelve una colecci�n con las claves eliminadas de la colecci�n original
	 * @return las claves eliminadas de la colecci�n original
	 */
	public Set<K> removedKeys();
	/**
	 * Devuelve una coleci�n con las claves que NO son nuevas ni se han eliminado
	 * del mapa original, es decir, las claves originales
	 * @return las claves de las entradas NO nuevas ni eliminadas (las originales)
	 */
	public Set<K> notNewOrRemovedKeys();
///////////////////////////////////////////////////////////////////////////////////////////////////
//	OBTENCI�N DE LAS ENTRADAS 
//	NOTA:	Las entradas BORRADAS NO se pueden obtener ya que �nicamente se "guarda" la clave
///////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Devuelve las entradas nuevas de entre las actuales
	 * @return las entradas nuevas
	 */
	public Map<K,V> newEntries();
	/**
	 * Devuelve una coleci�n con las entradas que NO son nuevas ni se han eliminado
	 * del mapa original, es decir, las entradas originales
	 * @return las claves de las entradas NO nuevas ni eliminadas (las originales)
	 */
	public Map<K,V> notNewOrRemovedEntries();
}

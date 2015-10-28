package r01f.types.dirtytrack.interfaces;

import java.util.Map;
import java.util.Set;

public interface ChangesTrackableLazyMap<K,V>
         extends ChangesTrackableMap<K,V> {
	/**
	 * Devuelve una coleci�n con las claves que NO son nuevas ni se han eliminado
	 * del mapa original, es decir, las claves originales, pero SOLO de entre aquellas
	 * que est�n CARGADAS en el mapa debido a que se han accedido en alg�n momento
	 * @return las claves de las entradas NO nuevas ni eliminadas (las originales)
	 */
	public Set<K> notNewOrRemovedKeys(boolean onlyLoaded);
	/**
	 * Devuelve un mapa con las entradas que NO son nuevas ni se han eliminado
	 * del mapa original, es decir, las entradas originales, pero SOLO de entre aquellas
	 * que est�n CARGADAS en el mapa debido a que se han accedido en alg�n momento
	 * @return las entradas NO nuevas ni eliminadas (las originales)
	 */
	public Map<K,V> notNewOrRemovedEntries(boolean onlyLoaded);
	/**
	 * Devuelve un mapa con las entradas que NO son nuevas ni se han eliminado y que 
	 * adem�s se han modificado, es decir, de las entradas originales CARGADAS, aquellas
	 * que han sido modificadas.
	 * IMPORTANTE!!!	
	 * Este m�todo solo devuelve valores si V es una instancia de {@link r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable}
	 * @return las entradas modificadas
	 */
	public Map<K,V> notNewOrRemovedDirtyEntries();
}

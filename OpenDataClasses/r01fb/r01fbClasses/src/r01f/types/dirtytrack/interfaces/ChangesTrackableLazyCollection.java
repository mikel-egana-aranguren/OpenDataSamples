package r01f.types.dirtytrack.interfaces;

import java.util.Set;

public interface ChangesTrackableLazyCollection<V> 
         extends ChangesTrackableCollection<V> {
	/**
	 * Devuelve una coleci�n con las entradas que NO son nuevas ni se han eliminado
	 * de la colecci�n original, es decir, los elementos originales, pero SOLO de entre 
	 * aquellos que est�n CARGADAS en la colecci�n debido a que se han accedido en alg�n 
	 * momento
	 * @return las entradas NO nuevas ni eliminadas (las originales)
	 */
	public Set<V> notNewOrRemovedEntries(boolean onlyLoaded);

}

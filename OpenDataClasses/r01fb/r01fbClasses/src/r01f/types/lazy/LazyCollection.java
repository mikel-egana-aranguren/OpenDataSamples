package r01f.types.lazy;

import java.util.Collection;

import org.apache.commons.collections.list.LazyList;

import r01f.types.dirtytrack.interfaces.ChangesTrackableLazyCollection;


/**
 * LazilyLoaded {@link Collection} marker interface
 * see {@link LazyList} 
 */
public interface LazyCollection<V> 
         extends ChangesTrackableLazyCollection<V> {
	/* nothing */
}

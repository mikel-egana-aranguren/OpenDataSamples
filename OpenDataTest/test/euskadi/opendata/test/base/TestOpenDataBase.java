package euskadi.opendata.test.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import r01f.util.types.collections.CollectionUtils;

import com.ejie.r01m.objects.searchengine.results.R01MSearchResultItemStructureCatalog;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

import euskadi.opendata.util.Labels;

public abstract class TestOpenDataBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static final String TEMA_SUBTEMA_STRUCTURE = "r01e00000fe4e6676dda470b898e584a4a1047312";
	private static final String ESTRUCTURA_CONTRATACION = "contrat";
	private static final String ESTRUCTURA_AUTONOMICA = "estrc";
	private static Map<String,Labels> LABELS;
	static {
		LABELS = Maps.newHashMap();
		LABELS.put(TEMA_SUBTEMA_STRUCTURE,new Labels("temas_subtemas.csv"));
		LABELS.put(ESTRUCTURA_CONTRATACION,new Labels("estructura_contratacion.csv"));
		LABELS.put(ESTRUCTURA_AUTONOMICA,new Labels("estructura_autonomica.csv"));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected static Map<String,String> _labelsInTemaSubTemaStructure(final List<R01MSearchResultItemStructureCatalog> cats,
															   		  final String role) {
		return _labelsInStructure(cats,
								  TEMA_SUBTEMA_STRUCTURE,role);
	}
	protected static Map<String,String> _labelsInContratacionStructure(final List<R01MSearchResultItemStructureCatalog> cats,
															    	   final String role) {
		return _labelsInStructure(cats,
								  ESTRUCTURA_CONTRATACION,role);
	}
	/**
	 * Returns a map with the labels of a certain structure indexed by their oid
	 * @param cats
	 * @param structureOid
	 * @param role
	 * @return
	 */
	private static Map<String,String> _labelsInStructure(final List<R01MSearchResultItemStructureCatalog> cats,
														 final String structureOid,final String role) {
		if (CollectionUtils.isNullOrEmpty(cats)) return null;
		Collection<R01MSearchResultItemStructureCatalog> roleCats = FluentIterable.from(cats)
																			      .filter(new Predicate<R01MSearchResultItemStructureCatalog>() {
																									@Override
																									public boolean apply(final R01MSearchResultItemStructureCatalog cat) {
																										return cat.getStructureOid().equals(structureOid)
																											&& cat.getRole().equals(role);
																									}
																				   		   })
																			      .toList();
		// Get the labels descriptions
		Map<String,String> outCats = new HashMap<String,String>(roleCats.size()); 
		Labels labels = LABELS.get(structureOid);		
		for (R01MSearchResultItemStructureCatalog cat : roleCats) {
			String labelOid = cat.getLabelOid();
			String label = labels.getByOid(labelOid);
			if (label != null) outCats.put(labelOid,label);
		}
		return outCats;
	}
}

package r01f.model.facets.delegates;

import r01f.model.facets.LangNamed;
import r01f.model.facets.LangNamed.HasLangNamedFacet;

/**
 * Encapsulates the {@link LangNamed} behavior
 * @param <L>
 */
public class LangNamedDelegate<L extends HasLangNamedFacet>
	 extends FacetDelegateBase<L>
  implements LangNamed {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public LangNamedDelegate(final L hasLangNamedFacet) {
		super(hasLangNamedFacet);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getName() {
		return _modelObject.getName();
	}
	@Override
	public void setName(final String name) {
		_modelObject.setName(name);
	}
}

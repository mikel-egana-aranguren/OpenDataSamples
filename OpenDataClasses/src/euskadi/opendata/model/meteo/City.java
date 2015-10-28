package euskadi.opendata.model.meteo;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.enums.EnumWithCodeAndLabel;
import r01f.enums.EnumWithCodeAndLabelWrapper;

@Accessors(prefix="_")
public enum City 
 implements EnumWithCodeAndLabel<String,City> {
	LAGUARDIA("24","Laguardia"),
	DONOSTI("18","Donostia-San Sebastian"),
	BILBAO("2","Bilbao"),
	ARRASATE("23","Arrasate/Mondragón"),
	GASTEIZ("19","Vitoria-Gasteiz"),
	PAMPLONA("17","Pamplona/Iruña");

	@Getter private final Class<String> _codeType = String.class;
	@Getter private String _code;
	@Getter private String _label;
	
	private City(final String code,final String name) {
		_code = code;
		_label = name;
	}
	private static final EnumWithCodeAndLabelWrapper<String,City> WRAPPER = EnumWithCodeAndLabelWrapper.create(City.class);


	@Override
	public boolean isIn(final City... els) {
		return WRAPPER.isIn(this,els);
	}
	@Override
	public boolean is(final City el) {
		return WRAPPER.is(this,el);
	}
	@Override
	public boolean canBeFrom(final String label) {
		return WRAPPER.canBeFrom(label);
	}
	public static City fromCode(final String code) {
		return WRAPPER.fromCode(code);
	}
}

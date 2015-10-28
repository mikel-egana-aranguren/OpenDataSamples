package euskadi.opendata.model.meteo;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.enums.EnumWithCode;
import r01f.enums.EnumWithCodeWrapper;

@Accessors(prefix="_")
public enum Day 
 implements EnumWithCode<String,Day> {
	TODAY("today"),
	TOMORROW("tomorrow"),
	NEXT("next");
	
	@Getter private final Class<String> _codeType = String.class;
	@Getter private final String _code;
	
	private Day(final String code) {
		_code = code;
	}
	
	private static final EnumWithCodeWrapper<String,Day> WRAPPER = EnumWithCodeWrapper.create(Day.class);

	@Override
	public boolean isIn(final Day... els) {
		return WRAPPER.isIn(this,els);
	}
	@Override
	public boolean is(final Day el) {
		return WRAPPER.is(this,el);
	}
	public static Day fromCode(final String code) {
		return WRAPPER.fromCode(code);
	}
	public static Day fromName(final String name) {
		return WRAPPER.fromName(name);
	}
}

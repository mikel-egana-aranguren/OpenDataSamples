package euskadi.opendata.model;

import java.util.Map;

import euskadi.opendata.CSVRepresentable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.types.Path;
import r01f.util.types.Strings;

///////////////////////////////////////////////////////////////////////////////////////////////////
//	ITEM FOR THE RETURNED DATA
///////////////////////////////////////////////////////////////////////////////////////////////////	
	@Accessors(prefix="_") 
public class ServiceData 
  implements CSVRepresentable,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static final String SEP = "|";
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter @Setter private String _contentName;
	@Getter @Setter private String _lang;
	@Getter @Setter private String _langVersionName;
	@Getter @Setter private String _procedureStatus;
	@Getter @Setter private Map<String,Path> _reusableAssets;
	@Getter @Setter private String _dataFileXML;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////
//  CSV CONVERSION
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String asCSVRow() {
		return Strings.create()
					  .add(_contentName).add(SEP)
					  .add(_lang).add(SEP)
					  .add(_langVersionName).add(SEP)
					  .add(_procedureStatus)
					  .asString();
	}
	public static String csvHEAD() {
		return Strings.create()
				  .add("CONTENT").add(SEP)
				  .add("LANG").add(SEP)
				  .add("NAME").add(SEP)
				  .add("STATUS")
				  .asString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String debugInfo() {
		return Strings.create().add("     Content Name: ").addLine(_contentName)
							   .add("Lang Version Name: ").add("(").add(_lang).add(")").addLine(_langVersionName)
							   .add(" Procedure status: ").addLine(_procedureStatus)
							   .add("  Reusable Assets: ").addLine((_reusableAssets != null ? Integer.toString(_reusableAssets.size()) : ""))
//								   .add("     DataFile XML: ").add(_dataFileXML)
							   .asString();
	}
}
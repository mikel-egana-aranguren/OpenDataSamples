package euskadi.opendata.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import euskadi.opendata.CSVRepresentable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.util.types.Strings;

/**
 * Procurement data
 */
@Accessors(prefix="_") 
public class ProcurementData 
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
	@Getter @Setter private String _tituloContrato;
	@Getter @Setter private String _entityOid;
	@Getter @Setter private String _entityName;
	@Getter @Setter private double _value;
	@Getter @Setter private String _dataFileXMLUrl;
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
					  .add(_formatValue(_value)).add(SEP)
					  .add(_lang).add(SEP)
					  .add(_tituloContrato).add(SEP)
					  .add(_entityOid).add(SEP)
					  .add(_entityName).add(SEP)
					  .add(_dataFileXMLUrl)
					  .asString();
	}
	public static String csvHEAD() {
		return Strings.create()
				  .add("CONTENT").add(SEP)
				  .add("VALUE").add(SEP)
				  .add("LANG").add(SEP)
				  .add("TITULO_CONTRATO").add(SEP)
				  .add("ENTITY_OID").add(SEP)
				  .add("ENTITY").add(SEP)
				  .add("DATAFILE_URL")
				  .asString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String debugInfo() {
		return Strings.create().add("     Content Name: ").addLine(_contentName)
							   .add("            Value: ").addLine(_formatValue(_value))
							   .add("  Titulo contrato: ").add("(").add(_lang).add(")").addLine(_tituloContrato)
							   .add("       Entity OID: ").addLine(_entityOid)
							   .add("           Entity: ").addLine(_entityName)
							   .add("     DataFile URL: ").addLine(_dataFileXMLUrl)
							   .asString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static String _formatValue(final double value) {
	    // Conversor
	    DecimalFormat df = new DecimalFormat("###,###.##");
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(',');
		dfs.setMonetaryDecimalSeparator(',');
		dfs.setGroupingSeparator('.');
		df.setDecimalFormatSymbols(dfs);
		
		return df.format(value);
	}
}
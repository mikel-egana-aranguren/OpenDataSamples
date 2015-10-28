package euskadi.opendata.model;

import java.util.Map;

import euskadi.opendata.CSVRepresentable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * DataSet data
 */
@Accessors(prefix="_") 
public class OpenDataDataSet 
  implements CSVRepresentable,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static final String SEP = "|";
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter @Setter private String _typo;
	@Getter @Setter private String _contentName;
	@Getter @Setter private String _description;
	@Getter @Setter private String _lang;
	@Getter @Setter private String _langVersionName;
	@Getter @Setter private String _source;
	@Getter @Setter private String _urlFicha;
	@Getter @Setter private String _urlFicha2;
	@Getter @Setter private String _fechaCreacion;
	@Getter @Setter private String _fechaDifusion;
	@Getter @Setter private String _periodoInicio;
	@Getter @Setter private String _periodoFin;
	@Getter @Setter private String _periodicidad;
	@Getter @Setter private String _tags;
	@Getter @Setter private String _formats;
	@Getter @Setter private String _tema;
	@Getter @Setter private Map<String,String> _temas;
	@Getter @Setter private Map<String,String> _subTemas;
	@Getter @Setter private String _files;
	@Getter @Setter private String _oficialidadEstadistica;		
	@Getter @Setter private Map<String,Path> _reusableAssets;
	@Getter @Setter private String _dataFileXML;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CSV CONVERSION
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String asCSVRow() {
		return Strings.create()
					  .add(_typo).add(SEP)
					  .add(_contentName).add(SEP)
					  .add(_lang).add(SEP)
					  .add(_langVersionName).add(SEP)
					  .add(_description).add(SEP)
					  .add(_source).add(SEP)
					  .add(_urlFicha).add(SEP)
					  .add(_urlFicha2).add(SEP)
					  .add(_fechaCreacion).add(SEP)
					  .add(_fechaDifusion).add(SEP)
					  .add(_periodicidad).add(SEP)
					  .add(_periodoInicio).add(SEP)
					  .add(_periodoFin).add(SEP)
					  .add(_tags).add(SEP)
					  .add(_tema).add(SEP)
					  .add(CollectionUtils.of(_temas.keySet()).toStringCommaSeparated()).add(SEP)
					  .add(CollectionUtils.of(_subTemas.keySet()).toStringCommaSeparated()).add(SEP)
					  .add(CollectionUtils.of(_temas.values()).toStringCommaSeparated()).add(SEP)
					  .add(CollectionUtils.of(_subTemas.values()).toStringCommaSeparated()).add(SEP)
					  .add(_files).add(SEP)
					  .add(_oficialidadEstadistica)
					  .asString();
	}
	public static String csvHEAD() {
		return Strings.create()
				  .add("TYPO").add(SEP)
				  .add("CONTENT").add(SEP)
				  .add("LANG").add(SEP)
				  .add("NAME").add(SEP)
				  .add("DESCRIPTION").add(SEP)
				  .add("SOURCE").add(SEP)
				  .add("URL1").add(SEP)
				  .add("URL2").add(SEP)
				  .add("CREACION").add(SEP)
				  .add("DIFUSION").add(SEP)
				  .add("PERIODICIDAD").add(SEP)
				  .add("PERIODO_INCIO").add(SEP)
				  .add("PERIODO_FIN").add(SEP)
				  .add("TAGS").add(SEP)
				  .add("OID_TEMAS").add(SEP)
				  .add("OID_SUBTEMAS").add(SEP)
				  .add("NAMES_TEMAS").add(SEP)
				  .add("NAMES_SUBTEMAS").add(SEP)
				  .add("SUBTEMAS").add(SEP)
				  .add("FICHEROS").add(SEP)
				  .add("ESTADISTICA_OFICIAL")
				  .asString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public String debugInfo() {
		return Strings.create().add("             Typo: ").addLine(_typo)
							   .add("     Content Name: ").addLine(_contentName)
							   .add("Lang Version Name: ").add("(").add(_lang).add(")-").addLine(_langVersionName)
							   .add("      Descripcion: ").addLine(_description)
							   .add("        URL ficha: ").addLine(_urlFicha)
							   .add("       URL ficha2: ").addLine(_urlFicha2)
							   .add("           Fuente: ").addLine(_source)
							   .add("   Fecha Creación: ").addLine(_fechaCreacion)
							   .add("   Fecha Difusion: ").addLine(_fechaDifusion)
							   .add("          Periodo: ").add(_periodicidad).add(" > ").add(_periodoInicio).add(" - ").addLine(_periodoFin)
							   .add("             Tags: ").addLine(_tags)
							   .add("             Tema: ").addLine(_tema)
							   .add("       OIDs Temas: ").addLine(CollectionUtils.of(_temas.keySet()).toStringCommaSeparated())
							   .add("    OIDs SubTemas: ").addLine(CollectionUtils.of(_subTemas.keySet()).toStringCommaSeparated())
							   .add("            Temas: ").addLine(CollectionUtils.of(_temas.values()).toStringCommaSeparated())
							   .add("         SubTemas: ").addLine(CollectionUtils.of(_subTemas.values()).toStringCommaSeparated())
							   .add("            Files: ").addLine(_files)
							   .add("  Reusable Assets: ").addLine((_reusableAssets != null ? Integer.toString(_reusableAssets.size()) : ""))
							   .add(" Estadist Oficial: ").addLine(_oficialidadEstadistica)
							   .add("     DataFile XML: ").add(_dataFileXML)
							   .asString();
	}
}
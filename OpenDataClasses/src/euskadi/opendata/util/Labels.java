package euskadi.opendata.util;

import java.io.IOException;
import java.util.Map;

import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Maps;

/**
 * Parsea las etiquetas de catalogación desde un fichero CSV
 */
public class Labels {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final Map<String,String> _labels;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public Labels(final String csvPath) {
		_labels = _loadLabels(csvPath); 	// temas_subtemas, estructura_autonomica, estructura_contratacion
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public String getByOid(final String oid) {
		return _labels.get(oid);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static Map<String,String> _loadLabels(final String filePath) {
		System.out.println("==>Loading labels from: " + filePath);
		Map<String,String> outLabels = null;
		try {
			ResourcesLoader resLoader = ResourcesLoaderBuilder.createDefaultResourcesLoader();
			String[] lines = Strings.of(resLoader.getInputStream(filePath)).getLines();
			if (CollectionUtils.hasData(lines)) {
				outLabels = Maps.newHashMapWithExpectedSize(lines.length);
				for (String line : lines) {
					String[] lineTokenized = line.split("\\|");
					outLabels.put(lineTokenized[1].trim(),lineTokenized[0]);
				}
				// Debug
				for (Map.Entry<String,String> me : outLabels.entrySet()) {
					System.out.println("\t-" + me.getKey() + " > " + me.getValue());
				}
			}
		} catch(IOException ioEx) {
			ioEx.printStackTrace();
		}
		return outLabels;
	}
}

package euskadi.opendata.test.procurements;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

abstract class TestProcurementsMaxMinBase 
	   extends TestProcurementsBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	TestProcurementsMaxMinBase(){
		super();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads every procurement data and computes the min & max procurement
	 * @param procurementDataUrls
	 */
	protected static void _computeMinAndMax(final Map<String,String> procurementDataUrls) {
		try {
			if (procurementDataUrls != null) {
				// [1]: Use a map indexed by procurement name to avoid possible duplicate entries
				Map<String,Double> values = new HashMap<String,Double>();
				double total = 0; 
				for (Map.Entry<String,String> procurement : procurementDataUrls.entrySet()) {
					String procContent = procurement.getKey();
					String procDataFileUrl = procurement.getValue();
					
					// Download the data file and get some data
					Procurement procData = _procurementDataFor(procContent,procDataFileUrl);
					
					System.out.println("--->" + procData.getValue() + " > " + procData.getTitle());
					
					total = total + procData.getValue();
					
					
					values.put(procData.getTitle(),procData.getValue());
				}
			    
				// [2]: Find the min & max procurement
			    double minValue = Collections.min(values.values());
				double maxValue = Collections.max(values.values());
				
				// Debug
				System.out.println("\n\n\n\n\n\n\n\n\n\n");
				System.out.println("El presupuesto menor de todas las contraciones administrativas de Euskadi.eus es de \"" + minValue + "\" € para la contratación \"" + values.get(minValue) + "\"");
			    System.out.println("El presupuesto mayor de todas las contraciones administrativas de Euskadi.eus es de \"" + maxValue + "\" € para la contratación \"" + values.get(maxValue) + "\"");
			    System.out.println("El presupuesto TOTAL de todas las contraciones administrativas de Euskadi.eus es de \"" + _formatValue(total) + "\" €");				
			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
		} catch (SAXException e) {
			e.printStackTrace(System.out);
		} catch (XPathExpressionException e) {
			e.printStackTrace(System.out);
		}
	}
}

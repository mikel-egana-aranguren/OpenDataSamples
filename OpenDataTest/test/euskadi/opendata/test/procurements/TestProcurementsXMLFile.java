package euskadi.opendata.test.procurements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import r01f.util.types.Strings;

/**
 * Process all procurements the hard way
 * http://opendata.euskadi.eus/contenidos/ds_contrataciones/contrataciones_admin_2015/opendata/contratos.xml
 */
public class TestProcurementsXMLFile 
     extends TestProcurementsMaxMinBase {

	public static void main(String[] args) {
		try {
			Map<String,String> procurementDataUrls = null;
			
			// [1]: Download the procurements dataset and get a list of every procurement data
			URL url = new URL("http://opendata.euskadi.eus/contenidos/ds_contrataciones/contrataciones_admin_2015/opendata/contratos.xml");			
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			
			// Process file retaining only the <dataxml>...</dataxml> lines
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.indexOf("<dataxml>") != -1) {
					if (procurementDataUrls == null) procurementDataUrls = new HashMap<String,String>();
					// remove the tags <datosxml>.
					String dataXmlUrl = inputLine.replace("<dataxml>","")
													 .replace("</dataxml>", "");
					String contentName = Strings.of(dataXmlUrl)		// http://opendata.euskadi.eus/contenidos/anuncio_contratacion/expx74j18373/es_doc/data/es_r01dpd0014eba22a3531a994e1d4cec560118c96a
												.match(".*/anuncio_contratacion/([^/]+)/.*")
												.group(1);
					System.out.println(">" + contentName + ": " + dataXmlUrl);
					procurementDataUrls.put(contentName,dataXmlUrl);
				}
			}
			in.close();
			
			System.out.println("\n\n\n\n\n");
			
			
			// [2]: Download every procurement data file, parse it and get it's value 
			_computeMinAndMax(procurementDataUrls);
			
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}

	}
}

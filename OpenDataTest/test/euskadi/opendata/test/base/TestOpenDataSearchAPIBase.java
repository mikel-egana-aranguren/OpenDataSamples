package euskadi.opendata.test.base;

import java.io.IOException;
import java.io.Writer;

import r01f.httpclient.HttpClient;
import r01f.types.Path;

import com.ejie.r01m.objects.searchengine.results.R01MSearchResultItem;
import com.google.common.base.Throwables;

import euskadi.opendata.CSVRepresentable;

public abstract class TestOpenDataSearchAPIBase
			  extends TestOpenDataBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * Buscar el datafile (esta parte NO estaba pensada para ser utilizad en OpenData, pero 
	 * es prácticamente la única opción que hay WTF!)
	 * item.getDocumentDataFilesGeneratedFilesDocumentRelativePaths() es un Mapa
	 * que relaciona los oids de los datafiles (que es lo que se necesita) con el fichero HTML
	 * generado
	 * Ej:	<documentDataFilesGeneratedFilesDocumentRelativePaths>
	 *			<r01dpd013a780b8c401e41497a285409dc514c34e>
	 *				<![CDATA[ procedimiento_ayuda_v2;main:abandono_2010.html ]]>
	 *			</r01dpd013a780b8c401e41497a285409dc514c34e>
	 *		</documentDataFilesGeneratedFilesDocumentRelativePaths>
	 * (se necesita el oid: r01dpd013a780b8c401e41497a285409dc514c34e)
	 */
	protected static String _downloadDataFileXML(final R01MSearchResultItem item) throws IOException {
		Path dataFilePath = item.getReusableDataFileContentRelativePath();
		String dataFileXML = null;
		if (dataFilePath != null) {
			String dataFileURL = "http://opendata.euskadi.eus/contenidos/" + item.getDocumentWorkAreaRelativePath() + dataFilePath.asAbsoluteString();
			dataFileXML = HttpClient.forUrl(dataFileURL)
									.GET()
					                .loadAsString();
			dataFileXML = dataFileXML.trim();
		}
		return dataFileXML;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Writes the data to a file
	 * @param okWriter
	 * @param errWriter
	 * @param data
	 * @throws IOException
	 */
	protected static void _writeRow(final Writer okWriter,
							        final CSVRepresentable data) {
		try {
			okWriter.append(data.asCSVRow());
			okWriter.append("\n");
		} catch(IOException ioEx) {
			ioEx.printStackTrace(System.out);
		}
	}
	protected static void _writeError(final Writer errWriter,
									  final Throwable th) {
		try {
			errWriter.append(Throwables.getStackTraceAsString(th));
			errWriter.append("\n");
		} catch(IOException ioEx) {
			ioEx.printStackTrace(System.out);
		}
	}
}

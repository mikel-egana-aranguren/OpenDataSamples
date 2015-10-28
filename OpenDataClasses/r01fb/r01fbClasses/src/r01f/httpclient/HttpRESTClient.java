/**
 *
 */
package r01f.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import r01f.exceptions.Throwables;
import r01f.mime.MimeType;
import r01f.types.weburl.WebUrl;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Lists;

/**
 * Aux type to do REST client-calls.
 * Una caracter�stica completamente alineada con REST del protocolo HTTP es el hecho de que tenga
 * una interfaz uniforme para todos los recursos web. HTTP define un conjunto predefinido y cerrado
 * de acciones o m�todos HTTP. Es importante tener en cuenta que la propia especificaci�n define los
 * conceptos de seguridad e idempotencia, y clasifica los m�todos conforme a estos dos criterios.
 *
 * Un m�todo se considera seguro si no produce efectos secundarios. Por efecto secundario se entiende
 * cualquier modificaci�n del estado del servidor, o interacci�n de �ste con cualquier otro sistema, que
 * produzca efectos perceptibles por el usuario. Normalmente s�lo los m�todos que representan lectura
 * se consideran seguros.
 *
 * Un m�todo es idempotente si la ejecuci�n repetida de �ste, con exactamente los mismos par�metros,
 * tiene el mismo efecto que si s�lo se hubiera ejecutado una vez. Esta propiedad nos permite reintentar
 * con seguridad una petici�n una y otra vez, y tener la seguridad de que la operaci�n no se va a duplicar.
 *
 * Los m�todos (o tambi�n llamados verbos) HTTP usados com m�s frecuencia son los siguientes:
 * M�todo     Seguro Idempotente 	Sem�ntica
 * ------	  ------ -----------    ---------
 *  GET 		S� 		S� 			Leer el estado del recurso
 *  HEAD 		S� 		S� 			Leer, pero s�lo las cabeceras
 *  PUT 		No 		S� 			Actualizar o crear
 *  DELETE 		No 		S� 			Eliminar un recurso
 *  POST 		No 		No 			Cualquier acci�n gen�rica no idempotente
 *  OPTIONS 	S� 		S� 			Averiguar las opciones de comunicaci�n disponibles de un recurso
 *
 * En general el uso de cada m�todo es bastante explicativo. Sin embargo, conviene aclarar algunos aspectos.
 *
 * El primero es la diferencia entre HEAD y GET. Ambos leen el recurso, pero el segundo devuelve tanto
 * los datos del recurso web, como las cabeceras HTTP, mientras que el primero s�lo las cabeceras.
 *
 * Por otro lado el m�todo POST es bastante misterioso y objeto de frecuentes malentendidos. En general
 * se usa para crear un nuevo recurso, modificar uno existente o para ejecutar una acci�n gen�rica que
 * no sea idempotente como realizar una transacci�n monetaria. Como veis la sem�ntica de PUT parece
 * que se solapa con la de POST de alguna manera, y esto es fuente de numerosos malentendidos.
 *
 * El m�todo OPTIONS se usa para determinar las opciones de comunicaci�n de un recurso, tales como
 * qu� m�todos HTTP podemos usar contra esa URI. El resultado de OPTIONS no es cacheable, ya que
 * el conjunto de m�todos que podemos usar puede cambiar con el estado en el que se encuentre el
 * recurso. Junto con HEAD nos permiten descubrir autom�ticamente c�mo podemos comunicarnos con
 * el servidor.
 *
 * Existen otros m�todos que no suelen ser muy usados en servicios REST, como CONNECT o TRACE.
 */
public class HttpRESTClient {
	/**
	 * Makes a GET call to an URL, read the state of web resource, returns web resource data and HTTP headers.
	 * @param url the url to GET
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @return the server-returned result
	 */
	public static HttpResponse doGET(final WebUrl url,final Map<String,String> urlParameters,
									 final Map<String, String> headers) {
		HttpResponse outResponse = null;
		try {
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.GET()
										.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a HEAD call to an URL, read state of web resource, but only returns HTTP headers, not resource data.
	 * @param url the url to HEAD
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @return the server-returned result

	 */
	public static HttpResponse doHEAD(final WebUrl url,final Map<String,String> urlParameters,
									  final Map<String,String> headers) {
		HttpResponse outResponse = null;
		try {
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.HEAD()
										.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500,new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a DELETE call to an URL, delete resource.
	 * @param url the url to DELETE
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @return the server-returned result
	 */
	public static HttpResponse doDELETE(final WebUrl url,final Map<String,String> urlParameters,
										final Map<String,String> headers) {
		HttpResponse outResponse = null;
		try {
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.DELETE()
										.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a PUT call to an URL, create or update web resource (idempotent).
	 * @param url the url to PUT
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param postPayload the PUTed data
	 * @return the server-returned result
	 */
	public static HttpResponse doPUT(final WebUrl url,final Map<String,String> urlParameters,
									  final Map<String,String> headers,
									  final InputStream postPayload) {
		HttpResponse outResponse = HttpRESTClient.doPUT(url,urlParameters,
														headers,
														postPayload,MimeType.forName("application/xml"));
		return outResponse;
	}
	/**
	 * Makes a PUT call to an URL, create or update web resource (idempotent).
	 * @param url the url to PUT at
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param postPayload the PUTed data
	 * @param mimeType the content-type of the posted data (if null application/xml is assumed)
	 * @return the server-returned result
	 */
	public static HttpResponse doPUT(final WebUrl url,final Map<String,String> urlParameters,
									 final Map<String,String> headers,
									 final InputStream postPayload,
									 final MimeType mimeType) {
		HttpResponse outResponse = null;
		try {
			MimeType theMimeType = mimeType != null ? mimeType
													: MimeType.forName("application/xml");
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.PUT().withPayload(HttpRequestPayload.wrap(postPayload)
																		 .mimeType(theMimeType))
									.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a PUT call to an URL sending a FORM in the payload, create or update web resource (idempotent).
	 * @param url the url to PUT at
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param formParams the PUTed form data
	 * @return the server-returned result
	 */
	public static HttpResponse doPUTForm(final WebUrl url,final Map<String,String> urlParameters,
										 final Map<String,String> headers,
										 final Map<String,String> formParams) {
		HttpResponse outResponse = null;
		try {
			List<HttpRequestFormParameter> postFormParams = null;
			if (CollectionUtils.hasData(formParams)) {
				postFormParams = Lists.newArrayListWithExpectedSize(formParams.size());
				for (Map.Entry<String,String> me : formParams.entrySet()) {
					postFormParams.add(HttpRequestFormParameterForText.of(me.getValue())
															  .withName(me.getKey()));
				}
			}
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.PUTForm().withPUTFormParameters(postFormParams)
									.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a POST call to an URL, create or update web resource (not idempotent action).
	 * @param url the url to POST at
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param postPayload the POSTed data
	 * @param mimeType the content-type of the posted data (if null application/xml is assumed)
	 * @return the server-returned result
	 */
	public static HttpResponse doPOST(final WebUrl url,final Map<String,String> urlParameters,
									  final Map<String,String> headers,
									  final InputStream postPayload,
									  final MimeType mimeType) {
		HttpResponse outResponse = null;
		try {
			MimeType theMimeType = mimeType != null ? mimeType
													: MimeType.APPLICATION_XML;
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.POST()
										.withPayload(HttpRequestPayload.wrap(postPayload)
																	   .mimeType(theMimeType))
									.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a POST call to an URL, create or update web resource (not idempotent action).
	 * @param url the url to POST at
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param postPayload the POSTed data
	 * @return the server-returned result
	 */
	public static HttpResponse doPOST(final WebUrl url,final Map<String,String> urlParameters,
									  final Map<String,String> headers,
									  final InputStream postPayload) {
		HttpResponse outResponse = HttpRESTClient.doPOST(url,urlParameters,
														 headers,
														 postPayload,MimeType.APPLICATION_XML);
		return outResponse;
	}
	/**
	 * Makes a POST call to an URL sending a FORM in the payload, create or update web resource (not idempotent action).
	 * @param url the url to POST at
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param formParams the POSTed form data
	 * @return the server-returned result
	 */
	public static HttpResponse doPOSTForm(final WebUrl url,final Map<String,String> urlParameters,
										  final Map<String,String> headers,
										  final Map<String,String> formParams) {
		HttpResponse outResponse = null;
		try {
			List<HttpRequestFormParameter> postFormParams = null;
			if (CollectionUtils.hasData(formParams)) {
				postFormParams = Lists.newArrayListWithExpectedSize(formParams.size());
				for (Map.Entry<String,String> me : formParams.entrySet()) {
					postFormParams.add(HttpRequestFormParameterForText.of(me.getValue())
															  .withName(me.getKey()));
				}
			}
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.POSTForm().withPOSTFormParameters(postFormParams)
									.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
// PRIVATE METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	private static List<HttpRequestURLEncodedParameter> _composeUrlParameters(final Map<String,String> urlParameters) {
		List<HttpRequestURLEncodedParameter> outParams = null;
		if (CollectionUtils.hasData(urlParameters)) {
			outParams = Lists.newArrayListWithExpectedSize(urlParameters.size());
			for (Map.Entry<String,String> me :  urlParameters.entrySet()) {
				outParams.add(HttpRequestURLEncodedParameter.of(me.getValue())
															.withName(me.getKey()));
			}
		}
		return outParams;
	}
}

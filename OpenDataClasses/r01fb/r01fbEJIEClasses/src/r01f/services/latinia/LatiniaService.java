package r01f.services.latinia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.HandlerRegistry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;

import r01f.exceptions.Throwables;
import r01f.marshalling.Marshaller;
import r01f.object.latinia.LatiniaRequest;
import r01f.object.latinia.LatiniaRequestMessage;
import r01f.object.latinia.LatiniaResponse;
import r01f.services.ServiceCanBeDisabled;
import r01f.types.Factory;
import r01f.types.weburl.SerializedURL;
import r01f.util.types.Strings;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.ejie.w91d.client.W91DSendSms;
import com.ejie.w91d.client.W91DSendSmsWebServiceImplService_Impl;
import com.google.inject.name.Named;

/**
 * Encapsulates latinia message sending
 * Sample usage in a guice injected app:
 * <pre class='brush:java'>
 *	    public static void main(String[] args) {
 *	    	Injector injector = Guice.createInjector(new LatiniaServiceGuiceModule(AppCode.forId("x47b")));
 *      
 *	    	LatiniaService latiniaService = injector.getInstance(LatiniaService.class);
 *	    	latiniaService.sendNotification(_createMockMessage());
 *	    }
 * </pre>
 * 
 * Sample usage in a not injected app:
 * <pre class='brush:java'>
 *		LatiniaServiceProvider latiniaServiceProvider = new LatiniaServiceProvider(props);
 *		LatiniaService latiniaService = latiniaServiceProvider.get();
 *	    latiniaService.sendNotification(_createMockMessage());
 * </pre>
 * 
 * To build a message: 
 * <pre class='brush:java'>
 *	    private static LatiniaRequestMessage _createMockMessage() {
 *	    	LatiniaRequestMessage latiniaMsg = new LatiniaRequestMessage();
 *	    	latiniaMsg.setAcknowledge("S");
 *	    	latiniaMsg.setMessageContent("TEST MESSAGE!!!");
 *	    	latiniaMsg.setReceiverNumbers("688671967");
 *	    	return latiniaMsg;
 *	    }
 * </pre>
 * 
 * For all this to work a properties file with the following config MUST be provided:
 * <pre class='xml'>
 * 	<latinia>
 *		<wsURL>http://svc.intra.integracion.jakina.ejiedes.net/ctxapp/W91dSendSms?WSDL</wsURL>
 *		<authentication>
 *		  <enterprise>
 *		    		<login>INNOVUS</login>
 *		    		<user>innovus.superusuario</user>
 *		    		<password>MARKSTAT</password>
 *		  </enterprise>
 *		  <clientApp>
 *		    		<productId>X47B</productId>
 *		    		<contractId>2066</contractId>
 *		    		<password>X47N</password>
 *		  </clientApp>
 *		</authentication>
 *	</latinia>
 * </pre>
 */
@Singleton
@Slf4j
public class LatiniaService
  implements ServiceCanBeDisabled {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private boolean _disabled;
	private final Marshaller _marshaller;
	private final LatiniaServiceAPIData _apiData;
	
	private final Factory<W91DSendSms> _wsClientFactory = new Factory<W91DSendSms>() {
															@Override @SuppressWarnings("deprecation")
															public W91DSendSms create() {
																log.debug("[Latinia] > creating the latinia ws client to URL {}",_apiData.getWebServiceUrl());
																W91DSendSms sendSmsService = null;
																try {
																	// [1] - Create the auth token
																	Map<String,String> authTokenMap = new HashMap<String,String>();
																	authTokenMap.put("sessionToken",XMLUtils.asStringLinearized(_apiData.getAuthToken())); //Linarize xml, strip whitespaces and newlines from latinia auth token

																	// [2] - Create the client
																	W91DSendSmsWebServiceImplService_Impl ws = new W91DSendSmsWebServiceImplService_Impl(_apiData.getWebServiceUrl().asString());
																	sendSmsService = ws.getW91dSendSms();
																	HandlerRegistry registry = ws.getHandlerRegistry();
																	Object port = ws.getPorts().next();

																	List<HandlerInfo> handlerList = new ArrayList<HandlerInfo>();
																	handlerList.add(new HandlerInfo(LatiniaTokenHandler.class,
																									authTokenMap,
																									null));	// ?
																	registry.setHandlerChain((QName)port,
																							 handlerList);

																} catch (Throwable th) {
																	log.error("[Latinia] > Error while creating the {} service: {}",W91DSendSms.class,th.getMessage(),th);
																}
																return sendSmsService;
															}
													};
/////////////////////////////////////////////////////////////////////////////////////////
//  UTIL
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	public static class LatiniaServiceAPIData {
		@Getter private final SerializedURL _webServiceUrl;
		@Getter private final Document _authToken;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor to be used with {@link LatiniaServiceGuiceModule}
	 * @param props
	 * @param marshaller
	 */
	@Inject
	public LatiniaService(@XMLPropertiesComponent("notifier") final XMLPropertiesForAppComponent props,
						  @Named("latiniaObjsMarshaller")     final Marshaller marshaller) {
		_marshaller = marshaller;
		_apiData = LatiniaServiceProvider.apiDataFrom(props,
													  "notifier");
	}
	/**
	 * Constructor to be used with {@link LatiniaServiceProvider}
	 * @param apiData
	 * @param marshaller
	 */
	public LatiniaService(final LatiniaServiceAPIData apiData,
						  final Marshaller marshaller) {
		_marshaller = marshaller;
		_apiData = apiData;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ServiceCanBeDisabled
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isEnabled() {
		return !_disabled;
	}
	@Override
	public boolean isDisabled() {
		return _disabled;
	}
	@Override
	public void setEnabled() {
		_disabled = false;
	}
	@Override
	public void setDisabled() {
		_disabled = true;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	API
/////////////////////////////////////////////////////////////////////////////////////////
	public LatiniaResponse sendNotification(final LatiniaRequestMessage msg) {
		log.debug("[Latinia] > Send message");

		// [1] - Create a ws client using the factory
		W91DSendSms sendSmsService = _wsClientFactory.create();
		if (sendSmsService == null) throw new IllegalStateException(Throwables.message("Could NOT create a {} instance!",W91DSendSms.class));

		// [2] - Compose the request
		LatiniaRequest request = new LatiniaRequest();
		request.addMessage(msg);

		// [3] - Send the request
		LatiniaResponse response = null;
		try {
			StringBuilder requestXml = new StringBuilder("<![CDATA[");
			requestXml.append(_marshaller.xmlFromBean(request));
			requestXml.append("]]>");
			log.info("[Latinia] > request XML: {} ",requestXml);

			final String responseXml = sendSmsService.sendSms(requestXml.toString());
			if (!Strings.isNullOrEmpty(responseXml)) {
				String theResponseXml = Strings.of(responseXml).replaceAll("PETICION","RESPUESTA")
											   .asString();
				log.info("[Latinia] > response XML: {}",responseXml);
				response = _marshaller.beanFromXml(theResponseXml);
			} else {
				throw new IllegalStateException("Latinia WS returned a null response!");
			}

		} catch (Throwable th) {
			log.error("[Latinia] > Error while calling ws at {}: {}",_apiData.getWebServiceUrl(),th.getMessage(),th);
		}
		return response;
	}
}

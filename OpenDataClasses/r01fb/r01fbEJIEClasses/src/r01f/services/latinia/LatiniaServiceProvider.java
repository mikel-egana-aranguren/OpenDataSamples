package r01f.services.latinia;

import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.marshalling.Marshaller;
import r01f.marshalling.Marshaller.MarshallerMappingsSearch;
import r01f.marshalling.simple.SimpleMarshallerBuilder;
import r01f.object.latinia.LatiniaObject;
import r01f.services.latinia.LatiniaService.LatiniaServiceAPIData;
import r01f.types.weburl.SerializedURL;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.inject.Provider;

/**
 * Provides a {@link LatiniaService} using a properties file info
 * The properties file MUST contain a config section like:
 * <pre class='java'>
 *		LatiniaServiceProvider latiniaServiceProvider = new LatiniaServiceProvider(props);
 *		LatiniaService latiniaService = latiniaServiceProvider.get();
 * </pre>
 * For this provider to work, a properties file with the following config MUST be provided:
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
@Slf4j
@RequiredArgsConstructor
public class LatiniaServiceProvider 
  implements Provider<LatiniaService> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unused")
	private final AppCode _appCode;
	private final XMLPropertiesForAppComponent _props;
	private final String _propsRootNode;
	private final Marshaller _latiniaObjsMarshaller;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public LatiniaServiceProvider(final AppCode appCode,
								  final XMLPropertiesForAppComponent props)  {
		this(appCode,props,
			 "notifier");
	}
	public LatiniaServiceProvider(final AppCode appCode,final XMLPropertiesForAppComponent props,
								  final String propsRootNode) {
		_appCode = appCode;
		_props = props;
		_propsRootNode = propsRootNode;
		_latiniaObjsMarshaller = LatiniaServiceProvider.latiniaObjectsMarshaller(); 
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PROVIDER
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public LatiniaService get() {	
		LatiniaServiceAPIData apiData = LatiniaServiceProvider.apiDataFrom(_props,
																		   _propsRootNode);
		return new LatiniaService(apiData,
								  _latiniaObjsMarshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	static Marshaller latiniaObjectsMarshaller() {
		return SimpleMarshallerBuilder.createForPackages(MarshallerMappingsSearch.inPackages(LatiniaObject.class.getPackage().getName()))	// persistence objects
									  .getForMultipleUse();
	}
	static LatiniaServiceAPIData apiDataFrom(final XMLPropertiesForAppComponent props,
											 final String propsRootNode) {
		SerializedURL latiniaWSUrl = props.propertyAt(propsRootNode + "/latinia/wsURL")
										  .asURL(SerializedURL.of("http://svc.extra.integracion.jakina.ejiedes.net/ctxapp/W91dSendSms?WSDL"));
		Document latiniaAuthToken = _createLatiniaAuthToken(props,
															propsRootNode);
		return new LatiniaServiceAPIData(latiniaWSUrl, 
										 latiniaAuthToken);
	}
	/**
	 * Creates an XML access token to latinia services from applications that do not have N38 session token.
	 * @param loginEnterprise
	 * @param userLatinia
	 * @param passwordLatinia
	 * @param refProduct
	 * @param idContract
	 * @param password
	 * @return Document formatted with latinia user info.
	 * @throws ParserConfigurationException
	 */
	@SuppressWarnings("cast")
	private static Document _createLatiniaAuthToken(final XMLPropertiesForAppComponent props,
												    final String propsRootNode) {
		Document outAuthToken = null;
		try {
			log.debug("[Latinia] > Creating authentication token .........");

			// [1] - Load the config from the properties file
			//            <authentication>
			//				  <enterprise>
			//                		<login>INNOVUS</login>
			//                		<user>innovus.superusuario</user>
			//                		<password>MARKSTAT</password>
			//				  </enterprise>
			//				  <clientApp>
			//                		<productId>X47B</productId>
			//                		<contractId>2066</contractId>
			//                		<password>X47N</password>
			//				  </clientApp>
			//            </authentication>
			AppCode enterpriseLogin = props.propertyAt(propsRootNode + "/latinia/authentication/enterprise/login").asAppCode("INNOVUS");
			UserCode publicUser = props.propertyAt(propsRootNode + "/latinia/authentication/enterprise/user").asUserCode("innovus.superusuario");
			Password publicPassword = props.propertyAt(propsRootNode + "/latinia/authentication/enterprise/password").asPassword("MARKSTAT");
			AppCode productId = props.propertyAt(propsRootNode + "/latinia/authentication/clientApp/productId").asAppCode("X47B");
			UserCode contractId = props.propertyAt(propsRootNode + "/latinia/authentication/clientApp/contractId").asUserCode("2066");
			Password password = props.propertyAt(propsRootNode + "/latinia/authentication/clientApp/password").asPassword("X47B");

			log.info("[Latinia] > Token props: enterprise[login={}, user={}, passwd={}] / clientApp[product={}, contract={}, passwd={}]",
					 enterpriseLogin,publicUser,publicPassword,
					 productId,contractId,password);


			// [2] - Create the Authentication token
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringElementContentWhitespace(true);

			DocumentBuilder builder = dbf.newDocumentBuilder();
			outAuthToken = builder.newDocument();

			Element authNode = outAuthToken.createElement("authenticationLatinia");

			Element enterpriseUserNode = outAuthToken.createElement("userLatinia");
			enterpriseUserNode.appendChild(outAuthToken.createTextNode(publicUser.asString()));
			authNode.appendChild(enterpriseUserNode);

			Element enterprisePasswordNode = outAuthToken.createElement("passwordLatinia");
			enterprisePasswordNode.appendChild(outAuthToken.createTextNode(publicPassword.asString()));
			authNode.appendChild(enterprisePasswordNode);

			Element clientProductNode = outAuthToken.createElement("refProduct");
			clientProductNode.appendChild(outAuthToken.createTextNode(productId.asString()));
			authNode.appendChild(clientProductNode);

			Element contractIdNode = outAuthToken.createElement("idContract");
			contractIdNode.appendChild(outAuthToken.createTextNode(contractId.asString()));
			authNode.appendChild(contractIdNode);

			Element loginEnterpriseNode = outAuthToken.createElement("loginEnterprise");
			loginEnterpriseNode.appendChild(outAuthToken.createTextNode(enterpriseLogin.asString()));
			authNode.appendChild(loginEnterpriseNode);

			Element contractPasswordNode = outAuthToken.createElement("password");
			contractPasswordNode.appendChild(outAuthToken.createTextNode(password.asString()));
			authNode.appendChild(contractPasswordNode);

			outAuthToken.appendChild((Node)authNode);

			log.debug("[Latinia] > Auth Token={}",XMLUtils.asStringLinearized(outAuthToken, Charset.forName("UTF-8")));

		} catch (Throwable th) {
			log.error("[Latinia] > Error while creating the latinia auth token: {}",th.getMessage(),th);
		}
		return outAuthToken;
	}
}

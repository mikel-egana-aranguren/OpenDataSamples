/*
 * @author Alex Lara Garachana 
 * Created on 16-may-2004
 */
package r01f.ejie.xlnets.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.ejie.xlnets.servlet.XLNetsAppCfg.ProviderDef;
import r01f.ejie.xlnets.servlet.XLNetsTargetCfg.ResourceAccess;
import r01f.exceptions.Throwables;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.reflection.ReflectionException;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLProperties;

/**
 * Filtro de entrada para control de la seguridad basada en XLNets de forma declarativa.
 * La configuraci�n de seguridad se declara en el fichero [codApp].properties.xml incluyendo una
 * seccion como la del siguiente ejemplo:
 * <pre class='brush:xml'>
 *     <authCfg useSession='true/false' override='true/false'>
 *          <provider>
 *              <className>com.ejie.r01f.xlnets.R01FXLNetsSecurityProvider</className>
 *          </provider>
 *     		<target id='theId' kind='restrict|allow'>
 *     			<uri>[Expresion regular para machear la uri que se solicita]</uri>
 *     			<items>
 *     				<item type='[itemType]' mandatory='true/false' oid='[itemOID]'>
 *     					<es>[Nombre en castellano]</es>
 *     					<eu>[Nombre en euskera]</eu>
 *     				</item>
 *     				<item type='[itemType]' mandatory='true/false' oid='[itemOID]'>
 *     					<es>[Nombre en castellano]</es>
 *     					<eu>[Nombre en euskera]</eu>
 *     				</item>
 *     				....
 *     			</items>
 *     		</target>
 *     		....
 *     </authCfg>
 * </pre>
 * Notas:
 * <pre>
 * 		useSession:			Indica si la informaci�n de autorizacion se almacena en memoria o bien hay que
 * 							volver a obtenerla cada vez que se accede al recurso
 * 		override:			Indica si se ha de ignorar la configuracion de seguridad (no hay seguridad)
 *      provider:           Configuraci�n del provider de seguridad
 * 		   className: 	    La clase que se encarga de consultar el almac�n de seguridad
 * 							      Puede utilizarse XLNets o un povider que obtiene la seguridad de BD
 * 		target:			    Un recurso que se protege
 *          id:             Identificador del recurso
 *          kind:           Tipo de protecci�n
 *                              allow   -> Permitir el acceso
 *                              restrict-> restringir el acceso
 * 			uri:			Una expresi�n regular con la url del recurso.
 * 			resources:		Elementos sobre los que hay que comprobar si el usuario tiene acceso
 *                          NOTA: Si el tipo es allow, NO se comprueban los recursos
 *              resource    Elemento individual sobre el que hay que comprobar si el usuario tiene acceso
 * 				  oid:		El oid del objeto de seguridad
 * 							En el caso de XLNets el oid puede corresponder al uid de una funci�n o un
 * 							tipo de objeto
 *                mandatory	true/false: Indica si este item es OBLIGATORIO, lo cual implica que en caso de
 * 							no tener acceso, se prohibir� el acceso al recurso.
 * 							Si en todos los items mandatory es false, se permitir� el acceso aunque
 * 							no haya autorizaci�n a los items, sin embargo, la informaci�n de seguridad se
 * 							dejar� en sesion.
 * 				  type:		El tipo de elemento a comprobar
 * 							En el caso de XLNets el tipo puede ser
 * 								function: Una funcion
 * 								object: Un tipo de objeto
 * 				  es/eu		La descripcion en euskera y castellano del item de seguridad
 *
 * </pre>
 * Se pueden definir m�ltiples recursos en una aplicacion, cada uno de ellos tendr� asociada una expresi�n regular
 * con la URI. Cuando llega una petici�n, se aplicar� la configuraci�n de seguridad del primer recurso cuya uri
 * machee la url solicitada al filtro.
 *
 * La secuencia de autorizacion es la siguiente:
 * <pre>
 * 1.- INSTANCIAR EL PROVIDER DE AUTORIZACION ESPECIFICADO EN LA CONFIGURACION
 * 2.- COMPROBAR SI EL USUARIO EST� AUTENTICADO
 * 	   Se llama al m�todo getContext() para ver si el usuario est� autenticado y si es as� obtener el contexto de
 *     autenticaci�n de usuario
 *     Aqu� a su vez se pueden dar dos casos:
 * 	   2.1 - EL USUARIO NO ESTA AUTENTICADO
 * 	           Si el usuario NO est� autenticado se le dirige a la p�gina de login llamando
 *             al metodo redirectToLogin() del provider de autorizacion
 *     2.2 - EL USUARIO ESTA AUTENTICADO PERO NO HAY INFORMACION DE CONTEXTO DE AUTORIZACION EN LA SESION
 *             El usuario ha hecho login, pero es la primera vez que entra al recurso y no hay informaci�n de
 *             contexto de autorizacion en la sesion. El provider en la llamada a al funcion getContext() devuelve
 *             todos los datos del usuario y de la sesi�n de seguridad en la que esta autenticado
 *             Ahora ya se est� como en el caso 2.3 (siguiente caso)
 * 	   2.3 - EL USUARIO EST� AUTENTICADO Y HAY INFORMACION DE CONTEXTO DE AUTORIZACION EN LA SESION
 *             Si en la sesi�n hay informaci�n de contexto, se busca en el contexto la informacion de autorizacion
 *             del destino solicitado. Pueden darse dos casos:
 *             2.3.1 - En el contexto NO hay informacion de autorizacion del destino (es la primera vez que se accede)
 *                        Se llama a la funcion authorize() del provider de seguridad que se encarga obtener
 *                        la autorizacion correspondiente.
 *                        A partir de este momento, esta informaci�n de autorizaci�n se mantiene en sesi�n
 *                        y no es necesario volver a pedirla al provider de seguridad
 *             2.3.2 - En el contexto HAY informacion de autorizacion del destino (ya se ha accedido al recurso)
 *                        Directamente se devuelve la informacion de autorizacion almacenada en sesion
 * </pre>
 */
@Accessors(prefix="_")
@NoArgsConstructor
@Slf4j
public class XLNetsAuthServletFilter 
  implements Filter {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTES
/////////////////////////////////////////////////////////////////////////////////////////	
	private static final transient String APPCODE = "appCode";			// Codigo de la aplicacion
	public static final String AUTHCTX_SESSIONATTR = "XLNetsAuthCtx";	// Contexto de usuario en la sesion
	public static final String AUTHCTX_REQUESTATTR = "XLNetsAuthCtx";	// Contexto de usuario en la request
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Configuraci�n de seguridad de la aplicacion 
	 */
	private static ConcurrentMap<String,XLNetsAppCfg> _appCfgs = new ConcurrentHashMap<String,XLNetsAppCfg>();	// Configuraci�n de seguridad de las aplicaciones
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * Configuraci�n del filtro (web.xml)
	 */
	@Getter @Setter private FilterConfig _config = null;
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
///////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void destroy() {
		_config = null;
	}	
	@Override
	public void init(final FilterConfig config) throws ServletException {
		this.setConfig(config);
	}
	@Override
	public void doFilter(final ServletRequest request,final ServletResponse response,
						 final FilterChain chain) throws IOException, 
						 								 ServletException {
		log.info("\n\n\n\n>>>>>>>>>> Inicio: Filtro de Autorizacion >>>>>>>>>>>>>>>>>>>>>>");

		//__________________________ CARGA DE LA CONFIGURACION ______________________________
		// Inicializar si no se ha cargado la configuraci�n de autorizaci�n de la
		// aplicaci�n.
		String appCode = _config.getInitParameter(APPCODE);
		XLNetsAppCfg appCfg = _retrieveAppCfg(appCode);
		if (appCfg == null || appCfg.getProvider("userProvider") == null || Strings.isNullOrEmpty(appCfg.getProvider("userProvider").getTypeName())) {
	        throw new ServletException(Throwables.message("No se ha podido cargar la configuracion de autorizacion para la aplicacion '{}' o bien esta NO es correcta. Revisa la seccion <authorization> del fichero '{}.xlnets.properties.xml'",
	        											  appCode,appCode));
		}

	    // __________________________ CONTROL DE AUTORIZACION ________________________________
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;

        // Obtener la URI solicitada y ver si la url a la que se quiere acceder verifica
        // alguno de los patrones establecidos en la configuracion de autorizacion
        String uri = _fullURI(request);
        log.trace("[XLNetsAuth]-URI: {}",uri);
        
        XLNetsTargetCfg targetCfg = _resourceThatFirstMatches(appCfg,uri);
        if (targetCfg == null) throw new ServletException(Throwables.message("El filtro de seguridad XLNetsAuthServletFilter NO ha verificado ning�n patron para la uri '{}'.\nRevisa la seccion <authCfg> del fichero {}.xlnets.properties.xml",
        												   					 uri,appCode));

	    XLNetsAuthCtx authCtx = null;
	    log.info("[XLNetsAuth]-{}",(appCfg.isOverride() ? "NO se comprueba la autorizacion!!!!!\n\tParametro override=true":"Comprobando autorizacion...") );
	    if (!appCfg.isOverride() 
	     && targetCfg.getKind() == ResourceAccess.RESTRICT) {

	    	log.info("[XLNetsAuth]-La url {} tiene el acceso protegido: authCfg.override={} targetCfg.kind={} ...comprobaci�n de la autenticaci�n:",
            		 uri,appCfg.isOverride(),targetCfg.getKind());
			
	    	// *********************** MONDONGO ************************
			// Obtener el contexto de la sesi�n (si hay sesi�n)
	    	authCtx = _retrieveSessionStoredAuthCtx(appCfg,req);		// Contexto de autorizacion

		    // No hay informaci�n de contexto de autorizacion. Pueden pasar dos cosas
		    //		1.- El usuario se ha autenticado y es la primera vez que entra al target
		    //		2.- El usuario NO se ha autenticado 
			if (authCtx == null) {
			    log.info("\n[XLNetsAuth]-No hay contexto de autorizacion:\n\t1.- Es la primera vez\n\t2.- No hay autenticacion");
			    
				// Instanciar el provider de autenticaci�n especificado en la configuracion y autenticar al usuario/a
				XLNetsAuthProvider authProvider = _instanceAuthProvider("userProvider",
																			appCfg,
																			req);
			    authCtx = authProvider.getAuthContext();
			    if (authCtx == null) {
			        // No hay sesi�n de usuario: Redirigir a la p�gina de login indicando la url a la que se quiere acceder ahora
			        log.info("[XLNetsAuth]-El usuario NO se ha autenticado, redirigir a la pagina de login");
			        _redirToLoginPage(authProvider,
			        				  req,res);
                    return;		// FIN!!
                }
			}

			// Aqu� ya hay un contexto de usuario, bien porque ya estaba en la sesi�n, bien porque se ha creado nuevo
			log.info("[XLNetsAuth]-El usuario ya est� autenticado. Comprobar la autorizaci�n de acceso para la uri: {}",uri);
			XLNetsTargetCtx targetCtx = authCtx.getTargetAuth(targetCfg.getUriPattern());	// La informaci�n de autorizaci�n para el target puede estar en YA en el contexto de autorizaci�n			
			if (targetCtx == null) targetCtx = _authorize(appCfg,targetCfg, 
														  authCtx,
														  req,res);							// ... pero si no est�, cargarla
			// Aqui ya hay informacion de autorizacion y todo el mondongo
            if (targetCtx == null || CollectionUtils.isNullOrEmpty(targetCtx.getAuthorizedResources()) ) {
		       // No se ha podido obtener la informacion de autorizacion o bien no se tiene acceso
		        log.warn("[XLNetsAuth]-NO se ha podido cargar la info de autorizacion del recurso:\n1.- No hay acceso\n2.- El provider de autorizacion no ha funcionado");
		        res.sendError(HttpServletResponse.SC_FORBIDDEN,
		        			  "El filtro de seguridad R01F NO ha permitido el acceso al recurso!");
		        return;		// A la porra!!!
            }
		} else {
		    // Dado que no se comprueba la autorizacion, hay que crear unos contextos 'virtuales'
		    // El recurso al que se llama deber�a comprobar el parametro appCfg.override para comprobar si se est� chequeando la autorizacion
            log.warn("[XLNetsAuth]-NO se ha comprobado la seguridad: appCfg.override={} targetCfg.kind={}\r\n" +
                                "\tEl atributo override=true o bien para el target que machea {} se ha configurado kind={}",
                     appCfg.isOverride(),targetCfg.getKind(),uri,ResourceAccess.RESTRICT);
		    authCtx = new XLNetsAuthCtx(null);
		}

	    // Permitir el acceso...
    	log.info("[XLNetsAuth]-Autorizado!!!!");
	    
	    // Antes de pasar el testigo al recurso que toca si se utiliza session, poner el contexto de
	    // autorizacion en session.
	    // En cualquier caso, tanto si se utiliza session el contexto se pasa al recurso como un atributo
	    // de la request
    	req.setAttribute(AUTHCTX_REQUESTATTR,authCtx);
    	if (appCfg.isUseSession()) {
	        HttpSession ses = req.getSession(true);		// Crear la session por huevos.
	        ses.setAttribute(AUTHCTX_SESSIONATTR,
	        				 authCtx);
    	} 

	    //__________________________ PASAR EL TESTIGO ________________________________
	    // Siguiente eslabon de la cadena.....
	    chain.doFilter(request,response);

		log.info("[XLNetsAuth]->>>>>>>>>> Fin: Filtro de Autorizacion >>>>>>>>>>>>>>>>>>>>>>\n\n\n\n");
	}



///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS PRIVADOS
///////////////////////////////////////////////////////////////////////////////////////////
	private static XLNetsAppCfg _retrieveAppCfg(final String appCode) {
		XLNetsAppCfg appCfg =_appCfgs.get(appCode);	// Configuracion de seguridad de la aplicacion
		if (appCfg == null) {
		    // Cargar la configuraci�n de autorizaci�n del fichero properties de la aplicaci�n 
			// (el codigo de aplicaci�n es un parametro de inicio del servlet que se indica en el web.xml)
			final XLNetsAppCfg newCfg = new XLNetsAppCfg(AppCode.forId(appCode));
			appCfg = _appCfgs.putIfAbsent(appCode,
            					 		   newCfg);
			if (appCfg == null) appCfg = newCfg;
		}
		return appCfg;
	}
    private static XLNetsAuthProvider _instanceAuthProvider(final String providerId,
    															final XLNetsAppCfg appCfg,
                                                                final ServletRequest req) {
        XLNetsAuthProvider outProvider = null; 
        ProviderDef cfgProvider = !Strings.isNullOrEmpty(providerId) ? appCfg.getProvider(providerId)
        															 : null;
        if (cfgProvider != null && cfgProvider.getTypeName() != null) {                     
            // Instanciar la clase que implementa la seguridad
            log.trace("[XLNetsAuth]-Instanciando el provider de autorizacion: {}",cfgProvider.getTypeName());
            outProvider = ReflectionUtils.createInstanceOf(cfgProvider.getTypeName());
            if (outProvider != null) {
	            outProvider.setProviderId(providerId);
	            outProvider.setProviderDef(appCfg.getProvider(providerId));
	            outProvider.setRequest(req);
            }
        }
        if (outProvider == null) throw ReflectionException.instantiationException(cfgProvider != null ? cfgProvider.getTypeName() 
        																				  			  : "el provider no est� configurado");        
        
        return outProvider;
    } 
	private static XLNetsAuthCtx _retrieveSessionStoredAuthCtx(final XLNetsAppCfg authCfg,
										   		 			   final HttpServletRequest req) {
		XLNetsAuthCtx authCtx = null;	
		if (authCfg.isUseSession()) {
			HttpSession ses = req.getSession(false);
			if (ses != null) {
                authCtx = (XLNetsAuthCtx)ses.getAttribute(AUTHCTX_SESSIONATTR);
                log.info("[XLNetsAuth]-\t\t--->Contexto de seguridad obtenido de la sesi�n web!");
            } else {
                log.info("[XLNetsAuth]-\t\t--->NO existe el contexto de seguridad en la sesi�n web!");
            }
		} else {
		    log.info("[XLNetsAuth]-La informacion de autorizacion NO se guarda en session http");
		}
		return authCtx;		// devuelve null si NO se utiliza sesi�n
	}
	private static XLNetsTargetCtx _authorize(final XLNetsAppCfg authCfg,final XLNetsTargetCfg targetCfg,
									   		  final XLNetsAuthCtx authCtx,
									   		  final HttpServletRequest req,final HttpServletResponse res) {

		log.info("[XLNetsAuth]-\t\t...autorizando el acceso a la uri {} en base al provider {}",
				 req.getRequestURI(),authCfg.getProvider("userProvider").getTypeName());
		
		// Instanciar el provider de autenticaci�n especificado en la configuracion y obtener el contexto para el destino
		XLNetsAuthProvider authProvider = _instanceAuthProvider("userProvider",
																	authCfg,
																	req);
	    XLNetsTargetCtx targetCtx = authProvider.authorize(authCtx,
	    								   				   targetCfg,
	    								   				   authCfg.isOverride());
	    
        // A�adir el contexto de autorizacion del recurso en el contexto de autorizaci�n de la aplicacion,
	    // as� est� disponible para futuras llamadas.
	    if (targetCtx != null) {
	        log.info("[XLNetsAuth]-Introducir la informacion de autorizacion para el patr�n de url {} en el contexto de autorizacion global en sesion",
	        		 targetCtx.getTargetCfg().getUriPattern());
	        if (authCtx.getAuthorizedTargets() == null)  authCtx.setAuthorizedTargets(new HashMap<String,XLNetsTargetCtx>());
	        authCtx.getAuthorizedTargets().put(targetCtx.getTargetCfg().getUriPattern(),
	        								   targetCtx);
	    }
        
        // Devolver el contexto del destino
        return targetCtx;
	}
	private void _redirToLoginPage(final XLNetsAuthProvider authProvider,
								   final HttpServletRequest req,final HttpServletResponse res) {
        // primer intento: configuraci�n de seguridad de la aplicaci�n
        String loginPage = authProvider.getProviderDef()
        							   .getLoginPage();
        // segundo intento: parametro del filtro 
        if (Strings.isNullOrEmpty(loginPage)) {
        	loginPage = _config.getInitParameter("xlnetsLoginURL");
        }
        // tercer intento: configuraci�n de seguridad de r01f
        if (Strings.isNullOrEmpty(loginPage)) {
        	loginPage = XMLProperties.createForAppComponent(AppCode.forId("r01f"),AppComponent.forId("default"))
        							 .notUsingCache()
        							 .propertyAt("authCfg/provider/loginPage").asString();
        }
        // cuarto intento: configuraci�n de seguridad de xlnets en r01f
        if (Strings.isNullOrEmpty(loginPage)) {
        	loginPage = XMLProperties.createForAppComponent(AppCode.forId("r01f"),AppComponent.forId("default"))
        							 .notUsingCache()
						 			 .propertyAt("xlnetsLoginURL").asString();
        }
        // ERROR
        if ( loginPage == null ) {
            log.warn("[XLNetsAuth]-NO se ha podido encontrar la url de login. El orden de b�squeda ha sido:\n\t-Propiedad authCfg/provider/loginPage de la aplicacion\n\t-Propiedad xlnetsLoginURL de configuracion del filtro en el fichero web.xml\n\t-Propiedad authCfg/provider/loginPage de R01F\n\t-Propiedad xlnetsLoginURL de R01F");
        } else {
            // �APA: XLNets necesita el parametro N38API con la url a la que se quiere ir...
            loginPage += "?N38API=" + req.getRequestURL();
            log.info("[XLNetsAuth]-redirecting to login page: " + loginPage);
            authProvider.redirectToLogin(res,loginPage);
        }
	}
    /**
     * Devuelve una cadena con la uri solicitada a partir de la request
     * @param request: La request
     * @return: Una cadena con la uri
     */
    private static String _fullURI(final ServletRequest request) {
	    // Obtener la uri de la request
	    HttpServletRequest req = (HttpServletRequest)request;
		return req.getServerName() + ( req.getServerPort() == 80 ? "" : ":" + req.getServerPort() ) + req.getRequestURI();		        
    }
    private static XLNetsTargetCfg _resourceThatFirstMatches(final XLNetsAppCfg appCfg,
    														 final String uri) {
    	String theUri = uri;
        if (theUri == null) {
            theUri = "dummy";
            log.trace("[XLNetsAuth]-La uri suministrada es nula... se toma una uri dummy, as� que se machear� unicamente el target con uriPattern * (si lo hay)");
        }
        log.trace("[XLNetsAuth]-Intentando casar la URI: {} con los patrones de uri especificados en el fichero de properties",theUri);
        
        XLNetsTargetCfg outTargetCfg = null;
        if (appCfg.getTargets() != null) {
            Map.Entry<String,XLNetsTargetCfg> me = null;
            String pattern = null;
            for (Iterator<Map.Entry<String,XLNetsTargetCfg>> it=appCfg.getTargets().entrySet().iterator(); it.hasNext(); ) {
                me = it.next();
                pattern = me.getKey();
                if ( _matches(theUri,pattern) ) {
                    // Se ha encontrado una configuraci�n de seguridad para la uri a la que se quiere acceder
                    log.trace("[XLNetsAuth]-pattern: {} MATCHES!!!",pattern);
                    outTargetCfg = me.getValue();
                    break;
                }
            }
        }
        if (outTargetCfg == null) log.warn("[XLNetsAuth]-No se ha encontrado ningun patron para la uri {}",theUri);        
        return outTargetCfg;
    } 
	private static boolean _matches(final String uri,final String pattern) {
		// Utilizar expresiones regulares para machear la uri recibida
	    // con la especificada en el fichero de prop�edades
		Pattern p = Pattern.compile("^" + pattern + "$");
		Matcher m = p.matcher(uri);
		return m.find();
	}
}
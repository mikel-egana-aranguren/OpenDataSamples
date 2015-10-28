package r01f.xmlproperties;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import lombok.AllArgsConstructor;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import r01f.enums.Enums;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.marshalling.Marshaller;
import r01f.reflection.ReflectionUtils;
import r01f.resources.ResourcesLoaderDef;
import r01f.types.Path;
import r01f.types.contact.EMail;
import r01f.types.weburl.Host;
import r01f.types.weburl.SerializedURL;
import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended;
import r01f.xmlproperties.XMLPropertiesForApp.ComponentProperties;

import com.google.common.base.Function;

/**
 * Encapsula las propiedades de R01F.<br>
 *
 * <p>El uso t�pico es el siguiente:
 * <ul>
 * <ol>Crear una clase que encapsula el acceso a las propiedades de un c�digo de aplicaci�n
 * 	   y crear un inyector GUICE responsable de dar acceso al singleton, indicando en el constructor:
 * 			<ul>
 * 			<li>n�mero estimado de componentes (ficheros de propiedades).</li>
 * 			<li>n�mero estimado de propiedades por componente.</li>
 * 			</ul>
 * 		<pre class="brush:java">
 * 		public class XXProps {
 * 			private static Injector injector = Guice.createInjector(new XMLPropertiesGuiceModule(1,50));
 * 		}
 * 		</pre>
 * </ol>
 * <ol>Dar acceso a las propiedades creando un m�todo llamado por ejemplo "at"<br>
 * 			<p>OPCION 1: Hay un solo componente llamado por ejemplo "default"
 * 				<pre class="brush:java">
 * 				public static XMLPropertyWrapper at(final String xPath) {
 *					XMLPropertiesManager props = injector.getInstance(XMLPropertiesManager.class);
 *					XMLPropertyWrapper prop = new XMLPropertyWrapper(props.of("xx","default"),xPath);
 *					return prop;
 *				}
 *				</pre>
 *			<p>OPCION 2: Hay varios componentes
 * 				<pre class="brush:java">
 * 				public static XMLPropertyWrapper at(final String comp,final String xPath) {
 *					XMLPropertiesManager props = injector.getInstance(XMLPropertiesManager.class);
 *					XMLPropertyWrapper prop = new XMLPropertyWrapper(props.of("xx",comp),xPath);
 *					return prop;
 *				}
 *				</pre>
 *				o tambi�n es posible:
 * 				<pre class="brush:java">
 * 				public static XMLPropertyWrapper inCompYAt(final String xPath) {
 *					XMLPropertiesManager props = injector.getInstance(XMLPropertiesManager.class);
 *					XMLPropertyWrapper prop = new XMLPropertyWrapper(props.of("xx","compY"),xPath);
 *					return prop;
 *				}
 *				</pre>
 * </ol>
 * <ol>Acceder a las propiedades a trav�s de la clase XXProps:
 * 		<pre class="brush:java">
 * 		String myStringProp = XXProps.at(xPath).asString(defaultValue);
 * 		</pre>
 * </ol>
 * </ul>
 */
@AllArgsConstructor
public final class XMLPropertyWrapper {
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////	
	private ComponentProperties _props;
	private String _xPath;

/////////////////////////////////////////////////////////////////////////////////////////
//	ACCESO A LAS PROPIEDADES
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return el nodo xml que contiene la propiedad
	 */
	public Node node() {
		return _props.node(_xPath);
	}
	/**
 	 * @return the xml node list
	 */
	public NodeList nodeList() {
		return _props.nodeList(_xPath);
	}
	/**
	 * Transforms the child nodes into a collection of objects
	 * @param transformFunction
	 * @return
	 */
	public <T> Collection<T> asObjectList(final Function<Node,T> transformFunction) {
		return _props.getObjectList(_xPath,
									transformFunction);
	}
	/**
	 * Comprueba si la propiedad existe.
	 * @return <code>true</code> si existe la propiedad.
	 */
	public boolean exist() {
		return _props.existProperty(_xPath);
	}
	/**
	 * Obtiene el valor de la propiedad intentando inferir el tipo
	 * a devolver
	 * @return La propiedad en el tipo inferido
	 */
	public <T> T get() {
		return _props.<T>get(_xPath);
	}
	/**
	 * Obtiene el valor de la propiedad intentando inferir el tipo
	 * a devolver
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return La propiedad en el tipo inferido
	 */
	public <T> T get(final T defaultValue) {
		return _props.<T>get(_xPath,defaultValue);
	}
	/**
	 * Obtiene el valor de la propiedad como una cadena.
	 * @return La propiedad como un {@link String}.
	 */
	public String asString() {
		return this.asString(null);
	}
	/**
	 * Devuelve la propiedad como {@link String}.
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return the property value or the default value if the property is NOT found
	 */
	public String asString(final String defaultVal) {
		return _props.getString(_xPath,defaultVal);
	}
	/**
	 * Obtiene la propiedad como un wrapper.
	 * @return La propiedad como un {@link StringExtended}.
	 */
	public StringExtended asStringWrapped() {
		return this.asStringWrapped(null);
	}
	/**
	 * Devuelve la propiedad como un {@link StringExtended}.
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return the property value or the default value if the property is NOT found
	 */
	public StringExtended asStringWrapped(final String defaultVal) {
		return _props.getStringWrapped(_xPath,defaultVal);
	}
	/**
	 * El valor de la propiedad como un objeto num�rico.
	 * @return La propiedad como un {@link Number}.
	 */
	public Number asNumber() {
		return this.asNumber(null);
	}
	/**
	 * Devuelve la propiedad como un {@link Number}.
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return the property value or the default value if the property is NOT found
	 */
	public Number asNumber(final Number defaultVal) {
		return _props.getNumber(_xPath,defaultVal);
	}
	/**
	 * El valor de la propiedad como un objeto entero.
	 * @return La propiedad como un {@link Integer}.
	 */
	public int asInteger() {
		return _props.getInteger(_xPath);
	}
	/**
	 * Devuelve la propiedad como un {@link Integer}.
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return the property value or the default value if the property is NOT found
	 */
	public int asInteger(final int defaultVal) {
		return _props.getInteger(_xPath,defaultVal);
	}
	/**
	 * El valor de la propiedad como un objeto long.
	 * @return La propiedad como un {@link Long}.
	 */
	public long asLong() {
		return _props.getLong(_xPath);
	}
	/**
	 * Devuelve la propiedad como un {@link Long}.
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return the property value or the default value if the property is NOT found
	 */
	public long asLong(final long defaultVal) {
		return _props.getLong(_xPath,defaultVal);
	}
	/**
	 * El valor de la propiedad como un objeto double.
	 * @return La propiedad como un {@link Double}.
	 */
	public double asDouble() {
		return _props.getDouble(_xPath);
	}
	/**
	 * Devuelve la propiedad como un {@link Double}.
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return the property value or the default value if the property is NOT found
	 */
	public double asDouble(final double defaultVal) {
		return _props.getDouble(_xPath,defaultVal);
	}
	/**
	 * El valor de la propiedad como un objeto complejo.
	 * @return la propiedad como un {@link Float}.
	 */
	public float asFloat() {
		return _props.getFloat(_xPath);
	}
	/**
	 * Devuelve la propiedad como un {@link Float}.
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return the property value or the default value if the property is NOT found
	 */
	public float asFloat(final float defaultVal) {
		return _props.getFloat(_xPath,defaultVal);
	}
	/**
	 * El valor de la propiedad como un objeto booleano.
	 * @return La propiedad como un {@link Boolean}.
	 */
	public boolean asBoolean() {
		return _props.getBoolean(_xPath);
	}
	/**
	 * Devuelve la propiedad como un {@link Boolean}.
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return the property value or the default value if the property is NOT found
	 */
	public boolean asBoolean(boolean defaultVal) {
		return _props.getBoolean(_xPath,defaultVal);
	}
	/**
	 * El valor de la propiedad como un Path
	 * @return el path
	 */
	public Path asPath() {
		String path = _props.getString(_xPath);
		return path != null ? Path.of(path)
							: null;
	}
	/**
	 * El valor de la propiedad como un Path
	 * @param defaultVal el valor por defecto si la propiedad NO se encuentra definida
	 * @return the property value or the default value if the property is NOT found
	 */
	public Path asPath(final Path defaultVal) {
		Path outPath = this.asPath();
		return outPath != null ? outPath
							   : defaultVal;
	}
	/**
	 * El valor de la propiedad como un Path
	 * @param defaultVal el valor por defecto si la propiedad NO se encuentra definida
	 * @return the property value or the default value if the property is NOT found
	 */
	public Path asPath(final String defaultVal) {
		return this.asPath(Path.of(defaultVal));
	}
	/**
	 * The property value as an {@link AppCode} object
	 * @return
	 */
	public AppCode asAppCode() {
		String appCode = this.asString();
		return Strings.isNOTNullOrEmpty(appCode) ? AppCode.forId(appCode) : null;
	}
	/**
	 * The property value as an {@link AppCode} object or the default value if the
	 * property is not found
	 * @param defaultVal 
	 * @return 
	 */
	public AppCode asAppCode(final AppCode defaultVal) {
		String outAppCode = _props.getString(_xPath);
		return outAppCode != null ? AppCode.forId(outAppCode)
								  : defaultVal;
	}
	/**
	 * The property value as an {@link AppCode} object or the default value if the
	 * property is not found
	 * @param defaultVal 
	 * @return 
	 */
	public AppCode asAppCode(final String defaultVal) {
		return this.asAppCode(AppCode.forId(defaultVal));
	}
	/**
	 * El valor de la propiedad como un c�digo de usuario
	 * @return the property value or the default value if the property is NOT found
	 */
	public UserCode asUserCode() {
		String outUserCode = _props.getString(_xPath);
		return outUserCode != null ? UserCode.forId(outUserCode) : null;
	}
	/**
	 * El valor de la propiedad como un c�digo de usuario
	 * @param defaultVal valor por defecto si la propiedad NO se encuentra definida
	 * @return the property value or the default value if the property is NOT found
	 */
	public UserCode asUserCode(final UserCode defaultVal) {
		String outUserCode = _props.getString(_xPath);
		return outUserCode != null ? UserCode.forId(outUserCode)
								   : defaultVal;
	}
	/**
	 * El valor de la propiedad como un c�digo de usuario
	 * @param defaultVal valor por defecto si la propiedad NO se encuentra definida
	 * @return the property value or the default value if the property is NOT found
	 */
	public UserCode asUserCode(final String defaultVal) {
		return this.asUserCode(UserCode.forId(defaultVal));
	}
	/**
	 * El valor de la propiedad como un password el valor por defecto si la propiedad NO se encuentra definida
	 * @param defaultVal
	 * @return the property value or the default value if the property is NOT found
	 */
	public Password asPassword() {
		String outPwd = _props.getString(_xPath);
		return outPwd != null ? Password.forId(outPwd) : null;
	}
	/**
	 * El valor de la propiedad como un password el valor por defecto si la propiedad NO se encuentra definida
	 * @param defaultVal
	 * @return the property value or the default value if the property is NOT found
	 */
	public Password asPassword(final Password defaultVal) {
		String outPwd = _props.getString(_xPath);
		return outPwd != null ? Password.forId(outPwd)
							  : defaultVal;
	}
	/**
	 * El valor de la propiedad como un password el valor por defecto si la propiedad NO se encuentra definida
	 * @param defaultVal
	 * @return the property value or the default value if the property is NOT found
	 */
	public Password asPassword(final String defaultVal) {
		return this.asPassword(Password.forId(defaultVal));
	}
	/**
	 * The property value as an {@link EMail}
	 * @return
	 */
	public EMail asEMail() {
		String email = this.asString();
		return Strings.isNOTNullOrEmpty(email) ? EMail.create(email) : null;
	}
	/**
	 * The property value as an {@link EMail}
	 * @param defaultEMail
	 * @return
	 */
	public EMail asEMail(final String defaultEMail) {
		return EMail.create(this.asString(defaultEMail));
	}
	/**
	 * The property value as an {@link EMail}
	 * @param defaultEMail
	 * @return the property value or the default value if the property is NOT found
	 */
	public EMail asEMail(final EMail defaultEMail) {
		String email = this.asString();
		return Strings.isNOTNullOrEmpty(email) ? EMail.create(email)
											   : defaultEMail;
	}
	/**
	 * Gets the property as a {@link Host}
	 * @return
	 */
	public Host asHost() {
		String host = this.asString();
		return Host.of(host);
	}
	/**
	 * Gets the property as a {@link Host}
	 * @param defaultVal the url to be returned if no property is found
	 * @return the property value or the default value if the property is NOT found
	 */
	public Host asHost(final Host defaultVal) {
		String host = this.asString();
		Host outHost = null;
		if (Strings.isNullOrEmpty(host)) {
			outHost = defaultVal;
		} else {
			outHost = Host.of(host);
		}
		return outHost;
	}
	/**
	 * Gets the property as a {@link SerializedURL}
	 * @return
	 */
	public SerializedURL asURL() {
		String url = this.asString();
		return SerializedURL.of(url);
	}
	/**
	 * Gets the property as a {@link SerializedURL}
	 * @param defaultVal the url to be returned if no property is found
	 * @return the property value or the default value if the property is NOT found
	 */
	public SerializedURL asURL(final SerializedURL defaultVal) {
		String url = this.asString();
		SerializedURL outUrl = null;
		if (Strings.isNullOrEmpty(url)) {
			outUrl = defaultVal;
		} else {
			outUrl = SerializedURL.of(url);
		}
		return outUrl;
	}
	/**
	 * La propiedad como un objeto de tipo propiedad.
	 * @return la propiedad como un {@link Properties}.
	 */
	public Properties asProperties() {
		return _props.getProperties(_xPath);
	}
	/**
	 * Devuelve la propiedad como un {@link Properties}.
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return El valor de la propiedad o el valor por defecto si la propiedad NO se encuentra definida.
	 */
	public Properties asProperties(final Properties defaultVal) {
		return _props.getProperties(_xPath,defaultVal);
	}
	/**
	 * La propiedad como una lista de cadenas.
	 * @return La propiedad como un {@link List} de {@link String}.
	 */
	public List<String> asListOfStrings() {
		return _props.getListOfStrings(_xPath);
	}
	/**
	 * Devuelve la propiedad como un {@link List} de {@link String}.
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return El valor de la propiedad o el valor por defecto si la propiedad NO se encuentra definida.
	 */
	public List<String> asListOfStrings(final List<String> defaultVal) {
		return _props.getListOfStrings(_xPath,defaultVal);
	}
	/**
	 * Devuelve la propiedad como un {@link List} de {@link String}.
	 * @param defaultVal El valor por defecto si la propiedad NO se encuentra definida.
	 * @return El valor de la propiedad o el valor por defecto si la propiedad NO se encuentra definida.
	 */
	public List<String> asListOfStrings(final String... defaultStrings) {
		List<String> defaultVal = defaultStrings != null ? Arrays.asList(defaultStrings) : null;
		return _props.getListOfStrings(_xPath,defaultVal);
	}
	/**
	 * Devuelve la propiedad como un {@link Charset}
	 * @param defaultVal el valor por defecto si la propiedad NO se encuentra definida
	 * @return El valor de la propiedad o el valor por defecto si la propiedad NO se encuentra definida
	 */
	public Charset asCharset(final Charset defaultVal) {
		String outCharset = _props.getString(_xPath);
		return outCharset != null ? Charset.forName(outCharset)
								  : defaultVal;
	}
	/**
	 * Devuelve la propiedad como la definici�n de un tipo
	 * @return la definici�n del tipo
	 */
	public Class<?> asType() {
		Class<?> outType = null;
		String typeName = this.asString();
		if (!Strings.isNullOrEmpty(typeName)) {
			outType = ReflectionUtils.typeFromClassName(typeName);
		}
		return outType;
	}
	/**
	 * Devuelve la propiedad como la definici�n de un tipo
	 * @param defaultType la definici�n del tipo por defecto si no existe la propiedad
	 * @return
	 */
	public Class<?> asType(final Class<?> defaultType) {
		Class<?> outType = this.asType();
		return outType != null ? outType : defaultType;
	}
	/**
	 * Devuelve la propiedad como un objeto obtenido transformando de XML a objetos con el marshaller de R01.
	 * @param objType Tipo del objeto devuelto.
	 * @param marshaller El marshaller con la definici�n de la transformaci�n de XML a objetos.
	 * @return La propiedad como objeto.
	 */
	public <T> T asObject(final Class<T> objType,
						  final Marshaller marshaller) {
		return _props.getObject(_xPath,objType,marshaller);
	}
	/**
	 * Returns the property as an object transforming the node to an object
	 * using a {@link Function} 
	 * @param transformFuncion
	 * @return
	 */
	public <T> T asObject(final Function<Node,T> transformFuncion) {
		return _props.getObject(_xPath,transformFuncion);
	}
	/**
	 * Devuelve la propiedad como un objeto de definici�n de carga de recursos {@link ResourcesLoaderDef}
	 * (obviamente el XML tiene que tener la estrucutra impuesta por {@link ResourcesLoaderDef}).
	 * @return un objeto {@link ResourcesLoaderDef}
	 * @see ResourcesLoaderDef
	 */
	public ResourcesLoaderDef asResourcesLoaderDef() {
		return _props.getResourcesLoaderDef(_xPath);
	}
	/**
	 * Devuelve la propiedad como un elemento de un enum
	 * @param enumType el tipo del enum
	 * @return la propiedad como un elemento del enum 
	 */
	public <E extends Enum<E>> E asEnumElement(final Class<E> enumType) {
		return this.asEnumElement(enumType,null);
	}
	/**
	 * Devuelve la propiedad como un elemento de un enum
	 * @param enumType el tipo del enum
	 * @param devaultVelue el elemento del enum a devolver si no se encuentra la propiedad
	 * @return la propiedad como un elemento del enum 
	 */
	public <E extends Enum<E>> E asEnumElement(final Class<E> enumType,
											   final E defaultValue) {
		String enumAsStr = this.asString();
		E outE = defaultValue;
		if (!Strings.isNullOrEmpty(enumAsStr)) {
			try {
				outE = Enum.valueOf(enumType,enumAsStr);
			} catch(IllegalArgumentException illArgEx) {
				outE = defaultValue;	// No hay un valor para la propiedad
			}
		}
		return outE;
	}
	/**
	 * Devuelve la propiedad como un elemento de un enum
	 * @param enumType el tipo del enum
	 * @return la propiedad como un elemento del enum 
	 */
	public <E extends Enum<E>> E asEnumElementIgnoringCase(final Class<E> enumType) {
		return this.asEnumElementIgnoringCase(enumType,
											  null);
	}
	/**
	 * Devuelve la propiedad como un elemento de un enum
	 * @param enumType el tipo del enum
	 * @param defaultValue
	 * @return la propiedad como un elemento del enum 
	 */
	public <E extends Enum<E>> E asEnumElementIgnoringCase(final Class<E> enumType,
														   final E defaultValue) {
		String enumAsStr = this.asString();
		E outE = defaultValue;
		if (!Strings.isNullOrEmpty(enumAsStr)) {
			try {
				outE = Enums.of(enumType)
						   	.fromNameIgnoringCase(enumAsStr);
			} catch(IllegalArgumentException illArgEx) {
				outE = defaultValue;	// No hay un valor para la propiedad
			}
		}
		return outE;
	}
}

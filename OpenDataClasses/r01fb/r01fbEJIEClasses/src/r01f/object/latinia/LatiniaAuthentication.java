package r01f.object.latinia;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Los par�metros de conexi�n necesarios para autenticaci�n en LATINIA son:
 *
 * P�blicos (propios de la plataforma):
 *    - Empresa (Login enterprise): INNOVUS
 *    - Usuario Latinia: innovus.superusuario
 *    - Password Latinia: MARKSTAT
 *
 * Privados (propios de cada aplicaci�n)
 *    - Aplicaci�n (refProduct): <c�digo aplicaci�n>
 *    - Contrato (idcontract): <c�digo de contrato asociado a la aplicaci�n>
 *    - Contrase�a: <por defecto c�digo de aplicaci�n en desarrollo>
 *
 * Con esto puede formarse un xml v�lido para env�o SMS a trav�s de los WebServices de W91D.
 *
 * Ejemplo para una aplicaci�n Z99 con contrato 2000
 *
 * <authenticationLatinia>
 *     <loginEnterprise>INNOVUS</loginEnterprise>
 *     <userLatinia>innovus.superusuario</userLatinia>
 *     <passwordLatinia>MARKSTAT</passwordLatinia>
 *     <refProduct>Z99</refProduct>
 *     <idContract>2000</idContract>
 *     <password>Z99</password>
 * </authenticationLatinia>
 *
 * (La opci�n m�s recomendable para la autenticaci�n es el uso de XLNETS. De
 * esta forma la aplicaci�n se abstrae de los c�digos y contrase�as de
 * autenticaci�n en LATINIA que ser�n obtenidos por el conector W91D a partir
 * del token de sesi�n enviado en la petici�n al WebService)
 */
@XmlRootElement(name="authenticationLatinia")
@Accessors(prefix="_")
public class LatiniaAuthentication
	implements LatiniaObject {

	private static final long serialVersionUID = -9024193208434640204L;

/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlElement(name="loginEnterprise")
	@Getter @Setter private String _loginEnterprise; // INNOVUS

	@XmlElement(name="userLatinia")
	@Getter @Setter private String _userLatinia;     // innovus.superusuario

	@XmlElement(name="passwordLatinia")
	@Getter @Setter private String _passwordLatinia; // MARKSTAT

	@XmlElement(name="refProduct")
	@Getter @Setter private String _refProduct;      // X47B

	@XmlElement(name="idContract")
	@Getter @Setter private String _idContract;      // DESA:2066,PROD:2054 (el id que nos asignen...)

	@XmlElement(name="password")
	@Getter @Setter private String _password;        // X47N
}

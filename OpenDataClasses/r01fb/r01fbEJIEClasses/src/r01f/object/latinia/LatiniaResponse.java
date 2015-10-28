package r01f.object.latinia;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Resultado de una petici�n LatiniaRequest, en la petici�n se han podido enviar m�ltiples mensajes, cada uno a X tel�fonos.
 *
 * (un sms se ha podido enviar a n n�meros de tel�fono)
 *
 * Ejemplo (de documentaci�n Latinia):
 * <PETICION>
 * 	<MENSAJE NUM="1">
 * 		<TELEFONO NUM="659000001">
 * 			<RESULTADO>OK</RESULTADO>
 * 			<IDENTIFICADOR>UGsiZ7E1naZX/Uey32A1hFUq</IDENTIFICADOR>
 * 		</TELEFONO>
 * 		<TELEFONO NUM="666000001">
 * 			<RESULTADO>OK</RESULTADO>
 * 			<IDENTIFICADOR>UGsiZ7E2efSshUey32A1mU7o</IDENTIFICADOR>
 * 		</TELEFONO>
 * 		<TELEFONO NUM="600123456">
 * 			<RESULTADO>ERROR</RESULTADO>
 * 			<CODIGO_ERROR>301</CODIGO_ERROR>
 * 			<MENSAJE_ERROR>El mensaje ha expirado</MENSAJE_ERROR>
 * 		</TELEFONO>
 * 	</MENSAJE>
 *  <MENSAJE NUM="1">
 *  ...........
 *  </MENSAJE>
 * </PETICION>
 *
 * MENSAJE: Si un mensaje es enviado a varios n�meros de tel�fono nos devuelve una lista con el resultado de cada uno de ellos.
 * NUM: n�mero del mensaje que se ha enviado.
 * - TELEFONO: Mensaje que se ha enviado.
 * - NUM: N�mero de tel�fono al que se ha enviado el mensaje.
 * - RESULTADO: OK o ERROR. Si el mensaje se ha entregado bien se devuelve un OK. Si ha sucedido alg�n error se devuelve ERROR.
 * - IDENTIFICADOR: Si el resultado ha sido OK, se devuelve el identificador �nico del mensaje, id interno del mensaje en la plataforma
 *                  (para la Q68) y en identificador externo, no �nico, en el caso de las aplicaciones no Q68. En este �ltimo caso
 *                  Identificador + Num de tel�fono ser� la manera de identificar cada sms enviado.
 * - CODIGO_ERROR: C�digo asignado al error que se ha producido.
 * - MENSAJE_ERROR: Descripci�n del error que se ha generado.
 */
@XmlRootElement(name="RESPUESTA")
@Accessors(prefix="_")
@NoArgsConstructor
public class LatiniaResponse
	implements LatiniaObject {

	private static final long serialVersionUID = -3636899320335998954L;

/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlValue
	@Getter @Setter private List<LatiniaResponseMessage> _latiniaResponses;

}

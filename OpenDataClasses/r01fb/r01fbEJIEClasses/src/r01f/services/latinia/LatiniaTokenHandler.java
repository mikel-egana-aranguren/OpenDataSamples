package r01f.services.latinia;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LatiniaTokenHandler
  implements Handler {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
    private HandlerInfo _handlerInfo;
/////////////////////////////////////////////////////////////////////////////////////////
//  INIT / DESTROY
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
	public void init(HandlerInfo info) {
        _handlerInfo = info;
    }
	@Override
	public void destroy() {
		// nothing
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
	public QName[] getHeaders() {
        return _handlerInfo.getHeaders();
    }
    /**
     * Latinia session Token:
     * 		<authenticationLatinia>
     * 			<loginEnterprise>INNOVUS</loginEnterprise>
     * 		    <userLatinia>innovus.superusuario</userLatinia>
     * 		    <passwordLatinia>MARKSTAT</passwordLatinia>
     * 		    <refProduct>X47B</refProduct>
     * 		    <idContract>xxxx</idContract>
     * 		    <password>X47B</password>
     * 		</authenticationLatinia>
     *
     * SOAP Message example:
     * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:w91d="http://w91d">
	 * 	<soapenv:Header>
	 *		<authenticationLatinia>
     *           <userLatinia>innovus.superusuario</userLatinia>
     *           <passwordLatinia>MARKSTAT</passwordLatinia>
     *           <refProduct>X47B</refProduct>
     *           <loginEnterprise>INNOVUS</loginEnterprise>
     *           <idContract>2066</idContract>
     *           <password>X47B</password>
	 *		</authenticationLatinia>
	 * 	</soapenv:Header>
	 * 	<soapenv:Body>
	 * 		<StringInput xmlns="http://w91d">
	 *		<![CDATA[<?xml version="1.0" encoding="UTF-8" standalone="no"?>
	 *		<PETICION>
	 *		<LATINIA>
	 *		<MENSAJES>
	 *		<MENSAJE_INFO ACUSE="S">
	 *		<TEXTO>Hola con prioridad BAJA pero BAJA</TEXTO>
	 *		<GSM_DEST>616178858</GSM_DEST>
	 *		</MENSAJE_INFO>
	 *		</MENSAJES>
	 *		</LATINIA>
	 *		</PETICION>]]>
	 *		</StringInput>
	 *	</soapenv:Body>
	 * </soapenv:Envelope>
	 *
     */
    @Override
	public boolean handleRequest(final MessageContext m)  {
         try {
            SOAPMessageContext ctx = (SOAPMessageContext)m;
            SOAPMessage message = ctx.getMessage();

            String sSessionToken = (String)_handlerInfo.getHandlerConfig().get("sessionToken");

            // recuperar cabecera SOAP
            final SOAPHeader soapHeader = message.getSOAPPart().getEnvelope().getHeader();

            // Remove XML header from latinia authorization XML
            if (sSessionToken.startsWith("<?xml version=\"1.0\"")) {
                int pos = sSessionToken.indexOf('>') + 1;
                sSessionToken = sSessionToken.substring(pos).trim();
            }

            // incluir token en la cabecera creada
            soapHeader.addTextNode(sSessionToken);

            ByteArrayOutputStream myByteArrayOutputString = new ByteArrayOutputStream();
            message.writeTo(myByteArrayOutputString);

            String sSoapMessage = new String(myByteArrayOutputString.toByteArray(), "UTF-8");
            //sSoapMessage = _processHeaderScape(sSoapMessage);
            // Remove XML header from SOAP message
            if (sSoapMessage.startsWith("<?xml version=\"1.0\"")) {
                int pos = sSoapMessage.indexOf('>') + 1;
                sSoapMessage = sSoapMessage.substring(pos).trim();
            }
            sSoapMessage = sSoapMessage.replaceAll("&lt;", "<").replaceAll("&gt;", ">");

            log.debug("X47B >>> handleRequest(m) - sSoapMessage:\n" + sSoapMessage);

            ByteArrayInputStream in = new ByteArrayInputStream(sSoapMessage.getBytes());

            MessageFactory factory = MessageFactory.newInstance();
            message =  factory.createMessage(null, in);
            ctx.setMessage(message);
        } catch (Exception e) {
            log.error("X47B >>> JAX-RPC error:" +  e.getMessage());
            throw new JAXRPCException(e);
        }
        return true;
    }
    private static String _processHeaderScape(final String sSoapMessage) {
        int posEndIni = sSoapMessage.indexOf("Header>") + "Header>".length();
        int posEndEnd = sSoapMessage.indexOf("Header>", posEndIni) + "Header>".length() - 1;
        int posIniEnd = sSoapMessage.lastIndexOf('<', posEndEnd);

        StringBuilder sb = new StringBuilder();
        sb.append(sSoapMessage.substring(0, posEndIni)); //the header of soap message

        StringBuilder headerContentSb = new StringBuilder();
        String headerContent = posEndIni<=posIniEnd ? sSoapMessage.substring(posEndIni, posIniEnd) : "";
        headerContent = headerContent.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
        headerContentSb.append(headerContent);
        sb.append(headerContentSb).append(sSoapMessage.substring(posIniEnd));

        return sb.toString();
    }
    @Override
    public boolean handleResponse(final MessageContext context) {
        return true;
    }
    @SuppressWarnings({ "unused","static-method" })
	public boolean handleResponse(final MessageContext m,
    							  final String error) {
        return true;
    }
	@Override
	public boolean handleFault(final MessageContext context) {
		return false;
	}
}

package r01f.services.latinia;

import r01f.guids.CommonOIDs.AppCode;
import r01f.object.latinia.LatiniaRequestMessage;
import r01f.xmlproperties.XMLPropertiesGuiceModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
/**
 * ERPI Consoles can help you to check process success:
 * DESARROLLO: svc.integracion.jakina.ejiedes.net/w43saConsolaWAR/
 * PRUEBAS: svc.integracion.jakina.ejiepru.net/w43saConsolaWAR/
 * PRODUCCION: svc.integracion.jakina.ejgvdns/w43saConsolaWAR/
 */
public class TestLatinia {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new XMLPropertiesGuiceModule(),
												 new LatiniaServiceGuiceModule(AppCode.forId("x47b")));


		LatiniaService latiniaService = injector.getInstance(LatiniaService.class);
		latiniaService.sendNotification(_createMockMessage());
	}
	private static LatiniaRequestMessage _createMockMessage() {
		LatiniaRequestMessage latiniaMsg = new LatiniaRequestMessage();
		latiniaMsg.setAcknowledge("S");
		latiniaMsg.setMessageContent("TEST MESSAGE x47b intento 1");
		latiniaMsg.setReceiverNumbers("555555555");
		return latiniaMsg;
	}
}

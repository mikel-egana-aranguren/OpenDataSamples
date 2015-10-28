package r01f.aspects.core.logging;

import r01f.aspects.interfaces.logging.LoggedMethodCallsParamsFormatter;
import r01f.util.types.collections.CollectionUtils;


/**
 * Implementaci�n por defecto del formateo de los par�metros de llamada 
 * a un m�todo
 * Es utilizado en la anotaci�n @LoggedMethodCalls que se utiliza junto con el aspecto
 * LoggedMethodCallsAspect
 */
public class LoggedMethodCallsParamsDefaultFormatter 
  implements LoggedMethodCallsParamsFormatter {

	@Override
	public String formatParams(Object... params) {
		// Devuelve una cadena con informaci�n sobre los par�metros de la llamada a un m�todo
		String outLog = null;
		if (CollectionUtils.hasData(params)) {
			StringBuffer sb = new StringBuffer(params.length * 10);
			sb.append("> ").append(params.length).append(" params: ");
			int i=0;
			for(Object o : params) {
				if (o != null) {
					sb.append(o.getClass().getName());
				} else {
					sb.append("null");
				}
				if (i > 0 && i < params.length-1) sb.append(", ");
				i++;
			}
			outLog = sb.toString();
		}
		return outLog;
	}

}

package r01f.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import r01f.marshalling.MarshallerException;
import r01f.util.types.collections.CollectionUtils;


public class BeanReflection {
	private Class<?> _beanType;
	private Class<?>[] _constructorArgsTypes;	
	
///////////////////////////////////////////////////////////////////////////////
// CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor a partir de la definici�n de la clase
	 * @param beanClassDef la definici�n de la clase
	 */
	public BeanReflection(final Class<?> beanClassDef) {
		_beanType = beanClassDef;
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Devuelve el tipo del bean
	 */
	public Class<?> getType() {
		return _beanType;
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCCION DE OBJETOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Establece el constructor a utilizar
     * @param constructorArgsTypes parametros del constructor a utilizar
     */
    public BeanReflection withConstructor(final Class<?>... constructorArgsTypes) {
    	_constructorArgsTypes = constructorArgsTypes;
    	return this;
    }  
    /**
     * Obtiene una instancia de una clase a partir del nombre completo (incluido paquete)
     * de la clase y utilizando el constructor por defecto
     * Por defecto intenta hacer accesible el constructor...
     * @return un wrapper para el acceso a los m�todos de la instancia
     * @throws ReflectionException si no se puede obtener la instancia del objeto
     */
    public BeanInstanceReflection load(final Object... constructorArgs) {
    	Class<?>[] theConstructorArgsTypes = null;
    	Object[] theConstructorArgs = null;
    	if (_constructorArgsTypes == null || _constructorArgsTypes.length == 0) {
    		if (CollectionUtils.hasData(constructorArgs)) throw new MarshallerException("Se ha llamado a load con una serie de constructores, sin embargo, NO se han establecido los tipos de los par�metros del contructor llamando al m�todo 'withConstructor'");
    		theConstructorArgsTypes = new Class<?>[0];
    		theConstructorArgs = new Object[0];
    	} else {
    		theConstructorArgsTypes = _constructorArgsTypes;
    		theConstructorArgs = constructorArgs;
    	}
    	Object theBean = null;
    	try {
    		theBean = ReflectionUtils.createInstanceOf(_beanType,
    											 	   theConstructorArgsTypes,theConstructorArgs,
    											 	   true);
    	} catch (ReflectionException refEx) {    	
    		if (CollectionUtils.hasData(constructorArgs) && refEx.isNoMethodException()) {
	        	// Los argumentos del constructor se obtienen identificando los miembros finales de la clase, pero puede darse
    			// el siguiente caso:
    			//		@RequiredArgsConstructor
    			//		public abstract class MyTypeBase() {
    			//			private final String myField;
    			//		}
    			//		public class MyType extends MyTypeBase() {		<-- realmente ESTA clase NO tiene un constructor con los miembros finales
    			//			public MyType() {
    			//				super("a");	<-- el miembro final SIEMPRE se establece en el constructor
    			//			}
    			//		}
	        	// En este caso hay que utilizar el constructor por defecto
        		theBean = ReflectionUtils.createInstanceOf(_beanType,null,null,true);
    		} else {
    			throw refEx;
    		}
    	}
    	return new BeanInstanceReflection(_beanType,theBean);    	
    } 
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS ESTATICOS
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Obtiene un m�todo est�tico
     * @param methodName nombre del metodo
     * @param paramTypes tipo de datos de los parametros
     * @return un wrapper para la invocaci�n del metodo
     * @throws ReflectionException si no se encentra el m�todo
     */
    public MethodInvokeReflection staticMethod(final String methodName,Class<?>... paramTypes) {
    	Method method = ReflectionUtils.method(_beanType,methodName,paramTypes);
    	return new MethodInvokeReflection(_beanType,null,method);
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CAMPOS ESTATICOS
///////////////////////////////////////////////////////////////////////////////////////////     
    /**
     * Obtiene un miembro est�tico
     * @param fieldName nombre del miembro
     * @return el miembro 
     * @throws ReflectionException si no se encuentra el miembro
     */
    public FieldReflection staticField(final String fieldName) {
    	Field field = ReflectionUtils.field(_beanType,fieldName,true);
    	return new FieldReflection(_beanType,null,field);
    }
}

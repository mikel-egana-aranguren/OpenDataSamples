package r01f.services.latinia;

import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.marshalling.Marshaller;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Names;

/**
 * This guice module is to be used when using the X47BLatiniaService in a standalone way (ie testing)
 * something like:
 * <pre class='brush:java'>
 *		Injector injector = Guice.createInjector(new X47BLatiniaServiceGuiceModule());
 *	
 *		X47BLatiniaService latiniaService = injector.getInstance(X47BLatiniaService.class);
 *		latiniaService.sendNotification(_createMockMessage());
 * </pre>
 * It's important to bind the XMLPropertiesGuiceModule:
 * <pre class='brush:java'>
 * 		binder.install(new XMLPropertiesGuiceModule());
 * </pre>
 */
@RequiredArgsConstructor
public class LatiniaServiceGuiceModule 
  implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final AppCode _appCode;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(final Binder binder) {
		// Install the XMLProperties module
		//binder.install(new XMLPropertiesGuiceModule());
		
		// Bind a latinia objects marshaller instance 
		binder.bind(Marshaller.class)
			  .annotatedWith(Names.named("latiniaObjsMarshaller"))
			  .toInstance(LatiniaServiceProvider.latiniaObjectsMarshaller());
		
		// Bind the latinia service
		binder.bind(LatiniaService.class)
			  .in(Singleton.class);
	}
	/**
	 * Alternative to using a provider
     * binder.bind(XMLPropertiesForAppComponent.class)
     * 	  .annotatedWith(new XMLPropertiesComponent() {		// see [Binding annotations with attributes] at https://github.com/google/guice/wiki/BindingAnnotations
     * 								@Override
     * 								public Class<? extends Annotation> annotationType() {
     * 									return XMLPropertiesComponent.class;
     * 								}
     * 								@Override
     * 								public String value() {
     * 									return "latinia";
     * 								}
     * 	  				 })
     * 	  .toProvider(new Provider<XMLPropertiesForAppComponent>() {
     * 						@Override
     * 						public XMLPropertiesForAppComponent get() {
     * 							return X47BServicesBootstrapGuiceModule.this.servicesProperties();
     * 						}
     * 	  			  });
	 */
	@Provides @XMLPropertiesComponent("notifier")
	XMLPropertiesForAppComponent provideXMLPropertiesForLatinia(final XMLProperties props) {
		XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(_appCode),
																							 AppComponent.forId("notifier"));
		return outPropsForComponent;
	}		
}

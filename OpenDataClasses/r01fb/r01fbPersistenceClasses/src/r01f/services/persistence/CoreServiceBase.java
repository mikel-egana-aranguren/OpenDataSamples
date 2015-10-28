package r01f.services.persistence;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.events.HasEventBus;
import r01f.internal.ServicesBootstrapGuiceModuleBase;
import r01f.services.core.CoreService;
import r01f.services.interfaces.ServiceInterface;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.common.eventbus.EventBus;

/**
 * Core service base
 */
@Accessors(prefix="_")
public abstract class CoreServiceBase 
  		   implements CoreService,		// it's a core service
  		   			  HasEventBus {		// it contains an event bus
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The {@link XMLProperties} for the services layer
	 */
	@Inject @XMLPropertiesComponent("services") 
	@Getter protected XMLPropertiesForAppComponent _serviceProperties;
	/**
	 * EventBus 
	 * IMPORTANT! The event listeners are subscribed at {@link ServicesBootstrapGuiceModuleBase}
	 * 			  The subscription takes place when an event listener is configured at the guice moduel (see XXServicesBootstrapGuiceModule)
	 */
	@Inject
	@Getter protected EventBus _eventBus;
/////////////////////////////////////////////////////////////////////////////////////////
//	DELEGATE PROVIDER
// 	A provider is used since typically a new persistence delegate is created at every
//	service impl method call to create a fresh new EntityManager 
//	Note that a fresh new EntityManger is needed in every service impl method call
//	in order to avoid a single EntityManager that would cause transactional and
// 	concurrency issues
//	When at a delegate method services from another entity are needed (maybe to do some
//	validations), create a new delegate for the other entity reusing the current delegate
//	state (mainly the EntityManager), this way the transactional state is maintained:
//		public class CRUDServicesDelegateForX
//			 extends CRUDServicesForModelObjectDelegateBase<XOID,X> {
//			...	
//			public CRUDResult<M> someMethod(..) {
//				....
//				CRUDServicesDelegateForY yDelegate = new CRUDServicesDelegateForY(this);	// reuse the transactional state
//				yDelegate.doSomething();
//				...
//			
//		}
/////////////////////////////////////////////////////////////////////////////////////////
	protected abstract Provider<? extends ServiceInterface> getDelegateProvider();
	
	@SuppressWarnings({ "unchecked" })
	public <S extends ServiceInterface> S createDelegateAs(@SuppressWarnings("unused") final Class<S> servicesType) {
		return (S)this.getDelegateProvider().get();
	}
}

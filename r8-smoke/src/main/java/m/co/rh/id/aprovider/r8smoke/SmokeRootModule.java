package m.co.rh.id.aprovider.r8smoke;

import m.co.rh.id.aprovider.Provider;
import m.co.rh.id.aprovider.ProviderModule;
import m.co.rh.id.aprovider.ProviderRegistry;

/**
 * Root module of the main provider: registers every smoke service exactly like
 * a real consumer app would. The order matters for the lookup scenarios:
 * factory/pool are registered last because a failed lookup instantiates their
 * values while probing the isInstance fallback path.
 */
public class SmokeRootModule implements ProviderModule {

    @Override
    public void provides(ProviderRegistry providerRegistry, Provider provider) {
        providerRegistry.register(ISmokeSingletonService.class, SmokeSingletonServiceImpl::new);
        providerRegistry.register(SmokeInterfaceServiceImpl.class, SmokeInterfaceServiceImpl::new);
        providerRegistry.register(SmokeBaseService.class, SmokeChildImpl::new);
        providerRegistry.registerLazy(ISmokeLazyService.class, SmokeLazyServiceImpl::new);
        providerRegistry.registerAsync(ISmokeAsyncService.class, SmokeAsyncServiceImpl::new);
        providerRegistry.registerFactory(ISmokeFactoryProduct.class, SmokeFactoryProduct::new);
        providerRegistry.registerPool(ISmokePoolService.class, SmokePoolProduct::new);
    }
}

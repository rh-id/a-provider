package m.co.rh.id.aprovider.test;

import m.co.rh.id.aprovider.Provider;
import m.co.rh.id.aprovider.ProviderModule;
import m.co.rh.id.aprovider.ProviderRegistry;

public class ModuleA implements ProviderModule {
    public boolean isDisposed;

    @Override
    public void provides(ProviderRegistry providerRegistry, Provider provider) {
        providerRegistry.register(IServiceA.class, ServiceAImpl::new);
    }

    @Override
    public void dispose(Provider provider) {
        isDisposed = true;
    }
}

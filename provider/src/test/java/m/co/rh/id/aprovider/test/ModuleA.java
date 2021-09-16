package m.co.rh.id.aprovider.test;

import android.content.Context;

import m.co.rh.id.aprovider.Provider;
import m.co.rh.id.aprovider.ProviderModule;
import m.co.rh.id.aprovider.ProviderRegistry;

public class ModuleA implements ProviderModule {
    public boolean isDisposed;

    @Override
    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
        providerRegistry.register(IServiceA.class, new ServiceAImpl());
    }

    @Override
    public void dispose(Context context, Provider provider) {
        isDisposed = true;
    }
}

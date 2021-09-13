package m.co.rh.id.provider.test;

import android.content.Context;

import m.co.rh.id.provider.Provider;
import m.co.rh.id.provider.ProviderModule;
import m.co.rh.id.provider.ProviderRegistry;

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

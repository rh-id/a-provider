package m.co.rh.id.aprovider.r8smoke;

import m.co.rh.id.aprovider.Provider;
import m.co.rh.id.aprovider.ProviderModule;
import m.co.rh.id.aprovider.ProviderRegistry;

/**
 * Root module of the dispose-probe provider (scenario 13). Keeps the
 * {@link ProviderRegistry} it was given so the runner can attempt a register
 * AFTER {@code provider.dispose()} and assert the IllegalStateException.
 */
public class SmokeDisposingModule implements ProviderModule {

    public ProviderRegistry registeredRegistry;

    @Override
    public void provides(ProviderRegistry providerRegistry, Provider provider) {
        registeredRegistry = providerRegistry;
        providerRegistry.register(ISmokeSingletonService.class, SmokeSingletonServiceImpl::new);
        providerRegistry.registerPool(ISmokePoolService.class, SmokePoolProduct::new);
    }

    @Override
    public void dispose(Provider provider) {
        SmokeResult.moduleDisposeCallbackRecorded = true;
    }
}

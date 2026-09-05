package m.co.rh.id.aprovider.r8smoke;

import m.co.rh.id.aprovider.Provider;
import m.co.rh.id.aprovider.ProviderModule;
import m.co.rh.id.aprovider.ProviderRegistry;

/**
 * Registers nothing - the nested provider (scenario 12) is created with this
 * module so every successful lookup through it proves parent traversal.
 */
public class SmokeEmptyModule implements ProviderModule {

    @Override
    public void provides(ProviderRegistry providerRegistry, Provider provider) {
        // intentionally empty: the nested provider must fall through to its parent
    }
}

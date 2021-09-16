package m.co.rh.id.aprovider;

import android.content.Context;

/**
 * Interface to allow Android module to register component/service dependencies
 */
public interface ProviderModule {
    /**
     * Entry point to register components/services or ProviderModules
     *
     * @param context          current context that this module is registered to, could be Activity or Application or others
     * @param providerRegistry registry to register components/services or ProviderModule
     * @param provider         index to assist in retrieving registered components from other modules
     */
    void provides(Context context, ProviderRegistry providerRegistry, Provider provider);

    /**
     * Perform cleanup for this module, invoked when {@link Provider} invoke dispose
     *
     * @param context  current context that this module is registered to, could be Activity or Application or others
     * @param provider index to assist in retrieving registered components from other modules
     */
    void dispose(Context context, Provider provider);
}

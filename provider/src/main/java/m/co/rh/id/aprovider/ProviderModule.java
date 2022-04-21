package m.co.rh.id.aprovider;

/**
 * Interface to allow Android module to register component/service dependencies
 */
public interface ProviderModule {
    /**
     * Entry point to register components/services or ProviderModules
     *
     * @param providerRegistry registry to register components/services or ProviderModule
     * @param provider         provider to assist in retrieving registered components from other modules
     */
    void provides(ProviderRegistry providerRegistry, Provider provider);

    /**
     * Perform cleanup for this module, invoked when {@link Provider} invoke dispose
     *
     * @param provider provider to assist in retrieving registered components from other modules
     */
    void dispose(Provider provider);
}

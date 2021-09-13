package m.co.rh.id.provider;

import android.content.Context;

/**
 * Main provider interface to provide components/services/objects
 */
public interface Provider {
    static Provider createProvider(Context context, ProviderModule rootModule) {
        return new DefaultProvider(context, rootModule);
    }

    /**
     * Get registered object from provider,
     *
     * @param clazz class to be retrieved
     * @param <I>   Object type to be returned
     * @return object with type I
     * @throws NullPointerException if not found
     */
    <I> I get(Class<I> clazz);

    /**
     * Same as {@link #get(Class)} it doesn't throw NullPointerException or any exception,
     * just return null value if not found. It should swallow any exception and return null
     *
     * @param clazz class to be retrieved
     * @param <I>   Object type to be returned
     * @return object with type I
     */
    <I> I tryGet(Class<I> clazz);

    /**
     * Clear all registered object from provider, and perform disposal/clean up of for all {@link ProviderModule}
     * this provider will not be able to be used once this method is called, new one will need to be instantiated
     */
    void dispose();
}

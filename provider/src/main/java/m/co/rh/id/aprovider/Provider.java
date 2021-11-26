package m.co.rh.id.aprovider;

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
     * Same as {@link #get(Class)}, this method doesn't throw NullPointerException or any exception,
     * just return null value if not found. This method should swallow any exception and return null
     *
     * @param clazz class to be retrieved
     * @param <I>   object type to be returned
     * @return object with type I
     */
    <I> I tryGet(Class<I> clazz);

    /**
     * Defer {@link #get(Class)} execution into ProviderValue instance.
     * To get the actual value call ProviderValue.get()
     *
     * @param clazz class to be retrieved
     * @param <I>   object type to be returned
     * @return ProviderValue with type I
     * @throws NullPointerException if clazz with type I not found
     */
    <I> ProviderValue<I> lazyGet(Class<I> clazz);

    /**
     * Same as {@link #lazyGet(Class)} with difference not throwing NullPointerException
     * if the class type is not registered yet.
     * <p>
     * This method guarantees to return ProviderValue without exception,
     * but not guarantee that ProviderValue.get() is not null.
     * ProviderValue.get() may return null without throwing any exception
     *
     * @param clazz class to be retrieved
     * @param <I>   object type to be returned
     * @return ProviderValue with type I
     */
    <I> ProviderValue<I> tryLazyGet(Class<I> clazz);

    /**
     * Clear all registered object from provider, and perform disposal/clean up of for all {@link ProviderModule}
     * this provider will not be able to be used once this method is called, new one will need to be instantiated
     */
    void dispose();
}

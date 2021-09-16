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
     * Same as {@link #get(Class)}, this method doesn't throw NullPointerException or any exception,
     * just return null value if not found. This method should swallow any exception and return null
     *
     * @param clazz class to be retrieved
     * @param <I>   object type to be returned
     * @return object with type I
     */
    <I> I tryGet(Class<I> clazz);

    /**
     * Perform {@link #get(Class)} on background thread and do action as defined by {@link ProviderAction} param.<br/>
     * NOTE: make sure that the registered Class is safe to get or executed in background thread.<br/>
     * If {@link #get(Class)} somehow throw exception it will be forwarded to actionOnMainThread.onError
     *
     * @param clazz              class to be retrieved
     * @param actionOnMainThread action to do on main thread
     * @param <I>                object type to be get asynchronously and executed on main thread
     */
    <I> void getAsyncAndDo(Class<I> clazz, ProviderAction<I> actionOnMainThread);

    /**
     * Clear all registered object from provider, and perform disposal/clean up of for all {@link ProviderModule}
     * this provider will not be able to be used once this method is called, new one will need to be instantiated
     */
    void dispose();
}

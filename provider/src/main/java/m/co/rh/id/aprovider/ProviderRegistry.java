package m.co.rh.id.aprovider;

import android.content.Context;

/**
 * Provider registry to register modules/components/services
 */
public interface ProviderRegistry {

    /**
     * Set this providerRegistry to ignore duplicate type during registration.
     * Default value should be false.
     *
     * @param skip set to true to skip, false to not skip
     */
    void setSkipSameType(boolean skip);

    /**
     * Register provider module
     *
     * @param providerModule module to be registered
     */
    void registerModule(ProviderModule providerModule);

    /**
     * Register components/services, example:<br/>
     * providerRegistrar.register(MyService.class, new MyService());
     *
     * @param <I>            type of the object/service
     * @param clazz          the class of the service usually interface
     * @param implementation ProviderValue with the actual service implementation,
     *                       in which will be invoked when ProviderRegistry register this type
     */
    <I> void register(Class<I> clazz, ProviderValue<I> implementation);

    /**
     * Register components/services lazily
     * Implement {@link ProviderDisposable} if you wish for this object to be disposed
     * when provider.dispose is invoked
     *
     * @param <I>           type of the object/service
     * @param clazz         the class of the service, usually interface
     * @param providerValue getter value of the implementation
     */
    <I> void registerLazy(Class<I> clazz, ProviderValue<I> providerValue);

    /**
     * Register components/services asynchronously in background thread.
     * Implement {@link ProviderDisposable} if you wish for this object to be disposed
     * when provider.dispose is invoked
     *
     * @param <I>           type of the object/service
     * @param clazz         the class of the service, usually interface
     * @param providerValue getter value of the implementation
     */
    <I> void registerAsync(Class<I> clazz, ProviderValue<I> providerValue);

    /**
     * Register as factory for components/services object.
     * new instance will always be returned by using {@link ProviderValue} as factory/producer.
     * <p>
     * if an object is produced by this factory and is implementing {@link ProviderDisposable}
     * then {@link ProviderDisposable#dispose(Context)} will be invoked on previous object
     * instantiated by this factory before returning new instance.
     *
     * @param <I>           type of the object/service
     * @param clazz         the class of the service, usually interface
     * @param providerValue getter value that acts as producer of new instance
     */
    <I> void registerFactory(Class<I> clazz, ProviderValue<I> providerValue);

    /**
     * Register as pool for components/services object.
     * new instance will always be returned by using {@link ProviderValue} as factory/producer.
     * <p>
     * Almost the same as {@link #registerFactory(Class, ProviderValue)},
     * the difference is that {@link ProviderDisposable#dispose(Context)} will NOT be invoked on previous object instantiated by this.
     * {@link ProviderDisposable#dispose(Context)} will be invoked to ALL INSTANCE at once
     * only when Provider.dispose is invoked.
     *
     * @param <I>           type of the object/service
     * @param clazz         the class of the service, usually interface
     * @param providerValue getter value that acts as producer of new instance
     */
    <I> void registerPool(Class<I> clazz, ProviderValue<I> providerValue);
}

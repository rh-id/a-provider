package m.co.rh.id.provider;

/**
 * Provider registry to register modules/components/services
 */
public interface ProviderRegistry {

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
     * @param implementation the actual service implementation
     */
    <I> void register(Class<I> clazz, I implementation);

    /**
     * Register components/services lazily
     *
     * @param <I>           type of the object/service
     * @param clazz         the class of the service, usually interface
     * @param providerValue getter value of the implementation
     */
    <I> void registerLazy(Class<I> clazz, ProviderValue<I> providerValue);

    /**
     * Register components/services asynchronously in background thread
     *
     * @param <I>           type of the object/service
     * @param clazz         the class of the service, usually interface
     * @param providerValue getter value of the implementation
     */
    <I> void registerAsync(Class<I> clazz, ProviderValue<I> providerValue);

    /**
     * Register as factory for components/services object.
     * new instance will always be returned by using ProviderValue as factory/producer.
     *
     * @param <I>           type of the object/service
     * @param clazz         the class of the service, usually interface
     * @param providerValue getter value that acts as producer of new instance
     */
    <I> void registerFactory(Class<I> clazz, ProviderValue<I> providerValue);
}

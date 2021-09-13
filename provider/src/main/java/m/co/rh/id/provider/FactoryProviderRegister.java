package m.co.rh.id.provider;

/**
 * Helper class to register factory
 */
class FactoryProviderRegister<I> extends ProviderRegister<I> {
    public FactoryProviderRegister(Class<I> type, ProviderValue<I> providerValue) {
        super(type, providerValue);
    }

    @Override
    public I get() {
        return getProviderValue().get();
    }
}

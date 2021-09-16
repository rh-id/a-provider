package m.co.rh.id.aprovider;

/**
 * Helper class to register lazy-loaded singleton to the provider
 */
class LazySingletonProviderRegister<I> extends ProviderRegister<I> {
    private I mValue;

    public LazySingletonProviderRegister(Class<I> type, ProviderValue<I> providerValue) {
        super(type, providerValue);
    }

    @Override
    public synchronized I get() {
        if (mValue == null) {
            mValue = getProviderValue().get();
        }
        return mValue;
    }
}

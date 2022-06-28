package m.co.rh.id.aprovider;

/**
 * Helper class to register singleton to the provider
 */
class SingletonProviderRegister<I> extends ProviderRegister<I> {
    private I mValue;

    public SingletonProviderRegister(Class<I> type, ProviderValue<I> providerValue) {
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

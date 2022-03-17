package m.co.rh.id.aprovider;

class CachedProviderValue<I> implements ProviderValue<I> {
    private ProviderValue<I> mProviderValue;
    private I mInstance;

    public CachedProviderValue(ProviderValue<I> providerValue) {
        mProviderValue = providerValue;
    }

    @Override
    public synchronized I get() {
        if (mInstance == null) {
            mInstance = mProviderValue.get();
        }
        return mInstance;
    }
}

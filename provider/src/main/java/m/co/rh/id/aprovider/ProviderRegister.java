package m.co.rh.id.aprovider;

/**
 * Provider member
 */
abstract class ProviderRegister<I> implements ProviderValue<I> {
    private ProviderValue<I> mProviderValue;
    private Class<I> mType;

    public ProviderRegister(Class<I> type, ProviderValue<I> providerValue) {
        mType = type;
        mProviderValue = providerValue;
    }

    public ProviderValue<I> getProviderValue() {
        return mProviderValue;
    }

    public Class<I> getType() {
        return mType;
    }
}

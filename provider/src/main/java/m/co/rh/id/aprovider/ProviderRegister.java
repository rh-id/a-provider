package m.co.rh.id.aprovider;

import java.util.Objects;

/**
 * Provider member
 */
abstract class ProviderRegister<I> implements ProviderValue<I>, ProviderDisposable {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProviderRegister<?> that)) return false;
        return Objects.equals(mType.getName(), that.mType.getName());
    }

    @Override
    public int hashCode() {
        return mType.getName().hashCode();
    }
}

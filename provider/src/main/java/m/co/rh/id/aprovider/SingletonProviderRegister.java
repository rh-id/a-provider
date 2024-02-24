package m.co.rh.id.aprovider;

import android.content.Context;

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

    @Override
    public synchronized void dispose(Context context) {
        if (mValue instanceof ProviderDisposable) {
            ((ProviderDisposable) mValue).dispose(context);
            mValue = null;
        }
    }
}

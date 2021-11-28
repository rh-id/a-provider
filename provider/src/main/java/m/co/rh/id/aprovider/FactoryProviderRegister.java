package m.co.rh.id.aprovider;

import android.content.Context;

/**
 * Helper class to register factory
 */
class FactoryProviderRegister<I> extends ProviderRegister<I> implements ProviderDisposable {
    private Context mContext;
    private I mPreviousValue;

    public FactoryProviderRegister(Class<I> type, ProviderValue<I> providerValue, Context context) {
        super(type, providerValue);
        mContext = context;
    }

    @Override
    public synchronized I get() {
        if (mPreviousValue != null) {
            if (mPreviousValue instanceof ProviderDisposable) {
                ((ProviderDisposable) mPreviousValue).dispose(mContext);
            }
        }
        mPreviousValue = getProviderValue().get();
        return mPreviousValue;
    }

    @Override
    public synchronized void dispose(Context context) {
        if (mPreviousValue != null) {
            if (mPreviousValue instanceof ProviderDisposable) {
                ((ProviderDisposable) mPreviousValue).dispose(mContext);
            }
        }
        mPreviousValue = null;
        mContext = null;
    }
}

package m.co.rh.id.aprovider;

import android.content.Context;
import android.util.Log;

/**
 * Helper class to register lazy-loaded singleton to the provider
 */
class LazySingletonProviderRegister<I> extends ProviderRegister<I> implements ProviderDisposable {
    private static final String TAG = "SingletonProvider";

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

    @Override
    public synchronized void dispose(Context context) {
        if (mValue != null) {
            if (mValue instanceof ProviderDisposable) {
                try {
                    ((ProviderDisposable) mValue).dispose(context);
                } catch (Exception e) {
                    Log.e(TAG, getType().getName() + " failed to dispose: " + e.getMessage());
                }
            }
        }
        mValue = null;
    }
}

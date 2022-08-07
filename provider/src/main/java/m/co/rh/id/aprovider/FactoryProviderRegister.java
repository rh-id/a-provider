package m.co.rh.id.aprovider;

import android.content.Context;
import android.util.Log;

/**
 * Helper class to register factory
 */
class FactoryProviderRegister<I> extends ProviderRegister<I> implements ProviderDisposable {
    private static final String TAG = "FactoryProvider";

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
                try {
                    ((ProviderDisposable) mPreviousValue).dispose(mContext);
                } catch (Exception e) {
                    Log.e(TAG, getType().getName() + " failed to dispose: " + e.getMessage());
                }
            }
        }
        mPreviousValue = getProviderValue().get();
        return mPreviousValue;
    }

    @Override
    public synchronized void dispose(Context context) {
        if (mPreviousValue != null) {
            if (mPreviousValue instanceof ProviderDisposable) {
                try {
                    ((ProviderDisposable) mPreviousValue).dispose(mContext);
                } catch (Exception e) {
                    Log.e(TAG, getType().getName() + " failed to dispose: " + e.getMessage());
                }
            }
        }
        mPreviousValue = null;
        mContext = null;
    }
}

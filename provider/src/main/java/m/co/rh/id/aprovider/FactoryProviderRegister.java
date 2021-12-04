package m.co.rh.id.aprovider;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Helper class to register factory
 */
class FactoryProviderRegister<I> extends ProviderRegister<I> implements ProviderDisposable {
    private static final String TAG = "FactoryProvider";

    private Context mContext;
    private SyncWorkStealingWorker mSyncWorkStealingWorker;
    private I mPreviousValue;

    public FactoryProviderRegister(Class<I> type, ProviderValue<I> providerValue, Context context, ThreadPoolExecutor threadPoolExecutor) {
        super(type, providerValue);
        mContext = context;
        mSyncWorkStealingWorker = new SyncWorkStealingWorker(threadPoolExecutor);
    }

    @Override
    public I get() {
        return mSyncWorkStealingWorker.submit(() -> {
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
        });
    }

    @Override
    public void dispose(Context context) {
        mSyncWorkStealingWorker.execute(() -> {
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
            mSyncWorkStealingWorker = null;
        });
    }
}

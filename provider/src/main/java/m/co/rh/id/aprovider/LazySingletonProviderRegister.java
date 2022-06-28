package m.co.rh.id.aprovider;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Helper class to register lazy-loaded singleton to the provider
 */
class LazySingletonProviderRegister<I> extends ProviderRegister<I> implements ProviderDisposable {
    private static final String TAG = "LazySingletonProvider";

    private SyncWorkStealingWorker mSyncWorkStealingWorker;
    private I mValue;

    public LazySingletonProviderRegister(Class<I> type, ProviderValue<I> providerValue, ThreadPoolExecutor threadPoolExecutor) {
        super(type, providerValue);
        mSyncWorkStealingWorker = new SyncWorkStealingWorker(threadPoolExecutor);
    }

    @Override
    public I get() {
        return mSyncWorkStealingWorker.submit(() -> {
            if (mValue == null) {
                mValue = getProviderValue().get();
            }
            return mValue;
        });
    }

    @Override
    public void dispose(Context context) {
        mSyncWorkStealingWorker.execute(() -> {
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
        });
    }
}

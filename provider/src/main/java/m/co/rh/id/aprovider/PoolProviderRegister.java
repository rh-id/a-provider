package m.co.rh.id.aprovider;

import android.content.Context;
import android.util.Log;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Helper class to register pool
 */
class PoolProviderRegister<I> extends ProviderRegister<I> implements ProviderDisposable {
    private static final String TAG = "PoolProvider";

    private ThreadPoolExecutor mThreadPool;
    private CopyOnWriteArrayList<I> mPreviousValues;
    private SyncWorkStealingWorker mSyncWorkStealingWorker;

    public PoolProviderRegister(Class<I> type, ProviderValue<I> providerValue, ThreadPoolExecutor threadPoolExecutor) {
        super(type, providerValue);
        mThreadPool = threadPoolExecutor;
        mPreviousValues = new CopyOnWriteArrayList<>();
        mSyncWorkStealingWorker = new SyncWorkStealingWorker(threadPoolExecutor);
    }

    @Override
    public I get() {
        return mSyncWorkStealingWorker.submit(() -> {
            I previousVal = getProviderValue().get();
            mPreviousValues.add(previousVal);
            checkAndRemoveDisposedObjects();
            return previousVal;
        });
    }

    private void checkAndRemoveDisposedObjects() {
        mThreadPool.execute(() -> {
            if (!mPreviousValues.isEmpty()) {
                for (Iterator<I> iterator = mPreviousValues.iterator();
                     iterator.hasNext(); ) {
                    I prevVal = iterator.next();
                    if (prevVal instanceof ProviderIsDisposed) {
                        if (((ProviderIsDisposed) prevVal).isDisposed()) {
                            iterator.remove();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void dispose(Context context) {
        mSyncWorkStealingWorker.execute(() -> {
            while (!mPreviousValues.isEmpty()) {
                I prevValue = mPreviousValues.remove(0);
                if (prevValue instanceof ProviderDisposable) {
                    mThreadPool.execute(() -> {
                        try {
                            ((ProviderDisposable) prevValue).dispose(context);
                        } catch (Exception e) {
                            Log.e(TAG, getType().getName() + " failed to dispose: " + e.getMessage());
                        }
                    });
                }
            }
            mPreviousValues = null;
            mThreadPool = null;
            mSyncWorkStealingWorker = null;
        });
    }
}

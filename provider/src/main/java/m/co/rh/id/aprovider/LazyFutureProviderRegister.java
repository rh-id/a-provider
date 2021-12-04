package m.co.rh.id.aprovider;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Helper class to register lazy-loaded future singleton to the provider
 */
class LazyFutureProviderRegister<I> extends ProviderRegister<I> implements ProviderDisposable {
    private static final String TAG = "FutureProvider";
    private Future<I> mFutureValue;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private SyncWorkStealingWorker mSyncWorkStealingWorker;

    public LazyFutureProviderRegister(Class<I> type, ProviderValue<I> providerValue, ThreadPoolExecutor executorService) {
        super(type, providerValue);
        mThreadPoolExecutor = executorService;
        mSyncWorkStealingWorker = new SyncWorkStealingWorker(mThreadPoolExecutor);
    }

    @Override
    public I get() {
        return mSyncWorkStealingWorker.submit(() -> {
            startLoad();
            try {
                while (!mFutureValue.isDone()) {
                    // try to run next task to avoid deadlock due to limited thread size
                    Runnable nextTask = mThreadPoolExecutor.getQueue().poll();
                    if (nextTask != null) {
                        nextTask.run();
                    }
                }
                return mFutureValue.get();
            } catch (Exception e) {
                Log.e(TAG, getType().getName() + " throws exception with message: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    public void startLoad() {
        mSyncWorkStealingWorker.execute(() -> {
            if (mFutureValue == null) {
                mFutureValue = mThreadPoolExecutor.submit(() -> getProviderValue().get());
            }
        });
    }

    @Override
    public void dispose(Context context) {
        mSyncWorkStealingWorker.execute(() -> {
            if (mFutureValue != null) {
                try {
                    I i = mFutureValue.get();
                    if (i instanceof ProviderDisposable) {
                        ((ProviderDisposable) i).dispose(context);
                    }
                } catch (Exception e) {
                    Log.e(TAG, getType().getName() + " failed to dispose: " + e.getMessage());
                }
            }
            mFutureValue = null;
            mThreadPoolExecutor = null;
            mSyncWorkStealingWorker = null;
        });
    }
}

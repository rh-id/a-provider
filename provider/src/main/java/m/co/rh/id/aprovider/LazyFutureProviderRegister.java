package m.co.rh.id.aprovider;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Helper class to register lazy-loaded future singleton to the provider
 */
class LazyFutureProviderRegister<I> extends ProviderRegister<I> {
    private static final String TAG = "FutureProvider";
    private Future<I> mFutureValue;
    private ExecutorService mExecutorService;

    public LazyFutureProviderRegister(Class<I> type, ProviderValue<I> providerValue, ExecutorService executorService) {
        super(type, providerValue);
        mExecutorService = executorService;
    }

    @Override
    public synchronized I get() {
        long beforeGetTimeMilis = System.currentTimeMillis();
        startLoad();
        try {
            I value = mFutureValue.get();
            long afterGetTimeMilis = System.currentTimeMillis();
            long difference = afterGetTimeMilis - beforeGetTimeMilis;
            // If it takes more than 0.5 seconds it seemed to have performance issue?
            if (difference > 500) {
                Log.w(TAG, getType().getName() + " takes " + difference + " ms");
            }
            return value;
        } catch (Exception e) {
            Log.e(TAG, getType().getName() + " throws exception with message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public synchronized void startLoad() {
        if (mFutureValue == null) {
            mFutureValue = mExecutorService.submit(() -> getProviderValue().get());
        }
    }
}

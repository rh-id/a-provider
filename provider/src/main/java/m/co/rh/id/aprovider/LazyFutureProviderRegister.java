package m.co.rh.id.aprovider;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Helper class to register lazy-loaded future singleton to the provider
 */
class LazyFutureProviderRegister<I> extends ProviderRegister<I> implements ProviderDisposable {
    private static final String TAG = "FutureProvider";
    private Future<I> mFutureValue;
    private ExecutorService mExecutorService;

    public LazyFutureProviderRegister(Class<I> type, ProviderValue<I> providerValue, ExecutorService executorService) {
        super(type, providerValue);
        mExecutorService = executorService;
    }

    @Override
    public synchronized I get() {
        startLoad();
        try {
            return mFutureValue.get();
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

    @Override
    public synchronized void dispose(Context context) {
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
        mExecutorService = null;
    }
}

package m.co.rh.id.aprovider;

import android.content.Context;
import android.util.Log;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

/**
 * Helper class to register pool
 */
class PoolProviderRegister<I> extends ProviderRegister<I> implements ProviderDisposable {
    private static final String TAG = "PoolProvider";

    private ExecutorService mExecutorService;
    private LinkedList<I> mPreviousValues;

    public PoolProviderRegister(Class<I> type, ProviderValue<I> providerValue, ExecutorService executorService) {
        super(type, providerValue);
        mExecutorService = executorService;
        mPreviousValues = new LinkedList<>();
    }

    @Override
    public synchronized I get() {
        I previousVal = getProviderValue().get();
        mPreviousValues.add(previousVal);
        checkAndRemoveDisposedObjects();
        return previousVal;
    }

    private void checkAndRemoveDisposedObjects() {
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
    }

    @Override
    public synchronized void dispose(Context context) {
        while (!mPreviousValues.isEmpty()) {
            I prevValue = mPreviousValues.pop();
            if (prevValue instanceof ProviderDisposable) {
                mExecutorService.execute(() -> {
                    try {
                        ((ProviderDisposable) prevValue).dispose(context);
                    } catch (Exception e) {
                        Log.e(TAG, getType().getName() + " failed to dispose: " + e.getMessage());
                    }
                });
            }
        }
        mPreviousValues = null;
        mExecutorService = null;
    }
}

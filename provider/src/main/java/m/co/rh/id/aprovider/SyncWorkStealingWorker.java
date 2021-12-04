package m.co.rh.id.aprovider;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Special class to synchronize execution in a lock.
 * If lock is not yet acquired then it will try to steal and run task rather than waiting
 */
class SyncWorkStealingWorker {

    private static final String TAG = SyncWorkStealingWorker.class.getName();
    private ThreadPoolExecutor mThreadPoolExecutor;
    private ReentrantLock mLock;

    public SyncWorkStealingWorker(ThreadPoolExecutor threadPoolExecutor) {
        mThreadPoolExecutor = threadPoolExecutor;
        mLock = new ReentrantLock(true);
    }

    public <R> R submit(Callable<R> callable) {
        tryLock();
        R result;
        try {
            result = callable.call();
        } catch (Exception e) {
            Log.e(TAG, "Error executing task", e);
            throw new RuntimeException(e);
        } finally {
            mLock.unlock();
        }
        return result;
    }

    public void execute(Runnable runnable) {
        tryLock();
        try {
            runnable.run();
        } catch (Exception e) {
            Log.e(TAG, "Error executing task", e);
            throw e;
        } finally {
            mLock.unlock();
        }
    }

    private void tryLock() {
        while (!mLock.tryLock()) {
            try {
                Runnable poolRunnable = mThreadPoolExecutor.getQueue().poll();
                if (poolRunnable != null) {
                    poolRunnable.run();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error executing stolen task", e);
                throw e;
            }
        }
    }
}

package m.co.rh.id.aprovider.r8smoke;

import android.content.Context;

import java.util.concurrent.atomic.AtomicInteger;

import m.co.rh.id.aprovider.ProviderDisposable;
import m.co.rh.id.aprovider.ProviderIsDisposed;

/**
 * Pool product: instances are NOT disposed when replaced (unlike factory), only
 * when {@code Provider.dispose()} is invoked - every dispose callback is counted
 * into {@link SmokeResult#POOL_DISPOSE_CALLBACK_COUNT} so the dispose scenario
 * can assert ALL pooled instances were disposed at once.
 */
public class SmokePoolProduct implements ISmokePoolService, ProviderDisposable, ProviderIsDisposed {

    private boolean mDisposed;

    @Override
    public String poolPing() {
        return "pool-pong";
    }

    @Override
    public synchronized void dispose(Context context) {
        mDisposed = true;
        SmokeResult.POOL_DISPOSE_CALLBACK_COUNT.incrementAndGet();
    }

    @Override
    public synchronized boolean isDisposed() {
        return mDisposed;
    }
}

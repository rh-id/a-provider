package m.co.rh.id.aprovider.r8smoke;

import android.content.Context;

import java.util.concurrent.atomic.AtomicInteger;

import m.co.rh.id.aprovider.ProviderDisposable;

/**
 * Factory product. Each instance gets a unique id; when the factory replaces it
 * with the next instance it records the disposed id into {@link SmokeResult}
 * so the runner can assert the PREVIOUS instance (and only that one) was
 * disposed.
 */
public class SmokeFactoryProduct implements ISmokeFactoryProduct, ProviderDisposable {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    private final int mId;

    public SmokeFactoryProduct() {
        mId = ID_COUNTER.incrementAndGet();
    }

    @Override
    public int getProductId() {
        return mId;
    }

    @Override
    public void dispose(Context context) {
        SmokeResult.disposedFactoryProductId = mId;
    }
}

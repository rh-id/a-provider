package m.co.rh.id.aprovider.r8smoke;

import android.content.Context;

import m.co.rh.id.aprovider.ProviderDisposable;

/**
 * Lazy-singleton implementation. The instantiation flag is READ BY APP CODE
 * ONLY (the scenario runner in {@link SmokeApplication}) - the instrumented
 * test never touches this class, so R8 is free to shrink it exactly like in a
 * real consumer app.
 */
public class SmokeLazyServiceImpl implements ISmokeLazyService, ProviderDisposable {

    private static volatile boolean sInstantiated;

    public SmokeLazyServiceImpl() {
        sInstantiated = true;
    }

    /** Read by the app-side scenario runner only. */
    public static boolean isInstantiated() {
        return sInstantiated;
    }

    @Override
    public String lazyPing() {
        return "lazy-pong";
    }

    @Override
    public void dispose(Context context) {
        // The main provider is never disposed in this suite; recorded for
        // completeness of the ProviderDisposable contract coverage.
        SmokeResult.lazyServiceDisposeCallbackRecorded = true;
    }
}

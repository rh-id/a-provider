package m.co.rh.id.aprovider.r8smoke;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Volatile holder for the scenario outcomes of the minified-run harness.
 *
 * The instrumented test (in the separately minified test APK) reads ONLY this
 * class. It must never touch the smoke service impls or their fields directly:
 * a direct reference from test code would keep those classes through the app
 * APK's R8 run and falsify the whole experiment.
 */
public final class SmokeResult {

    // Scenario 1: exact-type register/get + singleton identity.
    public static volatile boolean singletonIdentityOk = false;
    // Scenario 2: interface lookup through Class.isAssignableFrom.
    public static volatile boolean interfaceLookupViaIsAssignableFromOk = false;
    // Scenario 3: concrete interface lookup through Class.isInstance(value).
    public static volatile boolean instanceLookupViaIsInstanceOk = false;
    // Scenario 4: lazyGet returns a working ProviderValue; value created only on first get.
    public static volatile boolean lazyGetProviderValueOk = false;
    // Scenario 4: tryLazyGet on an unregistered type returns a ProviderValue
    // whose get() yields null without throwing.
    public static volatile boolean tryLazyGetUnregisteredReturnsNullOk = false;
    // Scenario 5: tryGet on an unregistered type returns null without throwing.
    public static volatile boolean tryGetUnregisteredReturnsNullOk = false;
    // Scenario 6: get(ChildImpl.class) resolves via the registered parent class.
    public static volatile boolean parentClassLookupOk = false;
    // Scenario 7: registerAsync value became resolvable in the background poll.
    public static volatile boolean asyncServiceResolvedOk = false;
    // Scenario 8: two factory gets returned distinct instances.
    public static volatile boolean factoryDistinctInstancesOk = false;
    // Scenario 8: the previous factory instance was disposed when replaced.
    public static volatile boolean factoryPreviousInstanceDisposedOk = false;
    // Scenario 9: two pool gets returned distinct instances.
    public static volatile boolean poolDistinctInstancesOk = false;
    // Scenario 10: duplicate registration threw IllegalArgumentException.
    public static volatile boolean duplicateRegistrationThrowsOk = false;
    // Scenario 11: setSkipSameType(true) let the duplicate registration pass.
    public static volatile boolean skipSameTypeAllowsDuplicateOk = false;
    // Scenario 12: nested provider resolved a parent-only type via get().
    public static volatile boolean nestedProviderParentTraversalOk = false;
    // Scenario 12: nested provider resolved a parent-only type via lazyGet().
    public static volatile boolean nestedProviderLazyTraversalOk = false;
    // Scenario 13: ProviderDisposable.dispose(Context) was invoked on dispose.
    public static volatile boolean singletonDisposeCallbackRecorded = false;
    // Scenario 13: ProviderModule.dispose(Provider) was invoked on dispose.
    public static volatile boolean moduleDisposeCallbackRecorded = false;
    // Scenario 13: register after dispose() threw IllegalStateException.
    public static volatile boolean registerAfterDisposeThrowsOk = false;
    // Scenario 13: number of pool-instance dispose callbacks (expected 2).
    public static final AtomicInteger POOL_DISPOSE_CALLBACK_COUNT = new AtomicInteger();
    // Scenario 13 helper for the lazy impl dispose contract (never asserted:
    // the main provider is never disposed in this suite).
    public static volatile boolean lazyServiceDisposeCallbackRecorded = false;
    // Scenario 8: id of the factory product that received dispose(Context).
    public static volatile int disposedFactoryProductId = -1;
    // Scenario 14: getContext() returned the application context.
    public static volatile boolean contextOk = false;

    // Set true right before the latch is released.
    public static volatile boolean done = false;
    // Set if any scenario throws an unexpected exception (append-only).
    public static volatile String error = null;

    private static final CountDownLatch LATCH = new CountDownLatch(1);

    private SmokeResult() {
    }

    /** Records an unexpected failure; the test asserts this stays null. */
    public static void recordError(String message) {
        synchronized (SmokeResult.class) {
            error = error == null ? message : error + "\n" + message;
        }
    }

    /** Called by the harness runner once the outcomes are final. */
    public static void done() {
        LATCH.countDown();
    }

    public static void awaitDone(long timeout, TimeUnit unit) throws InterruptedException {
        LATCH.await(timeout, unit);
    }
}

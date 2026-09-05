package m.co.rh.id.aprovider.r8smoke;

import android.app.Application;
import android.content.Context;

import m.co.rh.id.aprovider.Provider;
import m.co.rh.id.aprovider.ProviderValue;

/**
 * Drives the whole smoke suite on a plain background thread started from
 * {@link #onCreate()}. Every scenario records its outcome into
 * {@link SmokeResult}; unexpected exceptions are captured per scenario into
 * {@link SmokeResult#error} instead of crashing, so a single R8 breakage
 * surfaces in the instrumented test as a specific failed assertion (plus a
 * named error) rather than an opaque app crash.
 */
public class SmokeApplication extends Application {

    private static final long ASYNC_TIMEOUT_MILLIS = 30_000;
    private static final long DISPOSE_CALLBACK_TIMEOUT_MILLIS = 10_000;
    private static final long POLL_INTERVAL_MILLIS = 50;

    // Provider 1: main suite. Provider 2: duplicate/skipSameType scenarios.
    // Provider 3: dispose scenarios. mProvider is never disposed.
    private Provider mProvider;
    private ISmokeSingletonService mSingletonInstance;
    private ISmokeChild mChildInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        // Everything runs on a plain background thread: the async scenario's
        // availability poll must never block the main thread.
        new Thread(this::runSmokeSuite, "r8-smoke-runner").start();
    }

    private void runSmokeSuite() {
        try {
            mProvider = Provider.createProvider(getApplicationContext(), new SmokeRootModule());
            runScenario(1, "exact-type register/get + singleton identity",
                    this::scenario01_exactTypeSingleton);
            runScenario(2, "interface lookup via isAssignableFrom",
                    this::scenario02_interfaceLookup);
            runScenario(3, "concrete-value lookup via isInstance",
                    this::scenario03_isInstanceLookup);
            runScenario(4, "lazyGet/tryLazyGet",
                    this::scenario04_lazyGet);
            runScenario(5, "tryGet unregistered returns null",
                    this::scenario05_tryGetUnregistered);
            runScenario(6, "parent-class lookup for unregistered child class",
                    this::scenario06_parentClassLookup);
            runScenario(7, "registerAsync background availability",
                    this::scenario07_registerAsync);
            runScenario(8, "registerFactory distinct instances + previous disposed",
                    this::scenario08_factory);
            runScenario(9, "registerPool distinct instances",
                    this::scenario09_pool);
            runScenario(10, "duplicate registration throws IllegalArgumentException",
                    this::scenario10_duplicateThrows);
            runScenario(11, "setSkipSameType(true) allows duplicate registration",
                    this::scenario11_skipSameType);
            runScenario(12, "nested provider parent traversal",
                    this::scenario12_nestedProvider);
            runScenario(13, "dispose callbacks + post-dispose register",
                    this::scenario13_dispose);
            runScenario(14, "getContext returns the application context",
                    this::scenario14_context);
        } catch (Throwable t) {
            SmokeResult.recordError("smoke suite setup threw: " + t);
        } finally {
            SmokeResult.done = true;
            SmokeResult.done();
        }
    }

    private static void runScenario(int number, String name, Runnable scenario) {
        try {
            scenario.run();
        } catch (Throwable t) {
            SmokeResult.recordError("scenario " + number + " (" + name + ") threw: " + t);
        }
    }

    /** Scenario 1: exact-type register/get + singleton identity. */
    private void scenario01_exactTypeSingleton() {
        ISmokeSingletonService first = mProvider.get(ISmokeSingletonService.class);
        if (first == null) {
            throw new IllegalStateException("get(ISmokeSingletonService.class) returned null");
        }
        if (!(first instanceof SmokeSingletonServiceImpl)) {
            throw new IllegalStateException("exact-type lookup returned the wrong implementation: "
                    + first.getClass().getName());
        }
        if (!"pong".equals(first.smokePing())) {
            throw new IllegalStateException("smokePing() on the resolved singleton failed");
        }
        ISmokeSingletonService second = mProvider.get(ISmokeSingletonService.class);
        if (first != second) {
            throw new IllegalStateException("register() behaved as a factory: two gets returned "
                    + "distinct instances");
        }
        mSingletonInstance = first;
        SmokeResult.singletonIdentityOk = true;
    }

    /** Scenario 2: interface lookup through the Class.isAssignableFrom fallback. */
    private void scenario02_interfaceLookup() {
        ISmokeInterfaceService service = mProvider.get(ISmokeInterfaceService.class);
        if (!(service instanceof SmokeInterfaceServiceImpl)) {
            throw new IllegalStateException("interface lookup returned the wrong implementation: "
                    + (service == null ? "null" : service.getClass().getName()));
        }
        if (!"interface-pong".equals(service.interfacePing())) {
            throw new IllegalStateException("interfacePing() on the resolved service failed");
        }
        SmokeResult.interfaceLookupViaIsAssignableFromOk = true;
    }

    /** Scenario 3: lookup by a never-registered interface via Class.isInstance(value). */
    private void scenario03_isInstanceLookup() {
        ISmokeChild child = mProvider.get(ISmokeChild.class);
        if (!(child instanceof SmokeChildImpl)) {
            throw new IllegalStateException("isInstance lookup for ISmokeChild failed: "
                    + (child == null ? "null" : child.getClass().getName()));
        }
        if (!"child-pong".equals(child.childPing())) {
            throw new IllegalStateException("childPing() on the resolved service failed");
        }
        SmokeBaseService asBase = mProvider.get(SmokeBaseService.class);
        if (asBase != child) {
            throw new IllegalStateException("exact-type lookup of SmokeBaseService returned a "
                    + "different instance than the ISmokeChild lookup");
        }
        mChildInstance = child;
        SmokeResult.instanceLookupViaIsInstanceOk = true;
    }

    /** Scenario 4: lazyGet/tryLazyGet semantics. */
    private void scenario04_lazyGet() {
        if (SmokeLazyServiceImpl.isInstantiated()) {
            throw new IllegalStateException("lazy impl was instantiated before the first get "
                    + "(registerLazy became eager)");
        }
        ProviderValue<ISmokeLazyService> lazyValue = mProvider.lazyGet(ISmokeLazyService.class);
        if (lazyValue == null) {
            throw new IllegalStateException("lazyGet returned null");
        }
        if (SmokeLazyServiceImpl.isInstantiated()) {
            throw new IllegalStateException("lazyGet instantiated the impl before "
                    + "ProviderValue.get() was called");
        }
        ISmokeLazyService lazy = lazyValue.get();
        if (!(lazy instanceof SmokeLazyServiceImpl)) {
            throw new IllegalStateException("lazyGet ProviderValue returned the wrong implementation");
        }
        if (!SmokeLazyServiceImpl.isInstantiated()) {
            throw new IllegalStateException("impl was not instantiated after ProviderValue.get()");
        }
        if (!"lazy-pong".equals(lazy.lazyPing())) {
            throw new IllegalStateException("lazyPing() on the resolved service failed");
        }
        if (lazyValue.get() != lazy) {
            throw new IllegalStateException("ProviderValue did not cache the lazy singleton");
        }
        SmokeResult.lazyGetProviderValueOk = true;

        ProviderValue<SmokeUnregisteredService> unregistered =
                mProvider.tryLazyGet(SmokeUnregisteredService.class);
        if (unregistered == null) {
            throw new IllegalStateException("tryLazyGet returned null for an unregistered type");
        }
        if (unregistered.get() != null) {
            throw new IllegalStateException("tryLazyGet().get() returned a value for an "
                    + "unregistered type instead of null");
        }
        SmokeResult.tryLazyGetUnregisteredReturnsNullOk = true;
    }

    /** Scenario 5: tryGet on an unregistered type returns null without throwing. */
    private void scenario05_tryGetUnregistered() {
        if (mProvider.tryGet(SmokeUnregisteredService.class) != null) {
            throw new IllegalStateException("tryGet returned a value for an unregistered type");
        }
        SmokeResult.tryGetUnregisteredReturnsNullOk = true;
    }

    /**
     * Scenario 6: get(ChildImpl.class) where only the PARENT class
     * (SmokeBaseService) is registered - the first registered parent class wins.
     */
    private void scenario06_parentClassLookup() {
        SmokeChildImpl viaParent = mProvider.get(SmokeChildImpl.class);
        if (viaParent == null) {
            throw new IllegalStateException("parent-class lookup returned null");
        }
        if (mChildInstance != null && viaParent != mChildInstance) {
            throw new IllegalStateException("parent-class lookup returned a different instance "
                    + "than the isInstance lookup of the same registration");
        }
        SmokeResult.parentClassLookupOk = true;
    }

    /** Scenario 7: registerAsync - poll tryGet in the background until resolvable. */
    private void scenario07_registerAsync() {
        long deadline = System.currentTimeMillis() + ASYNC_TIMEOUT_MILLIS;
        ISmokeAsyncService async = null;
        while (async == null && System.currentTimeMillis() < deadline) {
            async = mProvider.tryGet(ISmokeAsyncService.class);
            if (async == null) {
                try {
                    Thread.sleep(POLL_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        if (async == null) {
            throw new IllegalStateException("registerAsync service not resolvable within "
                    + ASYNC_TIMEOUT_MILLIS + "ms");
        }
        if (!(async instanceof SmokeAsyncServiceImpl)) {
            throw new IllegalStateException("registerAsync resolved the wrong implementation: "
                    + async.getClass().getName());
        }
        if (!"async-pong".equals(async.asyncPing())) {
            throw new IllegalStateException("asyncPing() on the resolved service failed");
        }
        SmokeResult.asyncServiceResolvedOk = true;
    }

    /** Scenario 8: factory produces distinct instances; previous instance disposed. */
    private void scenario08_factory() {
        ISmokeFactoryProduct first = mProvider.get(ISmokeFactoryProduct.class);
        if (!(first instanceof SmokeFactoryProduct)) {
            throw new IllegalStateException("factory lookup returned the wrong implementation");
        }
        ISmokeFactoryProduct second = mProvider.get(ISmokeFactoryProduct.class);
        if (!(second instanceof SmokeFactoryProduct)) {
            throw new IllegalStateException("factory lookup returned the wrong implementation");
        }
        if (first == second) {
            throw new IllegalStateException("registerFactory returned the same instance twice");
        }
        if (first.getProductId() == second.getProductId()) {
            throw new IllegalStateException("registerFactory reused the same product id");
        }
        if (SmokeResult.disposedFactoryProductId != first.getProductId()) {
            throw new IllegalStateException("the PREVIOUS factory instance (id "
                    + first.getProductId() + ") was not disposed when a new one was fetched; "
                    + "last disposed id=" + SmokeResult.disposedFactoryProductId);
        }
        SmokeResult.factoryDistinctInstancesOk = true;
        SmokeResult.factoryPreviousInstanceDisposedOk = true;
    }

    /** Scenario 9: pool produces distinct, not-yet-disposed instances. */
    private void scenario09_pool() {
        ISmokePoolService first = mProvider.get(ISmokePoolService.class);
        ISmokePoolService second = mProvider.get(ISmokePoolService.class);
        if (!(first instanceof SmokePoolProduct) || !(second instanceof SmokePoolProduct)) {
            throw new IllegalStateException("pool lookup returned the wrong implementation");
        }
        if (first == second) {
            throw new IllegalStateException("registerPool returned the same instance twice");
        }
        if (((SmokePoolProduct) first).isDisposed() || ((SmokePoolProduct) second).isDisposed()) {
            throw new IllegalStateException("pool instance reported isDisposed()=true before "
                    + "provider.dispose() (ProviderIsDisposed contract broken)");
        }
        SmokeResult.poolDistinctInstancesOk = true;
    }

    /** Scenario 10: duplicate registration must throw IllegalArgumentException. */
    private void scenario10_duplicateThrows() {
        try {
            Provider.createProvider(getApplicationContext(),
                    (providerRegistry, provider) -> {
                        providerRegistry.register(ISmokeDuplicateService.class,
                                SmokeDuplicateServiceImpl::new);
                        providerRegistry.register(ISmokeDuplicateService.class,
                                SmokeDuplicateServiceImpl::new);
                    });
        } catch (IllegalArgumentException expected) {
            SmokeResult.duplicateRegistrationThrowsOk = true;
            return;
        }
        throw new IllegalStateException("duplicate registration did not throw "
                + "IllegalArgumentException");
    }

    /** Scenario 11: setSkipSameType(true) must let the duplicate registration pass. */
    private void scenario11_skipSameType() {
        Provider second = Provider.createProvider(getApplicationContext(),
                (providerRegistry, provider) -> {
                    providerRegistry.setSkipSameType(true);
                    providerRegistry.register(ISmokeDuplicateService.class,
                            SmokeDuplicateServiceImpl::new);
                    providerRegistry.register(ISmokeDuplicateService.class,
                            SmokeDuplicateServiceImpl::new);
                });
        if (second.get(ISmokeDuplicateService.class) == null) {
            throw new IllegalStateException("provider created with skipSameType=true cannot "
                    + "resolve the duplicated type");
        }
        SmokeResult.skipSameTypeAllowsDuplicateOk = true;
    }

    /** Scenario 12: nested provider traverses to the parent for parent-only types. */
    private void scenario12_nestedProvider() {
        Provider child = Provider.createNestedProvider("smoke-child", mProvider,
                getApplicationContext(), new SmokeEmptyModule());
        ISmokeSingletonService fromChild = child.get(ISmokeSingletonService.class);
        if (!(fromChild instanceof SmokeSingletonServiceImpl)) {
            throw new IllegalStateException("nested provider did not traverse to the parent "
                    + "provider for a parent-only type");
        }
        if (mSingletonInstance != null && fromChild != mSingletonInstance) {
            throw new IllegalStateException("nested provider returned a different instance than "
                    + "the parent for a parent-only type");
        }
        SmokeResult.nestedProviderParentTraversalOk = true;

        ProviderValue<ISmokeSingletonService> lazy = child.lazyGet(ISmokeSingletonService.class);
        if (lazy == null || lazy.get() != fromChild) {
            throw new IllegalStateException("nested provider lazyGet did not traverse to the "
                    + "parent provider");
        }
        SmokeResult.nestedProviderLazyTraversalOk = true;
    }

    /** Scenario 13: dispose callbacks (service, module, pool) + post-dispose register. */
    private void scenario13_dispose() {
        SmokeDisposingModule module = new SmokeDisposingModule();
        Provider third = Provider.createProvider(getApplicationContext(), module);
        ISmokeSingletonService probe = third.get(ISmokeSingletonService.class);
        if (!(probe instanceof SmokeSingletonServiceImpl)) {
            throw new IllegalStateException("dispose-probe provider returned the wrong "
                    + "implementation");
        }
        ISmokePoolService poolFirst = third.get(ISmokePoolService.class);
        ISmokePoolService poolSecond = third.get(ISmokePoolService.class);
        if (poolFirst == poolSecond) {
            throw new IllegalStateException("registerPool returned the same instance twice on "
                    + "the dispose-probe provider");
        }

        third.dispose();

        // dispose() fans the ProviderDisposable callbacks out on the provider's
        // executor - wait (bounded) for all of them to land.
        long deadline = System.currentTimeMillis() + DISPOSE_CALLBACK_TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline
                && (!SmokeResult.singletonDisposeCallbackRecorded
                || !SmokeResult.moduleDisposeCallbackRecorded
                || SmokeResult.POOL_DISPOSE_CALLBACK_COUNT.get() < 2)) {
            try {
                Thread.sleep(POLL_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (!SmokeResult.singletonDisposeCallbackRecorded) {
            throw new IllegalStateException("ProviderDisposable.dispose(Context) was not invoked "
                    + "when provider.dispose() ran");
        }
        if (!SmokeResult.moduleDisposeCallbackRecorded) {
            throw new IllegalStateException("ProviderModule.dispose(Provider) was not invoked "
                    + "when provider.dispose() ran");
        }
        if (SmokeResult.POOL_DISPOSE_CALLBACK_COUNT.get() != 2) {
            throw new IllegalStateException("expected BOTH pool instances to be disposed exactly "
                    + "once on provider.dispose(), got count="
                    + SmokeResult.POOL_DISPOSE_CALLBACK_COUNT.get());
        }
        try {
            module.registeredRegistry.register(ISmokeSingletonService.class,
                    SmokeSingletonServiceImpl::new);
        } catch (IllegalStateException expected) {
            SmokeResult.registerAfterDisposeThrowsOk = true;
            return;
        }
        throw new IllegalStateException("register after dispose() did not throw "
                + "IllegalStateException");
    }

    /** Scenario 14: getContext() returns the context the provider was created with. */
    private void scenario14_context() {
        Context context = mProvider.getContext();
        if (context == null) {
            throw new IllegalStateException("getContext() returned null");
        }
        if (context != getApplicationContext()) {
            throw new IllegalStateException("getContext() did not return the application context");
        }
        SmokeResult.contextOk = true;
    }
}

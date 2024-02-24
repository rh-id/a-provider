package m.co.rh.id.aprovider;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import co.rh.id.lib.concurrent_utils.concurrent.executor.WeightedThreadPool;

/**
 * Default implementation of the provider
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class DefaultProvider implements Provider, ProviderRegistry {
    private static final String TAG = "DefaultProvider";
    private static ExecutorService sExecutorService;

    private static synchronized ExecutorService initExecutorService() {
        if (sExecutorService == null) {
            WeightedThreadPool weightedThreadPool = new WeightedThreadPool();
            weightedThreadPool.setMaxWeight(5);
            weightedThreadPool.setThreadTimeoutMillis(10_000);
            sExecutorService = weightedThreadPool;
        }
        return sExecutorService;
    }

    private Context mContext;
    private Set<ProviderRegister> mRegistry;
    private List<ProviderModule> mModuleList;
    private List<LazyFutureProviderRegister> mAsyncRegisterList;
    private ExecutorService mExecutorService;
    private ProviderModule mRootModule;
    private boolean mIsDisposed;

    private boolean skipSameType;

    DefaultProvider(Context context, ProviderModule rootModule) {
        this(context, rootModule, initExecutorService(), true);
    }

    DefaultProvider(Context context, ProviderModule rootModule, boolean autoStart) {
        this(context, rootModule, initExecutorService(), autoStart);
    }

    DefaultProvider(Context context, ProviderModule rootModule, ExecutorService executorService) {
        this(context, rootModule, executorService, true);
    }

    DefaultProvider(Context context, ProviderModule rootModule, ExecutorService executorService, boolean autoStart) {
        mContext = context;
        mRegistry = new LinkedHashSet<>();
        mRegistry.add(new SingletonProviderRegister(ProviderRegistry.class, () -> this));
        mModuleList = Collections.synchronizedList(new ArrayList<>());
        mAsyncRegisterList = Collections.synchronizedList(new ArrayList<>());
        mExecutorService = executorService;
        mRootModule = rootModule;
        if (autoStart) {
            start();
        }
    }

    private Object getValue(Class clazz) {
        Object val = null;
        for (ProviderRegister providerRegister : mRegistry) {
            Class type = providerRegister.getType();
            if (type.getName().equals(clazz.getName())) {
                val = providerRegister.get();
                break;
            }
        }
        return val;
    }

    @Override
    public <I> I get(Class<I> clazz) {
        Object result = getValue(clazz);
        if (result != null) {
            return processObject(result);
        }
        for (ProviderRegister providerRegister : mRegistry) {
            if (clazz.isAssignableFrom(providerRegister.getType())) {
                return processObject(providerRegister.get());
            } else if (
                    (!(providerRegister instanceof LazyFutureProviderRegister) &&
                            !(providerRegister instanceof LazySingletonProviderRegister)
                    )
                            &&
                            clazz.isInstance(providerRegister.get())) {
                return processObject(providerRegister.get());
            }
        }
        throw new ProviderNullPointerException(clazz.getName() + " not found");
    }

    @Override
    public <I> I tryGet(Class<I> clazz) {
        try {
            return get(clazz);
        } catch (ProviderNullPointerException e) {
            // Leave blank, this means get return null, not the service itself throws null pointer
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public <I> ProviderValue<I> lazyGet(Class<I> clazz) {
        // check existence of the object without processing ProviderRegister
        boolean classFound = false;
        for (ProviderRegister providerRegister : mRegistry) {
            Class type = providerRegister.getType();
            if (clazz.getName().equals(type.getName())) {
                classFound = true;
                break;
            } else if (clazz.isAssignableFrom(type)) {
                classFound = true;
                break;
            } else if (clazz.isInstance(providerRegister.get())) {
                classFound = true;
                break;
            }
        }
        if (!classFound) {
            throw new ProviderNullPointerException(clazz.getName() + " not found");
        }
        return new CachedProviderValue<>(() -> get(clazz));
    }

    @Override
    public <I> ProviderValue<I> tryLazyGet(Class<I> clazz) {
        return new CachedProviderValue<>(() -> tryGet(clazz));
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public synchronized void dispose() {
        if (mIsDisposed) {
            return;
        }
        mIsDisposed = true;
        if (!mModuleList.isEmpty()) {
            for (ProviderModule providerModule : mModuleList) {
                providerModule.dispose(this);
            }
            mModuleList.clear();
            mModuleList = null;
        }
        final Context disposeContext = mContext;
        for (ProviderRegister entry : mRegistry) {
            if (entry instanceof ProviderDisposable) {
                mExecutorService.execute(() ->
                        entry.dispose(disposeContext));
            }
        }
        mRegistry.clear();
        mRegistry = null;
        mAsyncRegisterList.clear();
        mAsyncRegisterList = null;
        mExecutorService = null;
        mRootModule = null;
        mContext = null;
    }

    @Override
    public void setSkipSameType(boolean skip) {
        skipSameType = skip;
    }

    @Override
    public void registerModule(ProviderModule providerModule) {
        checkDisposed();
        providerModule.provides(this, this);
        mModuleList.add(providerModule);
    }

    @Override
    public <I> void register(Class<I> clazz, ProviderValue<I> providerValue) {
        checkDisposed();
        putValue(new SingletonProviderRegister<>(clazz, providerValue));
    }

    @Override
    public <I> void registerLazy(Class<I> clazz, ProviderValue<I> providerValue) {
        checkDisposed();
        putValue(new LazySingletonProviderRegister<>(clazz, providerValue));
    }

    @Override
    public <I> void registerAsync(Class<I> clazz, ProviderValue<I> providerValue) {
        checkDisposed();
        LazyFutureProviderRegister providerRegister = new LazyFutureProviderRegister(clazz, providerValue, mExecutorService);
        boolean registered = putValue(providerRegister);
        if (registered) {
            mAsyncRegisterList.add(providerRegister);
        }
    }

    @Override
    public <I> void registerFactory(Class<I> clazz, ProviderValue<I> providerValue) {
        checkDisposed();
        putValue(new FactoryProviderRegister<>(clazz, providerValue, mContext));
    }

    @Override
    public <I> void registerPool(Class<I> clazz, ProviderValue<I> providerValue) {
        checkDisposed();
        putValue(new PoolProviderRegister<>(clazz, providerValue, mExecutorService));
    }

    private synchronized void checkDisposed() {
        if (mIsDisposed) {
            throw new IllegalStateException("This provider was disposed, please create new instance");
        }
    }

    private <I> I processObject(Object result) {
        if (result instanceof ProviderRegister) {
            return (I) ((ProviderRegister) result).get();
        }
        return (I) result;
    }

    private <I> boolean putValue(ProviderRegister<I> implementation) {
        Class clazz = implementation.getType();
        boolean added;
        if (implementation instanceof SingletonProviderRegister) {
            implementation.get();
        }
        added = mRegistry.add(implementation);
        if (added) {
            return true;
        } else {
            if (skipSameType) {
                Log.w(TAG, "Skipping " + clazz.getName());
            } else {
                throw new IllegalArgumentException("Duplicate " + clazz.getName() + " found");
            }
            return false;
        }
    }

    void start() {
        registerModule(mRootModule);
        if (!mAsyncRegisterList.isEmpty()) {
            for (LazyFutureProviderRegister lazyFutureProviderRegister : mAsyncRegisterList) {
                lazyFutureProviderRegister.startLoad();
            }
            mAsyncRegisterList.clear();
        }
    }
}
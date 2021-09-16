package m.co.rh.id.provider;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the provider
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class DefaultProvider implements Provider, ProviderRegistry {
    private static final String TAG = "DefaultProvider";
    private static ThreadPoolExecutor sThreadPoolExecutor;

    private static synchronized void initThreadPool() {
        if (sThreadPoolExecutor == null) {
            sThreadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                    Integer.MAX_VALUE,
                    30, TimeUnit.SECONDS, new SynchronousQueue<>());
            sThreadPoolExecutor.allowCoreThreadTimeOut(true);
            sThreadPoolExecutor.prestartAllCoreThreads();
        }
    }

    private Context mContext;
    private Map<Class, Object> mObjectMap;
    private List<ProviderModule> mModuleList;
    private List<LazyFutureProviderRegister> mAsyncRegisterList;
    private boolean mIsDisposed;

    DefaultProvider(Context context, ProviderModule rootModule) {
        mContext = context;
        mObjectMap = new ConcurrentHashMap<>();
        mModuleList = Collections.synchronizedList(new ArrayList<>());
        mAsyncRegisterList = Collections.synchronizedList(new ArrayList<>());
        initThreadPool();
        registerModule(rootModule);
        // after all things are registered, trigger load for all futures
        loadAllAsyncRegisters();
    }

    @Override
    public <I> I get(Class<I> clazz) {
        Object result = mObjectMap.get(clazz);
        if (result != null) {
            return processObject(result);
        }
        for (Map.Entry<Class, Object> entry : mObjectMap.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                return processObject(entry.getValue());
            } else if (clazz.isInstance(entry.getValue())) {
                return processObject(entry.getValue());
            }
        }
        throw new DefaultProviderNullPointerException(clazz.getName() + " not found");
    }

    @Override
    public <I> I tryGet(Class<I> clazz) {
        try {
            return get(clazz);
        } catch (DefaultProviderNullPointerException e) {
            // Leave blank, this means get return null, not the service itself throws null pointer
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public synchronized void dispose() {
        mIsDisposed = true;
        if (!mModuleList.isEmpty()) {
            for (ProviderModule providerModule : mModuleList) {
                providerModule.dispose(mContext, this);
            }
            mModuleList.clear();
        }
        mObjectMap.clear();
        mContext = null;
    }

    @Override
    public void registerModule(ProviderModule providerModule) {
        checkDisposed();
        providerModule.provides(mContext, this, this);
        mModuleList.add(providerModule);
    }

    @Override
    public <I> void register(Class<I> clazz, I implementation) {
        checkDisposed();
        putValue(clazz, implementation);
    }

    @Override
    public <I> void registerLazy(Class<I> clazz, ProviderValue<I> providerValue) {
        register(new LazySingletonProviderRegister<>(clazz, providerValue));
    }

    @Override
    public <I> void registerAsync(Class<I> clazz, ProviderValue<I> providerValue) {
        LazyFutureProviderRegister providerRegister = new LazyFutureProviderRegister(clazz, providerValue, sThreadPoolExecutor);
        register(providerRegister);
        mAsyncRegisterList.add(providerRegister);
    }

    @Override
    public <I> void registerFactory(Class<I> clazz, ProviderValue<I> providerValue) {
        register(new FactoryProviderRegister<>(clazz, providerValue));
    }

    private synchronized void checkDisposed() {
        if (mIsDisposed) {
            throw new IllegalStateException("This provider was disposed, please create new instance");
        }
    }

    private <I> void register(ProviderRegister<I> providerRegister) {
        checkDisposed();
        putValue(providerRegister.getType(), providerRegister);
    }

    private <I> I processObject(Object result) {
        if (result instanceof ProviderRegister) {
            return (I) ((ProviderRegister) result).get();
        }
        return (I) result;
    }

    private <I> void putValue(Class<I> clazz, Object implementation) {
        I tryGetResult = null;
        try {
            tryGetResult = get(clazz);
        } catch (DefaultProviderNullPointerException e) {
            // leave blank
        }
        if (tryGetResult == null) {
            mObjectMap.put(clazz, implementation);
        } else {
            throw new IllegalArgumentException("Duplicate " + clazz.getName() + " found");
        }
    }

    private void loadAllAsyncRegisters() {
        if (!mAsyncRegisterList.isEmpty()) {
            for (LazyFutureProviderRegister lazyFutureProviderRegister : mAsyncRegisterList) {
                lazyFutureProviderRegister.startLoad();
            }
            mAsyncRegisterList.clear();
        }
    }

    private static class DefaultProviderNullPointerException extends NullPointerException {
        public DefaultProviderNullPointerException(String message) {
            super(message);
        }
    }
}
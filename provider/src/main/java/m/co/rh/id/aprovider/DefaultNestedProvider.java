package m.co.rh.id.aprovider;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Nested provider implementation
 */
class DefaultNestedProvider extends DefaultProvider {
    private static final String TAG = "NestedDefaultProvider";

    private String mName;
    private Provider mParentProvider;

    DefaultNestedProvider(String name, Provider parentProvider, Context context, ProviderModule rootModule) {
        super(context, rootModule, false);
        mName = name;
        mParentProvider = parentProvider;
        start();
    }

    DefaultNestedProvider(String name, Provider parentProvider, Context context, ProviderModule rootModule, ThreadPoolExecutor threadPoolExecutor) {
        super(context, rootModule, threadPoolExecutor, false);
        mName = name;
        mParentProvider = parentProvider;
        start();
    }

    @Override
    public <I> I get(Class<I> clazz) {
        try {
            return super.get(clazz);
        } catch (ProviderNullPointerException e) {
            Log.d(TAG, clazz.getName() + " not found in " + mName
                    + ", try to find at parent");
            if (mParentProvider != null) {
                return mParentProvider.get(clazz);
            }
        }
        throw new ProviderNullPointerException(clazz.getName() + " not found");
    }

    @Override
    public <I> ProviderValue<I> lazyGet(Class<I> clazz) {
        try {
            return super.lazyGet(clazz);
        } catch (ProviderNullPointerException e) {
            Log.d(TAG, clazz.getName() + " not found in " + mName
                    + ", try to find at parent");
            if (mParentProvider != null) {
                return mParentProvider.lazyGet(clazz);
            }
        }
        throw new ProviderNullPointerException(clazz.getName() + " not found");
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        mName = null;
        mParentProvider = null;
    }
}
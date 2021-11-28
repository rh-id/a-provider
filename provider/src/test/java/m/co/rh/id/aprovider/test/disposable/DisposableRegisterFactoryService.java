package m.co.rh.id.aprovider.test.disposable;

import android.content.Context;

import m.co.rh.id.aprovider.ProviderDisposable;

/**
 * Test class, use Provider.registerFactory to register this class
 */
public class DisposableRegisterFactoryService implements ProviderDisposable {
    @Override
    public void dispose(Context context) {
        // leave blank, use mock to check if this is called or not
    }
}

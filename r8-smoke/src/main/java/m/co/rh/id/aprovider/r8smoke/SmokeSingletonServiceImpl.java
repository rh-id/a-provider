package m.co.rh.id.aprovider.r8smoke;

import android.content.Context;

import m.co.rh.id.aprovider.ProviderDisposable;

/**
 * Singleton implementation; its {@link #dispose(Context)} records the callback
 * into {@link SmokeResult} so the dispose scenario (provider 3) can assert it.
 */
public class SmokeSingletonServiceImpl implements ISmokeSingletonService, ProviderDisposable {

    @Override
    public String smokePing() {
        return "pong";
    }

    @Override
    public void dispose(Context context) {
        SmokeResult.singletonDisposeCallbackRecorded = true;
    }
}

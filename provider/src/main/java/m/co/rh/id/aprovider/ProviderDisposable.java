package m.co.rh.id.aprovider;

import android.content.Context;

/**
 * Contract class to indicate that an object is to be disposed when provider is disposed.
 * Implement this class to components to handle Provider.dispose event.
 */
public interface ProviderDisposable {

    /**
     * Handle provider.dispose for current component
     *
     * @param context the context of the provider
     */
    void dispose(Context context);
}

package m.co.rh.id.provider;

/**
 * Helper class to execute action on success and on error
 */
public interface ProviderAction<I> {
    /**
     * on success do
     */
    void onSuccess(I value);

    /**
     * on error do
     */
    void onError(Exception exception);
}

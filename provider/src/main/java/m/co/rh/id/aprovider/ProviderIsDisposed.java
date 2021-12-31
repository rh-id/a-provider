package m.co.rh.id.aprovider;

/**
 * Contract class to indicate that an object has been disposed.
 * Used for memory optimization on registerPool
 */
public interface ProviderIsDisposed {

    /**
     * @return true if this object has been disposed
     */
    boolean isDisposed();
}

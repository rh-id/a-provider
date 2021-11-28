package m.co.rh.id.aprovider;

/**
 * Provider null exception to differentiate between this library null pointer
 * or other null pointer outside this library
 */
class ProviderNullPointerException extends NullPointerException {
    public ProviderNullPointerException(String message) {
        super(message);
    }
}

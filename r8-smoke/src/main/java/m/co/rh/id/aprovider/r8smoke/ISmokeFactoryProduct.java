package m.co.rh.id.aprovider.r8smoke;

/**
 * Scenario 8 service: registered with {@code registerFactory}; every
 * {@code Provider.get} must produce a distinct instance.
 */
public interface ISmokeFactoryProduct {
    int getProductId();
}

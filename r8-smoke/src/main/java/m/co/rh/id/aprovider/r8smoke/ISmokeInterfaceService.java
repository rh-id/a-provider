package m.co.rh.id.aprovider.r8smoke;

/**
 * Scenario 2 service interface: the impl is registered under its CONCRETE class
 * type, so looking this interface up can only succeed through the
 * {@code Class.isAssignableFrom} fallback path.
 */
public interface ISmokeInterfaceService {
    String interfacePing();
}

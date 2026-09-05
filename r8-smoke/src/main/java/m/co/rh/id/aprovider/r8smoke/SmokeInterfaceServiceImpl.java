package m.co.rh.id.aprovider.r8smoke;

/**
 * Scenario 2 implementation: registered under its concrete class
 * ({@code register(SmokeInterfaceServiceImpl.class, ...)}), retrieved through
 * {@link ISmokeInterfaceService} to exercise the isAssignableFrom lookup path.
 */
public class SmokeInterfaceServiceImpl implements ISmokeInterfaceService {

    @Override
    public String interfacePing() {
        return "interface-pong";
    }
}

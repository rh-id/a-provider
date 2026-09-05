package m.co.rh.id.aprovider.r8smoke;

/**
 * Scenario 3/6 registered type: a CONCRETE (non-interface) parent class. The
 * provider registers {@link SmokeChildImpl} under this type, then the harness
 * queries the never-registered child/iface types to exercise the
 * {@code Class.isInstance} value-fallback lookup path and the
 * first-registered-parent-class feature.
 */
public class SmokeBaseService {

    public String basePing() {
        return "base-pong";
    }
}

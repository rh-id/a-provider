package m.co.rh.id.aprovider.r8smoke;

/**
 * Registered under its PARENT class {@link SmokeBaseService} only. Both
 * {@code get(ISmokeChild.class)} (isInstance path) and
 * {@code get(SmokeChildImpl.class)} (exact class not registered; first
 * registered parent class wins) must return this instance.
 */
public class SmokeChildImpl extends SmokeBaseService implements ISmokeChild {

    @Override
    public String childPing() {
        return "child-pong";
    }
}

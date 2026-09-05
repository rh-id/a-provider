package m.co.rh.id.aprovider.r8smoke;

/**
 * Interface implemented only by {@link SmokeChildImpl}; never registered in the
 * provider, so a lookup can only succeed via the isInstance value fallback.
 */
public interface ISmokeChild {
    String childPing();
}

package m.co.rh.id.aprovider.r8smoke;

/**
 * Scenario 7 service: registered with {@code registerAsync}; the value is
 * created on a background thread and the runner polls for availability.
 */
public interface ISmokeAsyncService {
    String asyncPing();
}

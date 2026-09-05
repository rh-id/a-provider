package m.co.rh.id.aprovider.r8smoke;

/**
 * Scenario 7 implementation: instantiated by registerAsync on the provider's
 * background executor.
 */
public class SmokeAsyncServiceImpl implements ISmokeAsyncService {

    @Override
    public String asyncPing() {
        return "async-pong";
    }
}

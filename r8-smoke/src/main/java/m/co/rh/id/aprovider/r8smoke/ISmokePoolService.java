package m.co.rh.id.aprovider.r8smoke;

/**
 * Scenario 9 service: registered with {@code registerPool}; every
 * {@code Provider.get} produces a distinct instance that is kept until
 * provider.dispose() (or pruned via {@code ProviderIsDisposed}).
 */
public interface ISmokePoolService {
    String poolPing();
}

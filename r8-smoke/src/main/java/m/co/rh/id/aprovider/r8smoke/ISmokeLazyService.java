package m.co.rh.id.aprovider.r8smoke;

/**
 * Scenario 4 service: registered with {@code registerLazy}, so the impl must
 * NOT be instantiated before the first {@code Provider.get}/
 * {@code ProviderValue.get()}.
 */
public interface ISmokeLazyService {
    String lazyPing();
}

package m.co.rh.id.aprovider.r8smoke;

/**
 * Deliberately NEVER registered in any provider - proves {@code tryGet} and
 * {@code tryLazyGet} return null for unknown types without throwing.
 */
public class SmokeUnregisteredService {
}

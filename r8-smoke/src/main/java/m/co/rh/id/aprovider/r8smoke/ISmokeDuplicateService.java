package m.co.rh.id.aprovider.r8smoke;

/**
 * Scenario 10/11 service: registered twice to prove the duplicate-registration
 * IllegalArgumentException (and its suppression via setSkipSameType(true)).
 */
public interface ISmokeDuplicateService {
    String duplicatePing();
}

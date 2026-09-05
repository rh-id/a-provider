package m.co.rh.id.aprovider.r8smoke;

/**
 * Scenario 1/12/13 service: registered as an eager singleton under this exact
 * interface type in the main provider (and again in the dispose-probe provider).
 */
public interface ISmokeSingletonService {
    String smokePing();
}

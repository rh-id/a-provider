package m.co.rh.id.aprovider.r8smoke;

/** Implementation for the duplicate-registration scenarios (10/11). */
public class SmokeDuplicateServiceImpl implements ISmokeDuplicateService {

    @Override
    public String duplicatePing() {
        return "duplicate-pong";
    }
}

package m.co.rh.id.aprovider.test;

public class ServiceBImpl implements IServiceB {
    private IServiceA iServiceA;

    public ServiceBImpl(IServiceA iServiceA) {
        this.iServiceA = iServiceA;
    }

    @Override
    public IServiceA getIServiceA() {
        return iServiceA;
    }
}

package model.broker;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class RunningBroker {
    private InetAddress address = null;
    private Boolean hasAContainerRunningWithThisBroker;

    public RunningBroker(InetAddress address, Boolean hasAContainerRunningWithThisBroker) {
        this.address = address;
        this.hasAContainerRunningWithThisBroker = hasAContainerRunningWithThisBroker;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public Boolean getHasAContainerRunningWithThisBroker() {
        return hasAContainerRunningWithThisBroker;
    }

    public void setHasAContainerRunningWithThisBroker(Boolean hasAContainerRunningWithThisBroker) {
        this.hasAContainerRunningWithThisBroker = hasAContainerRunningWithThisBroker;
    }
}

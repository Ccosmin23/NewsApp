package model.broker;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class RunningBroker {
    private InetAddress address = null;
    private AtomicBoolean hasAContainerRunningWithThisBroker;

    public RunningBroker(InetAddress address, AtomicBoolean hasAContainerRunningWithThisBroker) {
        this.address = address;
        this.hasAContainerRunningWithThisBroker = hasAContainerRunningWithThisBroker;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public AtomicBoolean getHasAContainerRunningWithThisBroker() {
        return hasAContainerRunningWithThisBroker;
    }

    public void setHasAContainerRunningWithThisBroker(AtomicBoolean hasAContainerRunningWithThisBroker) {
        this.hasAContainerRunningWithThisBroker = hasAContainerRunningWithThisBroker;
    }
}

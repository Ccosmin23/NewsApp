package architecture;

import java.net.InetAddress;

public class RingNode {
    private InetAddress inetAddress;
    private RingNode nextNode;

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public RingNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(RingNode nextNode) {
        this.nextNode = nextNode;
    }

}

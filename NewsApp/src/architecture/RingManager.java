package architecture;

import service.BrokerService;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class RingManager {
    private final BrokerService brokerService;
    private final Timer heartbeatTimer;

    public RingManager(BrokerService brokerService) {
        this.brokerService = brokerService;
        this.heartbeatTimer = new Timer();
    }

    public void startHeartbeat() {
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendHeartbeat();
            }
        }, 0, 5000); // la fiecare 5 secunde se trimite un semnal "heartbeat" ca sa verificam nodurile
    }

    private void sendHeartbeat() {
        try {
            InetAddress nextNode = brokerService.getNodUrmator() != null ? brokerService.getNodUrmator() : brokerService.getAdreseNoduri().get(0);
            brokerService.send(nextNode);

        } catch (ClassNotFoundException e) {
            System.out.println("\nnodul = " + brokerService.getNodUrmator() + " inca merge");
        }
    }

    public void stopHeartbeat() {
        heartbeatTimer.cancel();
        heartbeatTimer.purge();
    }
}

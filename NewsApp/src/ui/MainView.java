package ui;

import utils.StringUtils;

public class MainView {
    public static void showMenuMessages() {
        String publisherBolded = StringUtils.applyBoldTo("publisher", true);
        String subscriberBolded = StringUtils.applyBoldTo("subscriber", true);
        String brokerBolded = StringUtils.applyBoldTo("broker", true);

        System.out.println("Aplicatia nu poate porni. Te rugam sa adaugi unul dintre urmatoarele argumente:"
                + publisherBolded + ", " + subscriberBolded + " or " + brokerBolded + "\n" +
                publisherBolded + " -> incepe executia ca si publisher\n" +
                subscriberBolded + " -> incepe executia ca si subscriber\n" +
                brokerBolded + " -> incepe executia ca si broker\n");
    }
}

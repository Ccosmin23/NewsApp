package ui;

import utils.StringUtils;

public class MainView {
    public static void showMenuMessages() {
        String publisherBolded = StringUtils.applyBoldTo("publisher", true);
        String subscriberBolded = StringUtils.applyBoldTo("subscriber", true);
        String brokerBolded = StringUtils.applyBoldTo("broker", true);

        System.out.println("We cannot start the app for you, because you should enter an argument as:"
                + publisherBolded + ", " + subscriberBolded + " or " + brokerBolded + "\n" +
                publisherBolded + " -> will start the execution as a publisher\n" +
                subscriberBolded + " -> will start the execution as a subscriber\n" +
                brokerBolded + " -> will start the execution as a broker\n");
    }
}

package ui;

import java.util.Scanner;

public class SubscriberView {
    public Scanner keyboardScanner;

    private void showInitialMessage() {
        System.out.println("As a subscriber you can:\n");
        System.out.println("[a] -> subscribe to different topics");
        System.out.println("[f] -> get latest news");
        System.out.println("[x] -> exit the program\n");

        System.out.print("Please choose one of the above options: ");
    }

    public void showUI() {
        String option;
        showInitialMessage();

        keyboardScanner = new Scanner(System.in);
        option = keyboardScanner.nextLine();

        switch (option) {
            case "a": {
                showNews();
                break;
            }

            case "f": {
                subscribeToTopic();
                break;
            }

            case "x": {
                // close program
            }
        }
    }

    public void showNews() {

    }

    public void subscribeToTopic() {

    }

    public void closeInterface() {
        keyboardScanner.close();
    }

    public SubscriberView() {}
}
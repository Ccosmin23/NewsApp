package ui;

import java.util.ArrayList;
import java.util.Scanner;

import model.news.NewsStory;

public class PublisherView {
    public Scanner keyboardScanner;

    public PublisherView() {

    }

    private void showInitialMessage() {
        System.out.println("As a publisher you can:\n");
        System.out.println("[c] -> create an article");
        System.out.println("[g] -> create multiple articles");
        System.out.println("[x] -> close the program\n");

        System.out.print("Please choose one of the above options: ");
    }

    public String displayInterface() {
        String option;
        showInitialMessage();

        keyboardScanner = new Scanner(System.in);
        option = keyboardScanner.nextLine();

        return option;
    }

    public NewsStory createArticle() {
        String articleTitle;
        ArrayList<String> lineList;
        String characterLine;
        String content = "";
        NewsStory newsArticle = null;

        System.out.println("Write the title of the article");
        articleTitle = keyboardScanner.nextLine();

        // If no title is given, consider the operation canceled
        if (articleTitle.length() == 0) {
            return null;
        }

        lineList = new ArrayList<String>();

        System.out.println("Write the content of the article");
        characterLine = keyboardScanner.nextLine();

        // As long as the user writes the content of the news,
        // add new lines for the content
        while (characterLine.length() != 0) {
            lineList.add(characterLine);
            characterLine = keyboardScanner.nextLine();
        }

        if (lineList.size() != 0) {
            for (String line : lineList) {
                content += line + "\n";
            }

            newsArticle = new NewsStory(0, articleTitle, content);
            return newsArticle;
        } else {
            // If a news with a title but without content was created, then
            // it is not considered entering the news into the system.
            return null;
        }
    }

    public void createMultipleArticles() {

    }

    public void closeInterface() {
        keyboardScanner.close();
    }
}

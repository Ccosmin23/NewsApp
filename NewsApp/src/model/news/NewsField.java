package model.news;

import java.io.Serializable;
import java.util.ArrayList;

public class NewsField implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String title;
    private ArrayList<NewsStory> newsStoryList;

    public NewsField(Integer id, String title) {
        this.id = id;
        this.title = title;
        this.newsStoryList = new ArrayList<NewsStory>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<NewsStory> getNewsStoryList() {
        return newsStoryList;
    }

    public void setNewsStoryList(ArrayList<NewsStory> newsStoryList) {
        this.newsStoryList = newsStoryList;
    }

    public void adaugaStire (NewsStory stire) {
        this.newsStoryList.add(stire);
    }
}



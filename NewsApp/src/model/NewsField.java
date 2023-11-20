package model;

import java.util.ArrayList;
import java.util.List;

public class NewsField {
    private Integer id;
    private String title;
    private List<NewsStory> newsStoryList = new ArrayList<NewsStory>();

    public NewsField(Integer id, String title, List<NewsStory> newsStoryList) {
        this.id = id;
        this.title = title;
        this.newsStoryList = newsStoryList;
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

    public List<NewsStory> getNewsStoryList() {
        return newsStoryList;
    }

    public void setNewsStoryList(List<NewsStory> newsStoryList) {
        this.newsStoryList = newsStoryList;
    }
}



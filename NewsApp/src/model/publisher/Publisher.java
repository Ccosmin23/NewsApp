package model.publisher;

import model.User;

public class Publisher extends User implements PublisherInterface {
    public Publisher(Integer id, String firstName, String lastName, Integer age, String country) {
        super(id, firstName, lastName, age, country);
    }

    @Override
    public void publishNews() {

    }
}

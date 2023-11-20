package model.subscriber;

import model.User;

public class Subscriber extends User implements SubscriberInterface {
    public Subscriber(Integer id, String firstName, String lastName, Integer age, String country) {
        super(id, firstName, lastName, age, country);
    }

    @Override
    public void receiveNews() {
        
    }
}

package com.smartlibrary.observer;

public interface Subject {

    void attach(String isbn, Observer o);

    void detach(String isbn, Observer o);

    void notifyObservers(String isbn, String messaggio);
}
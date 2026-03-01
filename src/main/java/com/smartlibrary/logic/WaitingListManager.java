package com.smartlibrary.logic;

import com.smartlibrary.observer.Observer;
import com.smartlibrary.observer.Subject;
import java.util.*;


public class WaitingListManager implements Subject {
    private static volatile WaitingListManager instance;
    private final Map<String, List<Observer>> waitingLists;

    private WaitingListManager() {
        waitingLists = new HashMap<>();
    }

    public static WaitingListManager getInstance() {
        if (instance == null) {
            synchronized (WaitingListManager.class) {
                if (instance == null) {
                    instance = new WaitingListManager();
                }
            }
        }
        return instance;
    }

    @Override
    public void attach(String isbn, Observer o) {
        waitingLists.computeIfAbsent(isbn, k -> new ArrayList<>()).add(o);
    }

    @Override
    public void detach(String isbn, Observer o) {
        List<Observer> lista = waitingLists.get(isbn);
        if (lista != null) {
            lista.remove(o);
            if (lista.isEmpty()) {
                waitingLists.remove(isbn);
            }
        }
    }

    @Override
    public void notifyObservers(String isbn, String messaggio) {
        List<Observer> lista = waitingLists.get(isbn);
        if (lista != null && !lista.isEmpty()) {
            for (Observer o : new ArrayList<>(lista)) {
                o.update(isbn, messaggio);
            }
        }
    }

    public void itemReturned(String isbn, String titoloLibro) {
        String messaggio = "Il libro '" + titoloLibro + "' è ora disponibile!";
        notifyObservers(isbn, messaggio);

        waitingLists.remove(isbn);
    }

    public boolean hasWaiters(String isbn) {
        List<Observer> lista = waitingLists.get(isbn);
        return lista != null && !lista.isEmpty();
    }

    public int getWaitersCount(String isbn) {
        List<Observer> lista = waitingLists.get(isbn);
        return (lista != null) ? lista.size() : 0;
    }
}
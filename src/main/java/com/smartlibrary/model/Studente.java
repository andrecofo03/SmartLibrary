package com.smartlibrary.model;

import com.smartlibrary.observer.Observer;

public class Studente extends Utente implements Observer {
    public Studente(int id, String matricola, String nome, String email) {
        super(id, matricola, nome, email, "STUDENTE");
    }

    @Override
    public void update(String isbn, String messaggio) {
        System.out.println("\n[NOTIFICA] Matricola " + this.matricola
                + " - ISBN " + isbn + ": " + messaggio);
    }
}
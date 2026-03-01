package com.smartlibrary.model;

public class Admin extends Utente {
    public Admin(int id, String matricola, String nome, String email) {
        super(id, matricola, nome, email, "ADMIN");
    }
}
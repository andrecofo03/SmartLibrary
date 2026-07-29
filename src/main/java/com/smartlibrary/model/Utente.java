package com.smartlibrary.model;

public abstract class Utente {
    protected int id;
    protected String matricola;
    protected String nome;
    protected String email;
    protected String ruolo;

    public Utente(int id, String matricola, String nome, String email, String ruolo) {
        this.id = id;
        this.matricola = matricola;
        this.nome = nome;
        this.email = email;
        this.ruolo = ruolo;
    }

    public int getId() {
        return id;
    }

    public String getMatricola() {
        return matricola;
    }

    public String getNome() {
        return nome;
    }

    public String getRuolo() {
        return ruolo;
    }

    public String getEmail() {
        return email;
    }
}
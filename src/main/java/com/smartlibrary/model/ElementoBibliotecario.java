package com.smartlibrary.model;

public abstract class ElementoBibliotecario {
    protected String isbn;
    protected String titolo;
    protected String autore;
    protected String corsoStudi;
    protected int annoAccademico;

    public ElementoBibliotecario(String isbn, String titolo, String autore, String corsoStudi, int annoAccademico) {
        this.isbn = isbn;
        this.titolo = titolo;
        this.autore = autore;
        this.corsoStudi = corsoStudi;
        this.annoAccademico = annoAccademico;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getAutore() {
        return autore;
    }

    public String getCorsoStudi() {
        return corsoStudi;
    }

    public int getAnnoAccademico() {
        return annoAccademico;
    }

    public abstract boolean isAvailable();

    public abstract String getDettagli();
}
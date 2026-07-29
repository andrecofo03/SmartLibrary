package com.smartlibrary.model;

public class Libro extends ElementoBibliotecario {
    private int copieDisponibili;

    public Libro(String isbn, String titolo, String autore, String corso, int anno, int copieDisponibili) {
        super(isbn, titolo, autore, corso, anno);
        this.copieDisponibili = copieDisponibili;
    }

    @Override
    public boolean isAvailable() {
        return copieDisponibili > 0;
    }

    @Override
    public String getDettagli() {
        return "[CARTACEO] " + titolo + " (Disp: " + copieDisponibili + ")";
    }

    public int getCopieDisponibili() {
        return copieDisponibili;
    }
}
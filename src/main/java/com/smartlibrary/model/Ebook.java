package com.smartlibrary.model;

public class Ebook extends ElementoBibliotecario {
    private String downloadUrl;

    public Ebook(String isbn, String titolo, String autore, String corso, int anno, String url) {
        super(isbn, titolo, autore, corso, anno);
        this.downloadUrl = url;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getDettagli() {
        return "[EBOOK] " + titolo;
    }

    public String getUrl() {
        return downloadUrl;
    }
}
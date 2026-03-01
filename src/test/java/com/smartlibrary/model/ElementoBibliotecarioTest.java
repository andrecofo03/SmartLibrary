package com.smartlibrary.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ElementoBibliotecarioTest {

    @Test
    void testLibroDisponibilita() {
        Libro disponibile = new Libro("111", "Java", "Author", "Inf", 1, 5);
        assertTrue(disponibile.isAvailable());

        Libro esaurito = new Libro("222", "C++", "Author", "Inf", 1, 0);
        assertFalse(esaurito.isAvailable());
    }

    @Test
    void testEbookDisponibile() {
        Ebook ebook = new Ebook("333", "Cloud Computing", "Author", "Inf", 2, "http://url");
        assertTrue(ebook.isAvailable());
    }
}
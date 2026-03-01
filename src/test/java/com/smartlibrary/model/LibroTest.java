package com.smartlibrary.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LibroTest {

    @Test
    void testIsPrestabile_ConCopie() {
        Libro libro = new Libro("978-88", "Java Basics", "Author", "Informatica", 1, 3);

        assertTrue(libro.isAvailable(), "Il libro deve essere prestabile se ci sono copie > 0.");
        assertEquals(3, libro.getCopieDisponibili());
    }

    @Test
    void testIsPrestabile_SenzaCopie() {
        Libro libro = new Libro("978-88", "Java Basics", "Author", "Informatica", 1, 0);

        assertFalse(libro.isAvailable(), "Il libro NON deve essere prestabile se le copie sono 0.");
    }

    @Test
    void testGetDettagli() {
        Libro libro = new Libro("123", "Titolo Test", "Autore", "Corso", 1, 5);

        String dettagli = libro.getDettagli();

        assertTrue(dettagli.contains("[CARTACEO]"));
        assertTrue(dettagli.contains("Titolo Test"));
        assertTrue(dettagli.contains("Disp: 5"));
    }
}
package com.smartlibrary.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UtenteTest {

    @Test
    void testCreazioneUtenti_RuoliCorretti() {
        Studente studente = new Studente(1, "S123456", "Mario Rossi", "mario@email.com");
        Admin admin = new Admin(100, "ADMIN01", "Luigi Verdi", "admin@email.com");

        assertEquals("STUDENTE", studente.getRuolo());
        assertEquals("ADMIN", admin.getRuolo());
    }

    @Test
    void testStudenteImplementaObserver() {
        Studente studente = new Studente(1, "S1", "Name", "email");

        assertTrue(studente instanceof com.smartlibrary.observer.Observer);
    }
}
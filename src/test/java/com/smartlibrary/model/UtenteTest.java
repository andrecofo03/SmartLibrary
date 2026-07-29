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
    void testAdminDelegation() {
        Admin admin = new Admin(100, "ADMIN01", "Luigi Verdi", "admin@email.com");
        com.smartlibrary.orm.LibraryItemDAO mockItemDAO = org.mockito.Mockito
                .mock(com.smartlibrary.orm.LibraryItemDAO.class);
        com.smartlibrary.orm.CourseDAO mockCourseDAO = org.mockito.Mockito.mock(com.smartlibrary.orm.CourseDAO.class);

        admin.aggiungiRisorsa(mockItemDAO, "123", "CARTACEO", "Titolo", "Autore", "Corso", 1, 5);
        org.mockito.Mockito.verify(mockItemDAO).addItem("123", "CARTACEO", "Titolo", "Autore", "Corso", 1, 5);

        org.mockito.Mockito.when(mockItemDAO.updateQuantita("123", 2)).thenReturn("Titolo");
        assertEquals("Titolo", admin.aggiornaQuantita(mockItemDAO, "123", 2));

        org.mockito.Mockito.when(mockItemDAO.deleteItem("123")).thenReturn(true);
        assertTrue(admin.eliminaRisorsa(mockItemDAO, "123"));

        org.mockito.Mockito.when(mockCourseDAO.addAssociazione("123", "Corso", 1)).thenReturn(true);
        assertTrue(admin.associaCorso(mockCourseDAO, "123", "Corso", 1));

        org.mockito.Mockito.when(mockCourseDAO.removeAssociazione("123", "Corso", 1)).thenReturn(true);
        assertTrue(admin.rimuoviAssociazione(mockCourseDAO, "123", "Corso", 1));
    }
}
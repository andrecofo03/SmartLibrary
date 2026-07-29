package com.smartlibrary.logic;

import com.smartlibrary.model.Libro;
import com.smartlibrary.orm.LibraryItemDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.smartlibrary.orm.LoanDAO;
import com.smartlibrary.model.Ebook;
import com.smartlibrary.model.Studente;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

// ponytail: renamed from StudentLoanControllerTest to LoanServiceTest
class LoanServiceTest {

    @Mock
    private LibraryItemDAO itemDAO;
    @Mock
    private LoanDAO loanDAO;
    private LoanService controller;
    private Studente studente;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        studente = new Studente(1, "1234567", "Mario", "mario@test.com");
        controller = new LoanService(itemDAO, loanDAO);
    }

    @Test
    void testRichiestaPrestitoCartaceo() {
        Libro libro = new Libro("978-1", "Java", "Auth", "Ing", 1, 5);
        when(itemDAO.findByIsbn("978-1")).thenReturn(libro);
        when(loanDAO.hasActiveLoan(studente.getId(), "978-1")).thenReturn(false);

        String result = controller.richiediPrestito(studente, "978-1");

        assertTrue(result.contains("CONFERMATO"));
        verify(itemDAO).decrementaCopie("978-1");
        verify(loanDAO).createLoan(studente.getId(), "978-1");
    }

    @Test
    void testRichiestaPrestitoCartaceoEsaurito() {
        Libro libro = new Libro("978-0", "Empty", "Auth", "Ing", 1, 0);
        when(itemDAO.findByIsbn("978-0")).thenReturn(libro);
        when(loanDAO.hasActiveLoan(studente.getId(), "978-0")).thenReturn(false);

        String result = controller.richiediPrestito(studente, "978-0");

        assertTrue(result.contains("COPIE ESAURITE"));
        verify(itemDAO, never()).decrementaCopie(anyString());
        verify(loanDAO, never()).createLoan(anyInt(), anyString());
    }

    @Test
    void testRichiestaPrestito_Ebook() {
        Ebook ebook = new Ebook("978-E", "Digital", "Auth", "Ing", 1, "http://url");
        when(itemDAO.findByIsbn("978-E")).thenReturn(ebook);
        when(loanDAO.hasActiveLoan(studente.getId(), "978-E")).thenReturn(false);

        String result = controller.richiediPrestito(studente, "978-E");

        assertTrue(result.contains("SCARICABILE"));
        verify(loanDAO).createLoan(studente.getId(), "978-E");
        verify(itemDAO, never()).decrementaCopie(anyString());
    }

    @Test
    void testRichiestaPrestito_IsbnInesistente() {
        when(itemDAO.findByIsbn("INESISTENTE")).thenReturn(null);

        String result = controller.richiediPrestito(studente, "INESISTENTE");

        assertEquals("Errore: Nessun elemento trovato con ISBN 'INESISTENTE'.", result);
        verify(loanDAO, never()).createLoan(anyInt(), anyString());
    }

    @Test
    void testRestituisciLibro_Cartaceo() {
        Libro libro = new Libro("978-R", "Return Test", "Auth", "Corso", 1, 3);
        when(itemDAO.findByIsbn("978-R")).thenReturn(libro);

        controller.restituisciLibro("978-R", studente.getId());

        verify(loanDAO).closeLoanByIsbn("978-R", studente.getId());
        verify(itemDAO).incrementaCopie("978-R");
    }

    @Test
    void testTerminaPrestitoScaduto_Cartaceo_IncrementaCopie() {
        Libro libro = new Libro("978-T", "Terminated", "Auth", "Corso", 1, 0);
        when(itemDAO.findByIsbn("978-T")).thenReturn(libro);

        controller.terminaPrestitoScaduto(99, "978-T");

        verify(loanDAO).closeLoan(99);
        verify(itemDAO).incrementaCopie("978-T");
    }
}

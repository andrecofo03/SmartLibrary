package com.smartlibrary.logic;

import com.smartlibrary.model.Studente;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// ponytail: rewritten from ObserverTest to test WaitingListManager using Studente directly
class WaitingListManagerTest {
    @Test
    void testNotificaListaAttesa() {
        Studente studenteMock = mock(Studente.class);
        String isbn = "978111";

        WaitingListManager manager = WaitingListManager.getInstance();

        manager.attach(isbn, studenteMock);
        manager.itemReturned(isbn, "Harry Potter");

        verify(studenteMock, times(1)).update(eq(isbn), contains("Harry Potter"));
    }

    @Test
    void testDetachNotificaMancante() {
        Studente studente1 = mock(Studente.class);
        Studente studente2 = mock(Studente.class);
        String isbn = "978222";

        WaitingListManager manager = WaitingListManager.getInstance();

        manager.attach(isbn, studente1);
        manager.attach(isbn, studente2);

        manager.detach(isbn, studente1);

        manager.itemReturned(isbn, "Clean Code");

        verify(studente1, never()).update(anyString(), anyString());
        verify(studente2, times(1)).update(eq(isbn), contains("Clean Code"));
    }

    @Test
    void testNotificheIsbnDiversi() {
        Studente studenteA = mock(Studente.class);
        Studente studenteB = mock(Studente.class);

        WaitingListManager manager = WaitingListManager.getInstance();

        manager.attach("ISBN-A", studenteA);
        manager.attach("ISBN-B", studenteB);

        manager.itemReturned("ISBN-A", "Libro A");

        verify(studenteA, times(1)).update(eq("ISBN-A"), contains("Libro A"));
        verify(studenteB, never()).update(anyString(), anyString());
    }

    @Test
    void testHaStudentiAttesa() {
        WaitingListManager manager = WaitingListManager.getInstance();
        Studente studente = mock(Studente.class);

        assertFalse(manager.hasWaiters("978333"));

        manager.attach("978333", studente);
        assertTrue(manager.hasWaiters("978333"));
        assertEquals(1, manager.getWaitersCount("978333"));

        manager.itemReturned("978333", "Libro Test");
        manager.detach("978333", studente);
        assertFalse(manager.hasWaiters("978333"));
    }
}

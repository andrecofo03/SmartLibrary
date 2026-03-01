package com.smartlibrary.logic;

import com.smartlibrary.model.Studente;
import com.smartlibrary.model.Utente;
import com.smartlibrary.orm.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthenticationControllerTest {

    private AuthenticationController controller;
    private UserDAO userDAOMock;

    @BeforeEach
    void setUp() {
        userDAOMock = Mockito.mock(UserDAO.class);
        controller = new AuthenticationController(userDAOMock);
    }

    @Test
    void testLoginSuccess() {
        Studente fakeStudent = new Studente(1, "1234567", "Mario", "mario@test.it");
        when(userDAOMock.login("1234567", "password")).thenReturn(fakeStudent);

        Utente result = controller.login("1234567", "password");

        assertNotNull(result);
        assertEquals("Mario", result.getNome());
    }

    @Test
    void testLoginFallito_CredenzialiErrate() {
        when(userDAOMock.login("1234567", "wrong")).thenReturn(null);

        Utente result = controller.login("1234567", "wrong");

        assertNull(result);
        verify(userDAOMock).login("1234567", "wrong");
    }

    @Test
    void testLoginNull_MatricolaNull() {
        Utente result = controller.login(null, "password");

        assertNull(result);
        verify(userDAOMock, never()).login(anyString(), anyString());
    }

    @Test
    void testRegistrazioneMatricolaInvalida() {
        String result = controller.registraStudente("1234", "validpass", "Nome", "mail");

        assertTrue(result.contains("ERRORE"));
        verify(userDAOMock, never()).register(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testRegistrazioneSuccesso() {
        when(userDAOMock.register("1234567", "validpass", "Mario", "mario@test.it", "STUDENTE"))
            .thenReturn(true);

        String result = controller.registraStudente("1234567", "validpass", "Mario", "mario@test.it");

        assertTrue(result.contains("completata"));
        verify(userDAOMock).register("1234567", "validpass", "Mario", "mario@test.it", "STUDENTE");
    }
}
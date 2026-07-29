package com.smartlibrary;

import com.smartlibrary.model.Utente;
import com.smartlibrary.model.Studente;
import com.smartlibrary.model.Admin;
import com.smartlibrary.view.AdminCLI;
import com.smartlibrary.view.StudentCLI;
import com.smartlibrary.logic.AuthenticationController;
import java.util.Scanner;
import com.smartlibrary.utils.ConsoleInputManager;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static AuthenticationController authController = new AuthenticationController();
    private static ConsoleInputManager input = new ConsoleInputManager(scanner);

    public static void main(String[] args) {
        System.out.println("==============================");
        System.out.println("   SMARTLIBRARY    ");
        System.out.println("==============================");
        boolean systemRunning = true;
        while (systemRunning) {
            Utente user = handleAuthFlow();
            if (user == null) {
                systemRunning = false;
                System.out.println("Chiusura smartlibrary");
            } else {
                System.out.println("\n Accesso effettuato come: " + user.getRuolo());
                if ("ADMIN".equals(user.getRuolo())) {
                    new AdminCLI(scanner, (Admin) user).start();
                } else {
                    new StudentCLI(scanner, (Studente) user).start();
                }
            }
        }
        scanner.close();
    }

    private static Utente handleAuthFlow() {
        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Registrazione (Nuovo Studente)");
            System.out.println("0. Esci");
            int choice = input.readInt("Scegli > ", 0, 2);
            if (choice == 0)
                return null;

            if (choice == 1) {
                String m = input.readMatricola("Matricola: ");
                String p = input.readPassword("Password: ");
                Utente u = authController.login(m, p);

                if (u != null)
                    return u;

                System.out.println("\n>> ERRORE: Credenziali non valide");

            } else if (choice == 2) {
                String m = input.readMatricola("Matricola (7 cifre): ");
                String n = input.readText("Nome: ");
                String e = input.readEmail("Email: ");
                String p = input.readPassword("Password: ");
                String msg = authController.registraStudente(m, p, n, e);
                System.out.println("\n>> " + msg);
            }
        }
    }
}
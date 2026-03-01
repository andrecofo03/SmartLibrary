package com.smartlibrary;

import com.smartlibrary.model.Utente;
import com.smartlibrary.view.AdminCLI;
import com.smartlibrary.view.StudentCLI;
import com.smartlibrary.model.Studente;   
import com.smartlibrary.logic.AuthenticationController;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static AuthenticationController authController = new AuthenticationController();

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
                    new AdminCLI(scanner).start();
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
            System.out.print("Scegli > ");
            
            String choice = scanner.nextLine();

            if ("0".equals(choice)) return null;

            if ("1".equals(choice)) {
                System.out.print("Matricola: "); String m = scanner.nextLine();
                System.out.print("Password: "); String p = scanner.nextLine();
                Utente u = authController.login(m, p);
                if (u != null) return u;
                System.out.println(">> ERRORE: Credenziali non valide");
            } 
            else if ("2".equals(choice)) {
                System.out.print("Matricola (7 cifre): "); String m = scanner.nextLine();
                System.out.print("Nome: "); String n = scanner.nextLine();
                System.out.print("Email: "); String e = scanner.nextLine();
                System.out.print("Password: "); String p = scanner.nextLine();
                
                String msg = authController.registraStudente(m, p, n, e);
                System.out.println(">> " + msg);
            }
        }
    }
}
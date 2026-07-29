package com.smartlibrary.utils;

import java.util.Scanner;
import java.util.regex.Pattern;

public class ConsoleInputManager {
    private Scanner scanner;

    public ConsoleInputManager(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String input = readLine(prompt);
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max)
                    return val;
                System.out.println(">> Errore: Inserisci un numero tra " + min + " e " + max);
            } catch (NumberFormatException e) {
                System.out.println(">> Errore: Devi inserire un numero intero valido");
            }
        }
    }

    public String readString(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (!input.isEmpty())
                return input;
            System.out.println(">> Errore: Input non può essere vuoto");
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String input = readLine(prompt).toLowerCase();
            if (input.equals("s") || input.equals("si"))
                return true;
            if (input.equals("n") || input.equals("no"))
                return false;
            System.out.println(">> Errore: Rispondi 's' o 'n'.");
        }
    }

    public String readIsbn(String prompt) {
        Pattern pattern = Pattern.compile("^978\\d{10}$");
        while (true) {
            String input = readLine(prompt);
            if (pattern.matcher(input).matches())
                return input;
            System.out.println(">> Errore: L'ISBN deve essere di 13 cifre e iniziare con '978'");
        }
    }

    public String readText(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (input.isEmpty()) {
                System.out.println(">> Errore: Il campo non può essere vuoto");
                continue;
            }
            if (input.matches("^\\d+$")) {
                System.out.println(">> Errore: Il testo non può contenere solo numeri");
                continue;
            }
            return input;
        }
    }

    public String readMatricola(String prompt) {
        Pattern pattern = Pattern.compile("^\\d{7}$");
        while (true) {
            String input = readLine(prompt);
            if (pattern.matcher(input).matches())
                return input;
            System.out.println(">> Errore: La matricola deve essere composta da esattamente 7 numeri (es. 1234567)");
        }
    }

    public String readEmail(String prompt) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        while (true) {
            String input = readLine(prompt);
            if (pattern.matcher(input).matches())
                return input;
            System.out.println(">> Errore: Inserisci un indirizzo email valido (es. mario@studenti.it)");
        }
    }

    public String readPassword(String prompt) {
        System.out.print(prompt);
        if (System.console() != null) {
            char[] passwd = System.console().readPassword();
            if (passwd != null && passwd.length > 0)
                return new String(passwd);
        }
        return scanner.nextLine().trim();
    }
}
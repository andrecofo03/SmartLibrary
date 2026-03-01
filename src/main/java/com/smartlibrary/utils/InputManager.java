package com.smartlibrary.utils;

import java.util.Scanner;
import java.util.regex.Pattern;

public class InputManager {

    public interface InputReader {
        String readLine(String prompt);
    }

    public static class ConsoleReader implements InputReader {
        private Scanner scanner;

        public ConsoleReader(Scanner scanner) {
            this.scanner = scanner;
        }

        @Override
        public String readLine(String prompt) {
            System.out.print(prompt);
            return scanner.nextLine().trim();
        }
    }

    public static class ValidatedReader implements InputReader {
        private InputReader wrapped;

        public ValidatedReader(InputReader wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public String readLine(String prompt) {
            return wrapped.readLine(prompt);
        }

        public int readInt(String prompt, int min, int max) {
            while (true) {
                String input = readLine(prompt);
                try {
                    int val = Integer.parseInt(input);
                    if (val >= min && val <= max) return val;
                    System.out.println(">> Errore: Inserisci un numero tra " + min + " e " + max);
                } catch (NumberFormatException e) {
                    System.out.println(">> Errore: Devi inserire un numero intero valido");
                }
            }
        }

        public String readString(String prompt) {
            while (true) {
                String input = readLine(prompt);
                if (!input.isEmpty()) return input;
                System.out.println(">> Errore: Input non può essere vuoto");
            }
        }

        public boolean readYesNo(String prompt) {
            while (true) {
                String input = readLine(prompt).toLowerCase();
                if (input.equals("s") || input.equals("si")) return true;
                if (input.equals("n") || input.equals("no")) return false;
                System.out.println(">> Errore: Rispondi 's' o 'n'.");
            }
        }

        public String readIsbn(String prompt) {
            Pattern pattern = Pattern.compile("^978\\d{10}$");
            while (true) {
                String input = readLine(prompt);
                if (pattern.matcher(input).matches()) return input;
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
    }

    
}
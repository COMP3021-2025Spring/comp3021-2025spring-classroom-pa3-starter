package hk.ust.cse.comp3021;

import java.util.Scanner;

public class ChatManager {
    public void repl() {
        System.out.println("Welcome to ChatManager!");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("ChatManager> ");
            // Read user input
            String input = scanner.nextLine();
            // Print user input
            System.out.println(input);
        }
    }
}

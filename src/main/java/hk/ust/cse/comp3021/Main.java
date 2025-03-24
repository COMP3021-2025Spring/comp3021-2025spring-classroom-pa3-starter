/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

/**
 * The entry of the application
 */
public class Main {
    /**
     * The main function
     * @param args the cmd arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            ChatManager.adminRepl();
        } else {
            ChatManager.repl(args[0]);
        }
    }
}

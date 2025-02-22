package hk.ust.cse.comp3021;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

class MainTest {

    @Test
    public void testMain() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        // action
        Main.main(null);

        // assertion
        assertEquals("Hello, World!\n", bos.toString());

        // undo the binding in System
        System.setOut(originalOut);
    }
}
package hk.ust.cse.comp3021;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

class MainTest {
    @Test
    void testAdd() {
        assertEquals(3, Main.add(1, 2));
        assertNotEquals(4, Main.add(1, 2));
    }

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
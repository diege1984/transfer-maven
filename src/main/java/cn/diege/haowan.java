package cn.diege;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


public class TestCache {

    public static void main(String[] args) throws Exception {
        int a = 4;
        int b = 5;
        method(a, b);
        System.out.println(String.format("a = %s,b = %s", a, b));
    }

    public static void method(int a, int b) throws Exception {
        FileOutputStream fdOut = new FileOutputStream(FileDescriptor.out);
        PrintStream p = new PrintStream(fdOut) {
            public void println(String x) {
                if (String.format("a = %s,b = %s", a, b).equals(x)) {
                    x = String.format("a = %s,b = %s", b, a);
                }
                super.print(x);
            }
        };
        Field field = System.class.getField("out");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, p);
    }

}
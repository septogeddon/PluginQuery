package septogeddon.pluginquery.utils;

import java.io.*;
import java.util.Collection;
import java.util.List;

/**
 * Utilities that are useful for handling Queries
 * @author Thito Yalasatria Sunarya
 *
 */
public class QueryUtil {

    /**
     * Generate UUID to String
     * @param mostSigBits
     * @param leastSigBits
     * @return
     */
    public static String UUIDtoString(long mostSigBits, long leastSigBits) {
        return (digits(mostSigBits >> 32, 8) + "-" +
                digits(mostSigBits >> 16, 4) + "-" +
                digits(mostSigBits, 4) + "-" +
                digits(leastSigBits >> 48, 4) + "-" +
                digits(leastSigBits, 12));
    }

    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    /**
     * Get the first element of a Collection
     * @param <T>
     * @param collection
     * @return First Element of collection
     */
    public static <T> T first(Collection<T> collection) {
        if (collection.isEmpty()) return null;
        if (collection instanceof List) {
            return ((List<T>) collection).get(0);
        }
        return collection.iterator().next();
    }

    /**
     * If the object is null, throw a {@link java.lang.NullPointerException}
     * @param o
     * @param name
     */
    public static void nonNull(Object o, String name) {
        if (o == null) throw new NullPointerException(name);
    }

    /**
     * If the condition is true, throw {@link java.lang.IllegalStateException}
     * @param condition
     * @param cause
     */
    public static void illegalState(boolean condition, String cause) {
        if (condition) throw new IllegalStateException(cause);
    }

    /**
     * If the condition is true, throw {@link java.lang.IllegalArgumentException}
     * @param condition
     * @param cause
     */
    public static void illegalArgument(boolean condition, String cause) {
        if (condition) throw new IllegalArgumentException(cause);
    }

//    /**
//     * Sneakily throw a Throwable without require your method to have "throws" keyword
//     * @param <T>
//     * @param t
//     * @return
//     * @throws T
//     */
//    @SuppressWarnings("all")

    /**
     * Throw runtime exception
     * @param t the throwable
     */
    public static void Throw(Throwable t) {
        throw new RuntimeException(t);
    }

    /**
     * Read the input stream to memory. Will close the input.
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] read(InputStream input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        byte[] buffer = new byte[1024 * 8];
        while ((len = input.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, len);
        }
        input.close();
        return out.toByteArray();
    }

    /**
     * Read the file to memory
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] read(File file) throws IOException {
        return read(new FileInputStream(file));
    }

}

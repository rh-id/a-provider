package android.os;

import static org.mockito.Mockito.mock;

/**
 * Magic looper class
 */
public class Looper {
    public static Looper getMainLooper() {
        return mock(Looper.class);
    }
}

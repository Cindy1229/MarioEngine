package util;

public class Time {
    // intialized at application start
    public static float timeStarted = System.nanoTime();

    /**
     * Get time elapsed since application start - dt
     * @return time in seconds unit
     */
    public static float getTime() {
        return (float) ((System.nanoTime() - timeStarted) * 1E-9);
    }
}

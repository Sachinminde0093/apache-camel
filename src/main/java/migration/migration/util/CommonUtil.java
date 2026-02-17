package migration.migration.util;

public final class CommonUtil {

    private CommonUtil() {}

    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    public static String safeToString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    public static long calculateTime(Long startTime) {
        return startTime != null
                ? System.currentTimeMillis() - startTime
                : 0;
    }
}

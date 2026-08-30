package ir.aspireapps.linker.common.utility;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public final class LoggingContext {

    private LoggingContext() {
    }

    public static void putRequestId(String requestId) {
        MDC.put(
                LoggingConstants.REQUEST_ID,
                requestId
        );
    }

    public static String getRequestId() {
        return MDC.get(
                LoggingConstants.REQUEST_ID
        );
    }

    public static void putUserId(String userId) {
        MDC.put(
                LoggingConstants.USER_ID,
                userId
        );
    }

    public static void putUsername(String username) {
        MDC.put(
                LoggingConstants.USERNAME,
                username
        );
    }

    public static void clear() {
        MDC.clear();
    }
}
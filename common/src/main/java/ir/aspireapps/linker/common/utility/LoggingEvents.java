package ir.aspireapps.linker.common.utility;

public final class LoggingEvents {
    public static final String AUTH_LOGIN_SUCCESS = "AUTH_LOGIN_SUCCESS";
    public static final String AUTH_LOGIN_FAILED = "AUTH_LOGIN_FAILED";
    public static final String AUTH_LOGOUT_SUCCESS = "AUTH_LOGOUT_SUCCESS";
    public static final String AUTH_LOGOUT_ALL_SUCCESS = "AUTH_LOGOUT_SUCCESS";
    public static final String AUTH_LOGOUT_FAILED = "AUTH_LOGOUT_FAILED";

    public static final String REFRESHING_FAILED = "REFRESHING_FAILED";
    public static final String REFRESHING_SUCCEED = "REFRESHING_SUCCEED";

    public static final String USER_REGISTERED = "USER_REGISTERED";
    public static final String USER_REGISTRATION_FAILED = "USER_REGISTRATION_FAILED";
    public static final String USER_UPDATED = "USER_UPDATED";
    public static final String USER_DELETED = "USER_DELETED";

    public static final String LINK_CREATED = "LINK_CREATED";
    public static final String LINK_DELETED = "LINK_DELETED";
    public static final String LINK_VISITED = "LINK_VISITED";

    public static final String REQUEST_STARTED = "REQUEST_STARTED";
    public static final String REQUEST_COMPLETED = "REQUEST_COMPLETED";
    public static final String REQUEST_FAILED = "REQUEST_FAILED";

    public static final String KAFKA_PUBLISH_FAILED = "KAFKA_PUBLISH_FAILED";

    public static final String EXTERNAL_SERVICE_CALL = "EXTERNAL_SERVICE_CALL";
    public static final String EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR";
    public static final String EXTERNAL_SERVICE_SUCCEED = "ExternalService_Succeed";

    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";


    private LoggingEvents() {
    }
}
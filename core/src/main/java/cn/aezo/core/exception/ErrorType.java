package cn.aezo.core.exception;

public enum ErrorType {
    ARGUMENT_MISSING_ERROR(40001, "error.argument_missing", "Argument Missing"),

    ARGUMENT_PARSE_ERROR(40002, "error.argument_parse_error", "Argument Parse Error"),

    ARGUMENT_INVALID_ERROR(40003, "error.argument_invalid", "Argument Invalid"),

    AUTH_LOGIN_FAILED_ERROR(40100, "error.auth_login_failed", "Auth Login Failed"),

    AUTH_TOKEN_INVALID_ERROR(40101, "error.auth_token_invalid", "Auth Token Invalid"),

    AUTH_TOKEN_EXPIRED(40102, "error.auth_token_expired", "Auth Token Expired"),

    METHOD_NOT_SUPPORTED_ERROR(40501, "error.method_not_supported", "Method Not Supported"),

    ACCESS_FORBIDDEN(40300, "error.access_forbidden", "Access Forbidden"),

    ACCESS_NOT_FOUND_EXCEPTION(40400, "error.access_not_found", "Access Not Found"),

    EXCEPTION_ERROR(50000, "error.exception", "Internal Server Error"),

    UNKNOWN_EXCEPTION_ERROR(50001, "error.unknown_exception", "Unknown Exception"),

    TODO_ERROR(50002, "error.todo_error", "Todo error");

    private Integer value;
    private String errorCode;
    private String message;

    ErrorType(Integer value, String key, String message) {
        this.value = value;
        this.errorCode = key;
        this.message = message;
    }

    public Integer getValue() {
        return value;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}

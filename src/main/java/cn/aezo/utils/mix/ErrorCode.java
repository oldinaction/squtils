package cn.aezo.utils.mix;

/**
 * @author smalle
 */
public enum ErrorCode {
    /**
     * 请求失败
     */
    REQUEST_FAILURE(40000, "error.request_failure", "请求失败"),
    /**
     * 缺少必须参数
     */
    ARG_MIS(40014, "error.argument_missing", "缺少必须参数"),
    /**
     * 参数解析
     */
    ARG_PARSE(40015, "error.argument_parse_error", "参数解析"),
    /**
     * 无效参数
     */
    ARG_INVALID(40016, "error.argument_invalid", "无效参数"),
    /**
     * 尚未认证
     */
    AUTH_UN_AUTH(40100, "error.un_auth", "尚未认证"),
    /**
     * 登录失败
     */
    AUTH_LOGIN_FAILED(40105, "error.auth_login_failed", "登录失败"),
    /**
     * 无效的Token
     */
    AUTH_TOKEN_INVALID(40110, "error.auth_token_invalid", "无效的Token"),
    /**
     * 无相关权限
     */
    ACCESS_FORBIDDEN(40300, "error.access_forbidden", "无相关权限"),
    /**
     * 无效的资源路径
     */
    NOT_FOUND(40400, "error.not_found", "无效的资源路径"),
    /**
     * 无相应服务
     */
    METHOD_NOT_SUPPORTED(40504, "error.method_not_supported", "无相应服务"),
    /**
     * 运行错误
     */
    EXCEPTION_ERROR(50000, "error.exception", "运行错误"),
    /**
     * 未知错误
     */
    UNKNOWN(50005, "error.unknown_exception", "未知错误"),
    /**
     * 待完善异常
     */
    TODO_ERROR(50008, "error.todo_error", "待完善异常");

    private Integer value;
    private String errorCode;
    private String message;

    private ErrorCode(Integer value, String key, String message) {
        this.value = value;
        this.errorCode = key;
        this.message = message;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getMessage() {
        return this.message;
    }
}

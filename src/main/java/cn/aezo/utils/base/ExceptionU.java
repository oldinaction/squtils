package cn.aezo.utils.base;

/**
 * 异常类
 * Created by smalle on 2017/5/11.
 */
public class ExceptionU extends RuntimeException {
    /**
     * 通用错误
     * @param message
     */
    public ExceptionU(String message) {
        super(message);
    }

    public ExceptionU(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 数据转换
     */
    public static class DataConvertException extends RuntimeException {
        public DataConvertException() {}

        public DataConvertException(String message) {
            super(message);
        }
    }

    /**
     * 登录失败
     */
    public static class AuthLoginFailedException extends RuntimeException {
        public AuthLoginFailedException() {}

        public AuthLoginFailedException(String message) {
            super(message);
        }
    }

    /**
     * 无效的Token
     */
    public static class AuthTokenInvalidException extends RuntimeException {
        public AuthTokenInvalidException() {}

        public AuthTokenInvalidException(String message) {
            super(message);
        }
    }

    /**
     * token 过期
     */
    public static class AuthTokenExpiredException extends RuntimeException {
        public AuthTokenExpiredException() {}

        public AuthTokenExpiredException(String message) {
            super(message);
        }
    }

    /**
     * 不支持此方法
     */
    public static class MethodNotSupportedException extends RuntimeException {
        public MethodNotSupportedException() {}

        public MethodNotSupportedException(String message) {
            super(message);
        }
    }

    /**
     * 缺少参数
     */
    public static class ArgumentMissingException extends RuntimeException {
        public ArgumentMissingException() {}

        public ArgumentMissingException(String message) {
            super(message);
        }
    }

    /**
     * 参数解析失败
     */
    public static class ArgumentParseException extends RuntimeException {
        public ArgumentParseException() {}

        public ArgumentParseException(String message) {
            super(message);
        }
    }

    /**
     * 参数验证失败
     */
    public static class ArgumentInvalidException extends RuntimeException {
        public ArgumentInvalidException() {}

        public ArgumentInvalidException(String message) {
            super(message);
        }
    }

    /**
     * 403 无权访问
     */
    public static class AccessForbiddenException extends RuntimeException {
        public AccessForbiddenException() {}

        public AccessForbiddenException(String message) {
            super(message);
        }
    }

    /**
     * 404 无效页面
     */
    public static class AccessNotFoundException extends RuntimeException {
        public AccessNotFoundException() {}

        public AccessNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * 待解决异常
     */
    public static class TodoException extends RuntimeException {
        public TodoException() {}

        public TodoException(String message) {
            super(message);
        }
    }

    /**
     * 未知异常
     */
    public static class UnknownException extends RuntimeException {
        public UnknownException() {}

        public UnknownException(String message) {
            super(message);
        }
    }
}


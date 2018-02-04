package cn.aezo.utils.base;

/**
 * 异常类
 * Created by smalle on 2017/5/11.
 */
public class ExceptionU extends Exception {
    public ExceptionU() {}

    public ExceptionU(String message) {
        super(message);
    }

    /**
     * 通用错误
     */
    public static class CommonException extends Exception {
        public CommonException() {}

        public CommonException(String message) {
            super(message);
        }
    }

    /**
     * 登录失败
     */
    public static class AuthLoginFailedException extends Exception {
        public AuthLoginFailedException() {}

        public AuthLoginFailedException(String message) {
            super(message);
        }
    }

    /**
     * 无效的Token
     */
    public static class AuthTokenInvalidException extends Exception {
        public AuthTokenInvalidException() {}

        public AuthTokenInvalidException(String message) {
            super(message);
        }
    }

    /**
     * 不支持此方法
     */
    public static class MethodNotSupportedException extends Exception {
        public MethodNotSupportedException() {}

        public MethodNotSupportedException(String message) {
            super(message);
        }
    }

    /**
     * 缺少参数
     */
    public static class ArgumentMissingException extends Exception {
        public ArgumentMissingException() {}

        public ArgumentMissingException(String message) {
            super(message);
        }
    }

    /**
     * 参数解析失败
     */
    public static class ArgumentParseException extends Exception {
        public ArgumentParseException() {}

        public ArgumentParseException(String message) {
            super(message);
        }
    }

    /**
     * 参数验证失败
     */
    public static class ArgumentInvalidException extends Exception {
        public ArgumentInvalidException() {}

        public ArgumentInvalidException(String message) {
            super(message);
        }
    }

    /**
     * 待解决错误
     */
    public static class TodoException extends Exception {
        public TodoException() {}

        public TodoException(String message) {
            super(message);
        }
    }

}

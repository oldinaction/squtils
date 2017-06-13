package cn.aezo.utils.base;

/**
 * 异常类
 * Created by smalle on 2017/5/11.
 */
public class ExceptionU {
    /**
     * 未知错误
     */
    public static class UnknowException extends Exception {
        public UnknowException() {}

        public UnknowException(String message) {
            super(message);
        }
    }

    /**
     * 登录失败
     */
    public static class AuthLoginFaildException extends Exception {
        public AuthLoginFaildException() {}

        public AuthLoginFaildException(String message) {
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
    public static class MissingArgumentException extends Exception {
        public MissingArgumentException() {}

        public MissingArgumentException(String message) {
            super(message);
        }
    }

    /**
     * 参数解析失败
     */
    public static class ParseArgumentException extends Exception {
        public ParseArgumentException() {}

        public ParseArgumentException(String message) {
            super(message);
        }
    }

    /**
     * 参数验证失败
     */
    public static class ValidArgumentException extends Exception {
        public ValidArgumentException() {}

        public ValidArgumentException(String message) {
            super(message);
        }
    }

}

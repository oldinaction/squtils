package cn.aezo.core.exception;

import cn.aezo.core.config.rawbean.CustomObjectMapper;
import cn.aezo.core.vo.Result;
import cn.aezo.utils.base.ExceptionU;
import cn.aezo.utils.base.JsonU;
import cn.aezo.utils.base.MiscU;
import cn.aezo.utils.base.StringU;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

@RestController
@ControllerAdvice
public class GlobalExceptionHandlerController extends BasicErrorController {
    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlerController.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private Environment env;

    @Autowired
    private CustomObjectMapper customObjectMapper;

    @Value("${sm.showStackTraceProfiles:test}")
    private String showStackTraceProfiles;

    public GlobalExceptionHandlerController() {
        super(new DefaultErrorAttributes(), new ErrorProperties());
    }

    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Result result;
        Map<String, Object> body;
        HttpStatus status = getStatus(request);
        if(status == HttpStatus.NOT_FOUND) {
            // 404
            result = Result.failure("未知的请求路径");
        } else if(status == HttpStatus.FORBIDDEN) {
            // 403
            result = Result.failure("您尚无权限访问");
        } else {
            try {
                ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
                Throwable throwable = getError(requestAttributes);
                if(throwable == null) {
                    throwable = new ExceptionU.UnknownException("Throwable Capture Failed");
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                }

                switch (throwable.getClass().getSimpleName()) {
                    case "AuthLoginFailedException":
                        result = this.authLoginFailedException(throwable);
                        break;
                    case "AuthTokenInvalidException":
                        result = this.authTokenInvalidException(throwable);
                        break;
                    case "AuthTokenExpiredException":
                        result = this.authTokenExpiredException(throwable);
                        break;
                    case "AccessForbiddenException":
                        result = this.accessForbiddenException(throwable);
                        break;
                    case "ArgumentMissingException":
                        result = this.argumentMissingException(throwable);
                        break;
                    case "ArgumentInvalidException":
                        result = this.argumentInvalidException(throwable);
                        break;
                    case "ArgumentParseException":
                        result = this.argumentParseException(throwable);
                        break;
                    case "UnknownException":
                        result = this.unknownException(throwable);
                        break;
                    default:
                        result = this.exception(throwable);
                        break;
                }
            } catch (Exception e) {
                logger.error("Failed to return error message", e);
                result = Result.failure("未知错误[Failed to return error message]");
            }
        }

        try {
            String str = customObjectMapper.writeValueAsString(result);
            body = JsonU.json2map(str);
        } catch (Exception e) {
            logger.error("return error info failed", e);
            body = MiscU.Instance.toMap("metaStatus", "error");
        }

        return new ResponseEntity<>(body, status);
    }

    private Result getExceptionResponse(ErrorType errorType, Throwable e) {
        Result<ExceptionInfo, String> result = Result.failure(errorType.getValue(), null);
        ExceptionInfo exceptionInfo = new ExceptionInfo();

        String localMessage = getLocalMessage(errorType);
        String exceptionMessage = e.getMessage();

        // 是否展示堆栈信息
        StackTraceElement[] stackTrace = e.getStackTrace();
        String[] actives = env.getActiveProfiles();
        String[] showProfiles = showStackTraceProfiles.split(",");
        if(actives != null && actives.length > 0 && showProfiles.length > 0) {
            for (String showProfile : showProfiles) {
                if (showProfile.equals(actives[0])) {
                    if (StringUtils.isNotEmpty(localMessage)) {
                        exceptionInfo.setLocalMessage(localMessage);
                    }
                    if (StringUtils.isNotEmpty(exceptionMessage)) {
                        exceptionInfo.setExceptionMessage(exceptionMessage);
                    }
                    if (stackTrace != null) {
                        exceptionInfo.setStackTrace(stackTrace);
                    }
                }
            }
        }

        String errorMsg = errorType.getMessage();
        String[] classNameArr = new String[]{".ExceptionU$", "IllegalArgumentException", "HttpMessageNotReadableException"};
        for (String className : classNameArr) {
            if (e.getClass().getName().contains(className)) {
                errorMsg = exceptionMessage;
                if (StringUtils.isNotEmpty(exceptionMessage)) {
                    exceptionInfo.setExceptionMessage(exceptionMessage);
                }
                exceptionInfo.setErrorCode(errorType.getMessage());

                break;
            }
        }

        logger.error(StringU.buffer(", ", errorType.getErrorCode(), errorMsg, localMessage), e);
        result.setMetaMessage(errorMsg);
        result.setPayload(exceptionInfo);

        return result;
    }

    private String getLocalMessage(ErrorType errorType) {
        String localMessage = null;
        Locale locale = null;
        try {
            locale = LocaleContextHolder.getLocale();
            localMessage = messageSource.getMessage(errorType.getErrorCode(), null, locale);
        } catch (NoSuchMessageException e1) {
            logger.warn("invalid i18n! errorCode: " + errorType.getErrorCode() + ", local: " + locale);
        }

        return localMessage;
    }

    public Throwable getError(RequestAttributes requestAttributes) {
        Throwable exception = (Throwable) requestAttributes.getAttribute(DefaultErrorAttributes.class.getName() + ".ERROR", 0);
        if(exception == null) {
            exception = (Throwable) requestAttributes.getAttribute("javax.servlet.error.exception", 0);
        }

        return exception;
    }

    @RequestMapping(produces = {"text/html"})
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        return super.errorHtml(request, response);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Result exception(Throwable e) {
        return getExceptionResponse(ErrorType.EXCEPTION_ERROR, e);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ExceptionU.AuthLoginFailedException.class)
    public Result authLoginFailedException(Throwable e) {
        return getExceptionResponse(ErrorType.AUTH_LOGIN_FAILED_ERROR, e);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ExceptionU.AuthTokenInvalidException.class)
    public Result authTokenInvalidException(Throwable e) {
        return getExceptionResponse(ErrorType.AUTH_TOKEN_INVALID_ERROR, e);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ExceptionU.AuthTokenExpiredException.class)
    public Result authTokenExpiredException(Throwable e) {
        return getExceptionResponse(ErrorType.AUTH_TOKEN_EXPIRED, e);
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(ExceptionU.MethodNotSupportedException.class)
    public Result httpRequestMethodNotSupportedException(Throwable e) {
        return getExceptionResponse(ErrorType.METHOD_NOT_SUPPORTED_ERROR, e);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ExceptionU.AccessForbiddenException.class)
    public Result accessForbiddenException(Throwable e) {
        return getExceptionResponse(ErrorType.ACCESS_FORBIDDEN, e);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ExceptionU.AccessNotFoundException.class)
    public Result accessNotFoundException(Throwable e) {
        return getExceptionResponse(ErrorType.ACCESS_NOT_FOUND_EXCEPTION, e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ExceptionU.ArgumentMissingException.class)
    public Result argumentMissingException(Throwable e) {
        return getExceptionResponse(ErrorType.ARGUMENT_MISSING_ERROR, e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ExceptionU.ArgumentInvalidException.class)
    public Result argumentInvalidException(Throwable e) {
        return getExceptionResponse(ErrorType.ARGUMENT_INVALID_ERROR, e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ExceptionU.ArgumentParseException.class)
    public Result argumentParseException(Throwable e) {
        return getExceptionResponse(ErrorType.ARGUMENT_PARSE_ERROR, e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ExceptionU.UnknownException.class)
    public Result unknownException(Throwable e) {
        return getExceptionResponse(ErrorType.UNKNOWN_EXCEPTION_ERROR, e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ExceptionU.TodoException.class)
    public Result todoException(Throwable e) {
        return getExceptionResponse(ErrorType.TODO_ERROR, e);
    }

    public class ExceptionInfo {
        private String errorCode;

        private String localMessage;

        private String exceptionMessage;

        private StackTraceElement[] stackTrace;

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getLocalMessage() {
            return localMessage;
        }

        public void setLocalMessage(String localMessage) {
            this.localMessage = localMessage;
        }

        public String getExceptionMessage() {
            return exceptionMessage;
        }

        public void setExceptionMessage(String exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
        }

        public StackTraceElement[] getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(StackTraceElement[] stackTrace) {
            this.stackTrace = stackTrace;
        }
    }
}
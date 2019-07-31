package cn.aezo.core.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T, L> implements Serializable {
    private static final String Meta_Status_Success = "success";
    private static final String Meta_Status_Error = "error";
    private static final Integer Meta_Code_Success = 20000;
    private static final Integer Meta_Code_Error = 40000;

    private String metaStatus;
    private String metaMessage;
    private Map<String, Object> data;
    private T payload;
    private Integer metaCode;
    private String metaCodeKey;
    private List<L> metaErrors;

    public static boolean isSuccess(Result result) {
        return (result != null && Result.Meta_Status_Success.equals(result.getMetaStatus()));
    }

    public static boolean isError(Result result) {
        return !isSuccess(result);
    }

    public static Long getLongValue(Map<String, Object> context, String key) {
        if(context == null) return null;
        Object obj = context.get(key);
        if(obj == null) return null;
        if(obj instanceof Long) {
            return (Long) obj;
        } else if(obj instanceof Integer || obj instanceof String) {
            return Long.valueOf(obj + "");
        } else {
            throw new IllegalStateException("Illegal Long Key: " + key);
        }
    }

    public static Result success() {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Success;
        result.metaCode = Result.Meta_Code_Success;
        return result;
    }

    public static Result success(String metaMessage) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Success;
        result.metaCode = Result.Meta_Code_Success;
        result.metaMessage = metaMessage;
        return result;
    }

    public static Result success(Map<String, Object> data) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Success;
        result.metaCode = Result.Meta_Code_Success;
        result.data = data;
        return result;
    }

    public static <T> Result success(T payload) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Success;
        result.metaCode = Result.Meta_Code_Success;
        result.payload = payload;
        return result;
    }

    public static Result failure() {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Error;
        result.metaCode = Result.Meta_Code_Error;
        return result;
    }

    public static Result failure(String metaMessage) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Error;
        result.metaCode = Result.Meta_Code_Error;
        result.metaMessage = metaMessage;
        return result;
    }

    public static Result failure(String metaMessage, String metaCodeKey) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Error;
        result.metaCode = Result.Meta_Code_Error;
        result.metaMessage = metaMessage;
        result.metaCodeKey = metaCodeKey;
        return result;
    }

    public static Result failure(Integer metaCode, String metaMessage) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Error;
        result.metaCode = metaCode;
        result.metaMessage = metaMessage;
        return result;
    }

    public static Result failure(String metaMessage, Map<String, Object> data) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Error;
        result.metaCode = Result.Meta_Code_Error;
        result.metaMessage = metaMessage;
        result.data = data;
        return result;
    }

    public static <T> Result failure(String metaMessage, T payload) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Error;
        result.metaCode = Result.Meta_Code_Error;
        result.metaMessage = metaMessage;
        result.payload = payload;
        return result;
    }

    public static Result failure(String metaMessage, String metaCodeKey, Map<String, Object> data) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Error;
        result.metaCode = Result.Meta_Code_Error;
        result.metaMessage = metaMessage;
        result.metaCodeKey = metaCodeKey;
        result.data = data;
        return result;
    }

    public static <T> Result failure(String metaMessage, String metaCodeKey, T payload) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Error;
        result.metaCode = Result.Meta_Code_Error;
        result.metaMessage = metaMessage;
        result.metaCodeKey = metaCodeKey;
        result.payload = payload;
        return result;
    }

    public static <L> Result failure(String metaMessage, List<L> metaErrors, Map<String, Object> data) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Error;
        result.metaCode = Result.Meta_Code_Error;
        result.metaMessage = metaMessage;
        result.metaErrors = metaErrors;
        result.data = data;
        return result;
    }

    public static <L> Result failure(Integer metaCode, String metaMessage, List<L> metaErrors, Map<String, Object> data) {
        Result result = new Result();
        result.metaStatus = Result.Meta_Status_Error;
        result.metaCode = metaCode;
        result.metaMessage = metaMessage;
        result.metaErrors = metaErrors;
        result.data = data;
        return result;
    }
}
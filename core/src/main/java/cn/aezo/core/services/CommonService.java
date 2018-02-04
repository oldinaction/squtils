package cn.aezo.core.services;

import cn.aezo.utils.base.ValidU;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by smalle on 2017/11/19.
 */
public class CommonService {
    public static final String RetStatus = "retStatus";
    public static final String RetMessage = "retMessage";
    public static final String StatusSuccess = "success";
    public static final String StatusError = "error";

    public static Map<String, Object> returnSuccess() {
        Map<String, Object> retMap = new HashMap();
        retMap.put(CommonService.RetStatus, CommonService.StatusSuccess);
        return retMap;
    }

    public static Map<String, Object> returnSuccess(Map<String, ? extends Object> results) {
        Map<String, Object> retMap = new HashMap();
        retMap.put(CommonService.RetStatus, CommonService.StatusSuccess);
        retMap.putAll(results);

        return retMap;
    }

    public static Map<String, Object> returnError(String message) {
        Map<String, Object> retMap = new HashMap();
        retMap.put(CommonService.RetStatus, CommonService.StatusError);
        retMap.put(CommonService.RetMessage, message);
        return retMap;
    }

    public static Map<String, Object> returnError(String message, Map<String, ? extends Object> results) {
        Map<String, Object> retMap = new HashMap();
        retMap.put(CommonService.RetStatus, CommonService.StatusError);
        retMap.put(CommonService.RetMessage, message);
        retMap.putAll(results);
        return retMap;
    }

    public static boolean isSuccess(Map<String, Object> result) {
        if(ValidU.isEmpty(result)) return false;
        String retStatus = (String) result.get(CommonService.RetStatus);
        return CommonService.StatusSuccess.equals(retStatus) ? true : false;
    }

    public static boolean isError(Map<String, Object> result) {
        return !isSuccess(result);
    }
}

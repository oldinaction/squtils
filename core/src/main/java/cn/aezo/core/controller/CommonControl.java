package cn.aezo.core.controller;

import cn.aezo.core.common.CommonKeys;
import cn.aezo.core.model.SmUser;
import cn.aezo.utils.base.BeanU;
import cn.aezo.utils.base.ValidU;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by smalle on 2017/9/24.
 */
public class CommonControl {
    /**
     * 获取用户id
     * @param request
     * @return
     */
    public static String getUserId(HttpServletRequest request) {
        SmUser user = getSessionSmUser(request);
        if(user == null) return null;

        Long id = user.getId();
        if(ValidU.isEmpty(id)) return null;

        return String.valueOf(id);
    }

    /**
     * 获取session中的用户
     * @param request
     * @return
     */
    public static SmUser getSessionSmUser(HttpServletRequest request) {
        return (SmUser) request.getSession().getAttribute(CommonKeys.SessionUserInfo);
    }

    /**
     * 将对象(Bean, Map, List)以json的形式写出
     * @param response
     * @param object
     */
    public static void writeJson(HttpServletResponse response, Object object) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        PrintWriter writer = null;
        try {
            writer = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(writer != null) {
            String str = "";
            if(object instanceof String) {
                str = (String) object;
            } else {
                ObjectMapper om = new ObjectMapper();
                try {
                    str = om.writeValueAsString(object);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            writer.write(str);
            writer.flush();
            writer.close();
        }
    }

    public static void writeSuccess(HttpServletResponse response, String message) {
        Result result = new Result(Result.StatusSuccess, message);
        Map<String, Object> map = BeanU.transBean2Map(result);
        writeJson(response, map);
    }

    public static void writeError(HttpServletResponse response, String message) {
        Result result = new Result(Result.StatusError, message);
        Map<String, Object> map = BeanU.transBean2Map(result);
        writeJson(response, map);
    }

    public static void writeError(HttpServletResponse response, String message, Integer status) {
        Result result = new Result(Result.StatusError, message, status);
        Map<String, Object> map = BeanU.transBean2Map(result);
        writeJson(response, map);
    }

    public static void writeSuccess(HttpServletResponse response, String message, Map<String, ? extends Object> payload) {
        Result result = new Result(Result.StatusSuccess, message);
        Map<String, Object> map = BeanU.transBean2Map(result);
        map.putAll(payload);
        writeJson(response, map);
    }

    public static void writeError(HttpServletResponse response, String message, Map<String, ? extends Object> payload) {
        Result result = new Result(Result.StatusError, message);
        Map<String, Object> map = BeanU.transBean2Map(result);
        map.putAll(payload);
        writeJson(response, map);
    }

    public static void writeError(HttpServletResponse response, String message, Integer status, Map<String, ? extends Object> payload) {
        Result result = new Result(Result.StatusError, message, status);
        Map<String, Object> map = BeanU.transBean2Map(result);
        map.putAll(payload);
        writeJson(response, map);
    }

    public static class Result {
        public static final String StatusSuccess = "success";
        public static final String StatusError = "error";

        private String retStatus;
        private String retMessage;
        private Integer retCode;

        public Result() {}

        public Result(String retStatus, String retMessage) {
            this.retStatus = retStatus;
            this.retMessage = retMessage;
        }

        public Result(String retStatus, String retMessage, Integer retCode) {
            this.retStatus = retStatus;
            this.retMessage = retMessage;
            this.retCode = retCode;
        }

        public String getRetStatus() {
            return retStatus;
        }

        public void setRetStatus(String retStatus) {
            this.retStatus = retStatus;
        }

        public String getRetMessage() {
            return retMessage;
        }

        public void setRetMessage(String retMessage) {
            this.retMessage = retMessage;
        }

        public Integer getRetCode() {
            return retCode;
        }

        public void setRetCode(Integer retCode) {
            this.retCode = retCode;
        }
    }
}

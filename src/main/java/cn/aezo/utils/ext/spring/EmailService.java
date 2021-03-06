package cn.aezo.utils.ext.spring;

import cn.aezo.utils.base.ValidU;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Map;

/**
 * 邮件发送服务
 * @author smalle
 * @since 2021-01-05
 */
@Service
@Slf4j
public class EmailService {

    private static JavaMailSender javaMailSender;
    private static String sender;

    @Value("${spring.mail.properties.from:}")
    private String from;
    @Value("${spring.mail.username:}")
    private String username;

    @Lazy
    @Autowired(required = false)
    public EmailService(JavaMailSender javaMailSender) {
        EmailService.javaMailSender = javaMailSender;
    }

    @PostConstruct
    public void init() {
        if(ValidU.isNotEmpty(from)) {
            EmailService.sender = from;
        } else {
            EmailService.sender = username;
        }
    }

    /**
     * 发送简单邮件
     * @author smalle
     * @since 2021/2/6
     * @param subject 主题
     * @param content 内容
     * @param to 收件人
     * @return boolean
     */
    public static boolean sendEmail(String subject, String content, String... to) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage,true);
            messageHelper.setFrom(sender);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(content,true);
            javaMailSender.send(mimeMessage);
            log.debug("邮件发送成功. 邮件内容：" + content + ". 收件人：" + Arrays.asList(to));
            return true;
        } catch (Exception e) {
            log.error("邮件发送出错. 邮件内容：" + content + ". 收件人：" + Arrays.asList(to), e);
            return false;
        }
    }

    /**
     * 基于模板发送邮件
     * @param subject 主题
     * @param customEngine 使用的模板类型. 如：VelocityEngine.class、FreemarkerEngine.class
     * @param tpl 模板相对路径，基于 classpath:/templates/email 目录
     * @param context 模板填充值
     * @param to 收件人
     * @return boolean
     */
    public static boolean sendEmailByTpl(String subject, Class<? extends TemplateEngine> customEngine, String tpl, Map<String, Object> context, String... to) {
        try {
            TemplateConfig templateConfig = new TemplateConfig("/templates/email", TemplateConfig.ResourceMode.CLASSPATH);
            if(customEngine != null) {
                templateConfig.setCustomEngine(customEngine);
            }
            TemplateEngine engine = TemplateUtil.createEngine(templateConfig);
            Template template = engine.getTemplate(tpl);
            String content = template.render(context);
            return sendEmail(subject, content, to);
        } catch (Exception e) {
            log.error("邮件发送出错. 邮件内容：" + context + ". 收件人：" + Arrays.asList(to), e);
            return false;
        }
    }

    public static boolean sendEmailByTpl(String subject, String tpl, Map<String, Object> context, String... to) {
        return sendEmailByTpl(subject, null, tpl, context, to);
    }
}

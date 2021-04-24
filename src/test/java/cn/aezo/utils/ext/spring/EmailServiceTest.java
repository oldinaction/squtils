package cn.aezo.utils.ext.spring;

import cn.aezo.utils.base.MiscU;
import cn.hutool.extra.template.engine.velocity.VelocityEngine;

/**
 * @author smalle
 * @since 2021-02-06
 */
public class EmailServiceTest {

    // @Test
    public void sendEmail() {
        EmailService.sendEmail("SqBiz测试邮件主题", "SqBiz测试邮件", "demo@aezo.cn");
    }

    // @Test
    public void sendEmailByTpl() {
        EmailService.sendEmailByTpl("SqBiz测试模板邮件主题", VelocityEngine.class, "code.html.vm", MiscU.toMap(
                "signature", "SqBiz管理系统", "action", "注册账号" ,"code", "123456"),
                "demo@aezo.cn");
    }
}

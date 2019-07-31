package cn.aezo.core.config;

import cn.aezo.core.config.rawbean.CustomObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Created by smalle on 2019-01-23 23:12.
 */
@Configuration
public class CommonBeanConfig {
    @Bean
    public CustomObjectMapper customObjectMapper() {
        return new CustomObjectMapper()
                .setNotContainNull()
                .setDisableWriteDatesAsTimestamps(true)
                .setDateFormatPattern(DsConstant.dateFormat);
        // .setCamelCaseToLowerCaseWithUnderscores();
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter(this.customObjectMapper());
    }
}

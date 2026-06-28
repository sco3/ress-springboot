package com.tnf.cascfg;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;

import sco.common.properties.BisPropertiesHelper;
import sco.common.util.CasPropertiesHelper;

@Configuration
@ImportResource("classpath:beans.xml")
@ComponentScan(basePackages = { "com.tnf.cas" })
public class ContextCfg {
    /*
     * <context:property-placeholder location=
     * "classpath:cfg-cas.properties,classpath:cassandra_connection.properties"
     * ignore-resource-not-found="true" ignore-unresolvable="true" />
     */
    @Bean
    public PropertyPlaceholderConfigurer getCfg() {
        PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
        ClassPathResource casCfgProps = new ClassPathResource(//
                CasPropertiesHelper.CAS_PROPERTIES_CONFIG_FILENAME//
        );
        ClassPathResource casProps = new ClassPathResource(//
                BisPropertiesHelper.CASSANDRA_CONNECTION_PROPERTIES_FILE_NAME//
        );
        cfg.setLocations(casCfgProps, casProps);
        cfg.setIgnoreUnresolvablePlaceholders(true);
        cfg.setIgnoreResourceNotFound(true);
        return cfg;
    }
}

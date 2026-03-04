package com.lazydrop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spaces")
public class SpacesProperties {

    /** Full endpoint, e.g. https://nyc3.digitaloceanspaces.com */
    private String endpoint;

    /** DO Spaces region, e.g. nyc3, sfo3, ams3 */
    private String region;

    /** Bucket (Space) name */
    private String bucketName;

    /** Spaces access key */
    private String accessKey;

    /** Spaces secret key */
    private String secretKey;
}

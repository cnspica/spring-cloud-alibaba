package com.thatday.gateway.filter;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;


/**
 * nacos properties config
 * DEFAULT_GROUP
 * Properties
 * server-gateway-product.properties
 * ---------------------------------
 * enableIpRateLimit=true
 * enableLog=true
 * publicRefillTokens=100
 * publicCapacity=500
 * ipRefillTokens=1
 * ipCapacity=50
 */
@Data
@Component
@RefreshScope
public class RateLimitPubConfig {

    @Value("${enableIpRateLimit:true}")
    private boolean enableIpRateLimit;

    @Value("${enableLog:false}")
    private boolean enableLog;

    //通用限流
    @Value("${publicRefillTokens:100}")
    private int publicRefillTokens;

    @Value("${publicCapacity:200}")
    private int publicCapacity;

    //通用ip限流
    @Value("${ipRefillTokens:10}")
    private int ipRefillTokens;

    @Value("${ipCapacity:50}")
    private int ipCapacity;

}

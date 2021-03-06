package com.thatday.gateway.loadbalance;

import com.alibaba.cloud.nacos.ribbon.NacosServer;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * 按照权重走的 配合nacos
 */
public class WeightBalanceRule implements IChooseRule {

    private static final Map<String, Integer> nextServerMap = new ConcurrentHashMap<>();

    @Override
    public ServiceInstance choose(ServerWebExchange exchange, DiscoveryClient discoveryClient) {
        URI originalUrl = (URI) exchange.getAttributes().get(GATEWAY_REQUEST_URL_ATTR);
        String instancesId = originalUrl.getHost();
        List<ServiceInstance> instances = discoveryClient.getInstances(instancesId);
        return weightChooseRule(instancesId, instances);
    }

    //one by one
    private ServiceInstance defaultNext(String serverName, List<ServiceInstance> reachableServers) {
        Integer pos = nextServerMap.get(serverName);
        if (pos == null || pos >= reachableServers.size()) {
            pos = 0;
        }
        ServiceInstance server = reachableServers.get(pos);
        nextServerMap.put(serverName, pos + 1);
        return server;
    }

    private ServiceInstance weightChooseRule(String key, List<ServiceInstance> reachableServers) {
        if (reachableServers.size() == 0) {
            return null;
        }

        if (reachableServers.size() == 1) {
            return reachableServers.get(0);
        }

        String serverName = key;

        //全部权重相加
        int totalWeight = 0;
        for (ServiceInstance server : reachableServers) {
            if (server instanceof NacosServer) {
                serverName = ((NacosServer) server).getInstance().getServiceName();
                NacosServer nacosServer = ((NacosServer) server);
                totalWeight += (int) nacosServer.getInstance().getWeight();
            }
        }

        //如果不是nacos的话应该是0，权重都是1的情况就轮询的走
        if (totalWeight == 0 || totalWeight == reachableServers.size()) {
            return defaultNext(serverName, reachableServers);
        }

        //以全部权重为总和随机
        double random = Math.random() * totalWeight;

        //如果随机数减去当前的权重<=0了就是说明他就是这个服务
        for (ServiceInstance server : reachableServers) {
            if (server instanceof NacosServer) {
                NacosServer nacosServer = ((NacosServer) server);
                int w = (int) nacosServer.getInstance().getWeight();
                random -= w;

                if (random <= 0) {
                    return server;
                }
            }
        }

        return defaultNext(serverName, reachableServers);
    }
}

package com.springtutorials.timeline.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.UserCodeDeploymentConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class HazelcastConfig {
    private static final String MULTICAST_GROUP = "224.2" + ".2.15";
    private static final Integer MULTICAST_PORT = 54329;

    @Value("${payment.hazelcast.multicast.enabled:false}")
    private boolean multicastEnabled;

    @Value("${payment.hazelcast.kubernetes.enabled:false}")
    private boolean kubernetesEnabled;

    @Value("${payment.hazelcast.tcp-ip.enabled:false}")
    private boolean tcpIpEnabled;

    @Value("${payment.hazelcast.tcp-ip.nodes:}")
    private List<String> servers;

    @Value("${payment.hazelcast.kubernetes.namespace:}")
    private String kubernetesNamespace;

    @Value("${payment.hazelcast.kubernetes.service-dns:}")
    private String kubernetesServiceDns;

    @Value("${payment.hazelcast.clusterName:}")
    private String clusterName;

    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public Config config() {
        Config config = new Config();

        config.setClassLoader(Thread.currentThread().getContextClassLoader());

        UserCodeDeploymentConfig distCLConfig = config.getUserCodeDeploymentConfig();
        distCLConfig.setEnabled(true)
                .setClassCacheMode(UserCodeDeploymentConfig.ClassCacheMode.ETERNAL)
                .setProviderMode(UserCodeDeploymentConfig.ProviderMode.LOCAL_AND_CACHED_CLASSES);

        config.setInstanceName(clusterName);

        config.setProperty("hazelcast.max.no.heartbeat.seconds", "10");
        config.setProperty("hazelcast.phone.home.enabled", "false");
        config.setProperty("hazelcast.rest.enabled", "false");
        config.setProperty("hazelcast.socket.connect.timeout.seconds", "60");

        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701);
        network.setPortCount(5);
        network.setReuseAddress(true);
        network.setPortAutoIncrement(true);
        network.addOutboundPortDefinition("5706-5770");


        JoinConfig join = network.getJoin();
        join.getKubernetesConfig().setEnabled(kubernetesEnabled)
                .setProperty("namespace", kubernetesNamespace)
                .setProperty("service-dns", kubernetesServiceDns);
        join.getMulticastConfig().setEnabled(multicastEnabled)
                .setMulticastGroup(MULTICAST_GROUP)
                .setMulticastPort(MULTICAST_PORT)
                .setMulticastTimeToLive(1)
                .setMulticastTimeoutSeconds(10);
        join.getAwsConfig().setEnabled(false);
        for (String ip : servers) {
            join.getTcpIpConfig().addMember(ip);
        }
        join.getTcpIpConfig().setEnabled(tcpIpEnabled);

        return config;
    }

}

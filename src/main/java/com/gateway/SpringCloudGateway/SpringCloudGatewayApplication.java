package com.gateway.SpringCloudGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
//@EnableWebFlux


//@EnableAutoConfiguration(exclude = {
//		org.springframework.cloud.gateway.config.GatewayClassPathWarningAutoConfiguration.class
//})
//public class SpringCloudGatewayApplication extends SpringBootServletInitializer {
public class SpringCloudGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudGatewayApplication.class, args);
	}
	



}

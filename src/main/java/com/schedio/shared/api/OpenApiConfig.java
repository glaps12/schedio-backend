package com.schedio.shared.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

	@Bean
	OpenAPI schedioOpenApi() {
		return new OpenAPI().info(new Info()
			.title("Schedio API")
			.description("Appointment and business management API")
			.version("v1"));
	}
}

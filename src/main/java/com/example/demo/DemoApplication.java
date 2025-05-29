package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Snow Proxy API",
        version = "0.0.1",
        description = "OpenAPI documentation for the Snow database/SFTP proxy middleware"
    ),
    servers = @Server(url = "/", description = "Default Server URL")
)
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

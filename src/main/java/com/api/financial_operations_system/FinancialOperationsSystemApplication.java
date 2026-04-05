package com.api.financial_operations_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity
@SpringBootApplication
public class FinancialOperationsSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialOperationsSystemApplication.class, args);
	}

}

package io.github.MLNaiba.simpleinvoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SimpleInvoiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleInvoiceApplication.class, args);
	}

}

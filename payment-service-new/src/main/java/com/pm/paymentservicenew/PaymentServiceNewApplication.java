package com.pm.paymentservicenew;

import com.pm.paymentservicenew.config.PaymentKafkaProperties;
import com.pm.paymentservicenew.integration.paystack.config.PaystackProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication
@EnableConfigurationProperties({

		PaystackProperties.class,

		PaymentKafkaProperties.class

})
public class PaymentServiceNewApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceNewApplication.class, args);
	}

}

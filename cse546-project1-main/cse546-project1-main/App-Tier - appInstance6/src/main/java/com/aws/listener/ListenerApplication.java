package com.aws.listener;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.aws.listener.config.AppConfig;
import com.aws.listener.service.ListenerAndDispatchingService;

@SpringBootApplication
public class ListenerApplication {

	public static void main(String[] args) throws IOException, ParseException {
		SpringApplication.run(ListenerApplication.class, args);
		System.out.println("Application is running!");
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		// context.refresh();
		ListenerAndDispatchingService listenerAndDispatchingService = context
				.getBean(ListenerAndDispatchingService.class);
		listenerAndDispatchingService.generalFunction();
		context.close();
	}
}

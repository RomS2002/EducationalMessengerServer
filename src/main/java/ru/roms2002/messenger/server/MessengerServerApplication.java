package ru.roms2002.messenger.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MessengerServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessengerServerApplication.class, args);
	}
}

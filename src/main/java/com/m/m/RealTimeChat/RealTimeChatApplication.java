package com.m.m.RealTimeChat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@SpringBootApplication
//@EnableJdbcHttpSession
public class RealTimeChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealTimeChatApplication.class, args);
	}

}

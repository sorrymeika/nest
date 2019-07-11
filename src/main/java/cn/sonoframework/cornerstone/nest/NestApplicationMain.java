package cn.sonoframework.cornerstone.nest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@ComponentScan
@ServletComponentScan
@EnableScheduling
public class NestApplicationMain {
	private static Logger log = LoggerFactory.getLogger(NestApplicationMain.class);

	public static void main(String[] args) {
		log.info("application start!!");

		System.out.print("application start");

		SpringApplication.run(NestApplicationMain.class, args);
	}

}

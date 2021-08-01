package br.com.pedroxsqueiroz.bt.crypto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;
import java.util.logging.Logger;

@SpringBootApplication
@EnableAutoConfiguration
@EnableConfigurationProperties
@EnableScheduling
public class Application {

	private static Logger LOGGER = Logger.getLogger( Application.class.getName() );

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

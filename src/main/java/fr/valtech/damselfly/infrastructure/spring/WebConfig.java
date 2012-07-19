package fr.valtech.damselfly.infrastructure.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Configuration
@EnableWebMvc
@ComponentScan( { "fr.valtech.damselfly.interfaces.rest" } )
public class WebConfig {

	
	
	
	
}

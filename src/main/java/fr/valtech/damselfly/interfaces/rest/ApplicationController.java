package fr.valtech.damselfly.interfaces.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.http.HttpStatus;

import fr.valtech.damselfly.domain.model.NotFoundException;


/**
 * 
 * 
 * @author nchapon
 *
 */
@Controller
public class ApplicationController {

	
	@RequestMapping(value="/{appname}/{env}/{key}",method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody String get(@PathVariable String appname, @PathVariable String env, @PathVariable String key) {
		if (! "databaseUrl".equals(key)){
		    throw new NotFoundException();
		} 
		return "jdbc:mysql://localhost/mydb"; 
	}
	
	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(value=HttpStatus.NOT_FOUND,reason="Contact not found")
	public void notFound() { }
	
}

package fr.valtech.damselfly.interfaces.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import fr.valtech.damselfly.domain.model.ConfigData;
import fr.valtech.damselfly.domain.model.NotFoundException;
import fr.valtech.damselfly.service.GraphService;

/**
 * 
 * 
 * @author nchapon, jlt
 * 
 */
@Controller
public class ApplicationController {
	static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

	
	@Autowired()
	private GraphService gs;

	// private Repository item;

	@RequestMapping(value = "/apps/{appname}/{env}/{key}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody
	ConfigData get(@PathVariable String appname, @PathVariable String env,
			@PathVariable String key) {
		logger.debug("*** HOURA  "+appname+env+key);
		return gs.retrieveNodeProperty(appname, env, key);
	}

//	@RequestMapping(value = "/apps/{appname}/{env}/{key}/{value}", method = RequestMethod.PUT)
//	@ResponseBody
//	public ConfigData update(@PathVariable String appname,
//			@PathVariable String env, @PathVariable String key,
//			@PathVariable String value, @RequestBody ConfigData cd) {
//		System.out.println(this.getClass().getCanonicalName() + " update");
//		cd.setValue(value);
//		return cd;
//	}

	// @RequestMapping(value = "/apps/{appname}/{env}/{key}", method =
	// RequestMethod.DELETE)
	// public String delete(@PathVariable String appname,
	// @PathVariable String env, @PathVariable String key, Model model,
	// HttpServletResponse response) {
	//
	// List<Object> li = item.findByKey(key);
	//
	// return "redirect:/"+appname+"/"+env+"/"+key;
	// vrai -> suppression possible
	// faux -> suppression impossible, (par exemple à cause d'une cle
	// etrangere)
	// Object o = item.delete(new Long(li.get(0).toString()));
	// boolean wasOk = (o != null ? true : false);
	//
	// if (!wasOk) {
	// throw new DataAccessException("Unable to delete item: " + item);
	// Indication à l'utilisateur que l'article ne peut pas etre
	// supprime
	// response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	// model.addAttribute("item", li.get(0));
	// return "items/error";
	// }
	//
	// return "redirect:/items";
	// }
	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Key not found")
	public void notFound() {
	}

	// @ExceptionHandler(DataAccessException.class)
	// @ResponseStatus(HttpStatus.BAD_REQUEST)
	// public String handleDataAccessException(DataAccessException ex) {
	// return "errorView";
	// }

//	@RequestMapping(value = "/apps/{appname}/{env}/{key}/{value}", method = RequestMethod.POST)
//	@ResponseBody
//	public ConfigData create(@PathVariable String appname,
//			@PathVariable String env, @PathVariable String key,
//			@PathVariable String value) {
//		System.out.println(this.getClass().getCanonicalName() + " create");
//		// System.out.println(cd.getValue());
//		// cd.setKey(key);
//		// cd.setEnvrionment(env);
//		// cd.setValue(value);
//		// cd.setApplication(appname);
//		ConfigData cd = new ConfigData(new Long(1), appname, env, key, value);
//		return cd;
//	}

}

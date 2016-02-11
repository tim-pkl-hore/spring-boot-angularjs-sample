package id.tomatech.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import id.tomatech.demo.entity.Person;
import id.tomatech.demo.repository.PersonRepository;

@RestController
@RequestMapping(value="/people")
public class PersonController {
	@Autowired
	private PersonRepository personRepository;
	
	@RequestMapping(method=RequestMethod.GET)
	public HttpEntity<Page<Person>> listPerson(Pageable pageable) {
		Page<Person> listPerson = personRepository.findAll(pageable);
		return new ResponseEntity<Page<Person>>(listPerson, HttpStatus.OK);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public HttpEntity<Person> detailPerson(@PathVariable Long id) {
		if(!personRepository.exists(id)) {
			return new ResponseEntity<Person>(HttpStatus.NOT_FOUND);
		}
		
		Person person = personRepository.findOne(id);
		return new ResponseEntity<Person>(person, HttpStatus.OK);
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public HttpEntity<Person> registerPerson(@RequestBody @Validated Person person, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		Person savedPerson = personRepository.save(person);
		return new ResponseEntity<Person>(savedPerson, HttpStatus.OK);
	}
}

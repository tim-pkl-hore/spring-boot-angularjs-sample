package id.tomatech.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.tomatech.demo.entity.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

}

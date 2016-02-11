#README

## Setup Proyek Spring Boot
Setup proyek spring boot seperti yang sudah kita lakukan pada sesi sebelumnya. Buat proyek maven dan edit ```pom.xml``` sehingga isinya seperti di bawah ini.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>id.tomatech.demo</groupId>
	<artifactId>demo-spring-pkl--angularjs</artifactId>
	<version>0.0.1-SNAPSHOT</version>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.3.2.RELEASE</version>
	</parent>

	<properties>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.postgresql</groupId>
			<artifactId>postgresql</artifactId>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
</project>
```

Buat main-class, kelas yg kita run untuk menjalankan aplikasi, dalam tutorial ini kita gunakan base package ```id.tomatech.demo```. Silahkan pilih yang lain jika mau.

> Pada saat menjalankan main-class, di belakang layar spring-boot menjalankan server tomcat, atau istilah yg lebih tepat servlet-conteiner. Perlu di ingat bahwa spring-boot adalah teknologi yg dikembangkan di atas teknologi servlet (teknologi servlet sendiri adalah bagian dari Java EE). Untuk sejarahnya, bisa dibaca di halaman wikipedia [ini](https://en.wikipedia.org/wiki/Java_servlet).

```java
package id.tomatech.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DemoApplication {
	public static void main(String[] args) {
		new SpringApplicationBuilder(DemoApplication.class)
				.run(args);
	}
}
```

Jangan lupa, set konfigurasi (```application.properties```)  untuk koneksi ke database, termasuk di port mana server akan dijalankan. Silahkan sesuaikan nama database, username dan password mengikuti setting pada komputer kalian.
```ini
spring.datasource.url=jdbc:postgresql://localhost/tutorial-spring-dan-jpa
spring.datasource.username=postgres
spring.datasource.password=ehrahasialah!

spring.jpa.generate-ddl=true

server.port=9999
```

> Spring Boot menggunakan teknologi jdbc untuk koneksi ke database relasional (MySql, PostgreSql, Oracle Database, IBM DB2 Database, MsSQL dll). Teknologi jdbc sendiri merupakan bagian dari standard java. Untuk pengetahuan tambahan, silahkan baca halam wikipedia [ini](https://en.wikipedia.org/wiki/Java_Database_Connectivity).

> Selain penyimpanan data menggunakan database relational, kita bisa menggunakan NoSQL database, misalnya MongoDb yg menyimpan data dalam bentuk document json.

Buat entity sederhana ```Person```, termasuk repository dan controller untuk handle request dari client (Mobile atay browser).

```java
package id.tomatech.demo.entity;

// ... imports diabaikan, silahkan pake Ctrl+Shift+O

@Entity
@Table(name="person")
public class Person {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private Long id;
	
	@NotBlank
	@Column(name="name")
	private String name;
	
	@NotBlank
	@Email
	@Column(name="email")
	private String email;
	
	@NotBlank
	@Column(name="phone")
	private String phone;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name="gender")
	private Gender gender;
	
	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name="date_of_birth")
	private Date dateOfBirth;
	
	// ... Getter dan setter diabaikan, silahkan generate. 
}
```

Untuk gender dari person, kita menggunakan enum type ```Gender```.
```java
package id.tomatech.demo.entity;

public enum Gender {
	MALE, FEMALE
}
```

> Enum biasanya di gunakan untuk memodelkan pilihan yang tetap. Sebagai contoh lain ```enum DayOfWeek {MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY}```.

```java
package id.tomatech.demo.repository;
// ... imports diabaikan, silahkan pake Ctrl+Shift+O

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

}
```

```java
package id.tomatech.demo.controller;

// ... imports diabaikan, silahkan pake Ctrl+Shift+O

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
```

> Perlu diingat, type ```ResponseEntity``` adalah implementasi dari ```HttpEntity```.

## Front End AngularJS

Pertama buat file ```index.html``` dalam directory ```src/main/resources/public```. Berdasarkan perjanjian, seluruh content dalam directory tersebut dapat direquest langsung dari client secara statis (tidak perlu pakai controller). Maksudnya, pada saat akses ```http://localhost:9999/index.html``` maka otomatis ```index.html``` dalam directory tersebut yg kirim sebagai response.

> Untuk lebih jelas tentang static content ini, silahkan baca [tutorial ini](https://spring.io/blog/2013/12/19/serving-static-web-content-with-spring-boot).

```html
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Sample AngularJS</title>
</head>
<body>

</body>
</html>
```

Download [angular.min.js](https://code.angularjs.org/1.5.0/angular.min.js) , lalu simpaln dalam ```src/main/resources/public/scripts/vendor```. File ini adalah library core dari angularjs. Kemudian edit file ```index.html```.

Kemudian edit file ```index.html``` untuk manampilkan list dari person.

```html
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Sample AngularJS</title>
</head>
<body ng-app="tutorialApp">
	<!-- Start: Show list of person -->
	<div ng-controller="listCtrl">
		<table border="1">
			<thead>
				<tr>
					<th>Name</th>
					<th>Gender</th>
					<th>Date of Birth</th>
					<th>Email</th>
					<th>Phone</th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="item in items">
					<td>{{item.name}}</td>
					<td>{{item.gender}}</td>
					<td>{{item.dateOfBirth}}</td>
					<td>{{item.email}}</td>
					<td>{{item.phone}}</td>
				</tr>
			</tbody>
		</table>
	</div>
	<!-- Start: Show list of person -->
	<script type="text/javascript" src="/scripts/vendor/angular.min.js"></script>
	<script type="text/javascript">
		// our custom scripts.
		'use strict';
		angular.module('tutorialApp', []).controller('listCtrl',
				function($scope, $http, $log) {
					$scope.items = [];

					var request = {
						url : '/people',
						method : 'GET'
					};
					var successHandler = function(response) {
						// Tampilin struktur dari 'response' di console browser.
						$log.debug(angular.toJson(response, true));
						$scope.items = response.data.content;
					};
					var errorHandler = function(errors) {
						// Tampilin struktur dari 'errors' di console browser.
						$log.error(angular.toJson(errors, true));
					};

					// Make http request for member list.
					$http(request).then(successHandler, errorHandler);
				});
	</script>
</body>
</html>
```

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

### Setup Front End
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
	
	<script type="text/javascript" src="/scripts/vendor/angular.min.js"></script>
	<script type="text/javascript">
		// our custom scripts.
		'use strict';
		
		var app = angular.module('tutorialApp', []);
	</script>
</body>
</html>
```

Pada element ```<body>``` ditambahkan attribute ```ng-app```. Dalam angularjs, ```ng-app``` ini disebut ```directive```. Directive tujuannya untuk 'memperkaya' html. Sementara, ```ng-app``` sendiri maksudnya menandakan bahwa seluruh element DOM dalam ```<body>``` ada dalam skup module ```tutorialApp```. Sebenarnya, kita bisa mendefinisikan ```ng-app``` pada element ```<http>```, atau element lain sesuai keperluan.

Kemudian perhatikan custom javascript, kita mendefinisikan modul baru ```tutorialApp``` dengan sintaks
```js
var app = angular.module('tutorialApp', []);
```

### List Person
Tambah snippet berikut ini ke dalam element ```<body>``` file ```index.html```.
```html
	<!-- Start: Show list of person -->
	<div ng-controller="listCtrl">
		<h2>List People</h2>
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
	<!-- End: Show list of person -->
```

Perhatikan element ```<div>``` pada snippet di atas, ada directive ```ng-controller```. Maksudnya, element ```<div>``` tersebut (dan seluruh child DOM element di dalamnya) akan di binding ke controller tersebut. Directive berikutnya adalah ```ng-repeat```, fungsinya untuk itrasi array atau object, dalam contoh di atas iterasi scope object bernama ```items```

Dalam block javascript, tambahkan snippet di bawah ini (seletah module)

```js
		app.controller('listCtrl', function($scope, $http, $log) {
			$scope.items = [];

			var request = {
				url : '/people',
				method : 'GET'
			};
			var successHandler = function(response) {
				// Tampilin struktur dari 'response' di console browser.
				$log.debug("Response data dari server :\n" + angular.toJson(response.data, true));
				$scope.items = response.data.content;
			};
			var errorHandler = function(errors) {
				// Tampilin struktur dari 'errors' di console browser.
				$log.error(angular.toJson(errors, true));
			};

			// Make http request for member list.
			$http(request).then(successHandler, errorHandler);
		});
```

>Cara mendefinisikan controller dalam module ```tutorialApp``` adalah seperti berikut
>```js
>app.controller('namaController', controllerFunction);
>```



Perlu dicatat, ```$scope```, ```$http``` dan ```$log``` adalah service bawaan angular. Dalam contoh ini, parameter ```$scope```, ```$http``` dan ```$log``` dari controller akan di-auto-inject atau autowired berdasarkan namanya, jadi penulisan nama service itu tidak boleh salah.

### Register Person

Tambah snippet berikut ini ke dalam element ```<body>``` file ```index.html```.
```html
	<!-- Start: Show register person form -->
	<div ng-controller="registerCtrl">
		<h2>Register Person</h2>
		<form ng-submit="submit()">
			<input type="text" ng-model="person.name" placeholder="Full Name"><br />
			<select ng-model="person.gender">
				<option value="MALE">Male</option>
				<option value="FEMALE">Female</option>
			</select><br />
			<input type="text" ng-model="person.dateOfBirth" placeholder="Date of Birth (yyyy-MM-dd)"><br />
			<input type="text" ng-model="person.email" placeholder="Email Address"><br />
			<input type="text" ng-model="person.phone" placeholder="Phone Number"><br />
			<input type="submit" value="Register">
		</form>
	</div>
	<!-- End: Show register person form -->
```

Dalam snippet form di atas, ada beberapa directive baru, ```ng-submit``` pada element ```<form>``` maksudnya pada saat form tersebut disubmit maka akan di-handle oleh fungsi $scope yg diberikan, dalam contoh ini fungsi bernama ```submit()```. Kemudian directive ```ng-model``` di element ```<input>``` dan ```<select>``` maksudnya binding dua arah isi dari element ke object dalam scope dengan nama ```person```, jadi pada saat input diubah, nilai dari object scope ```person``` juga berubah, begitu juga sebaliknya. Untuk membuktikannya, copy-paste element ```<form>``` dari snippet di atas dua kali dalam satu controller yang sama pada file ```index.html``` lalu udah satu form, perhatikan form yang lainnya seperti contoh berikut ini

```html
	<!-- Start: Show register person form -->
	<div ng-controller="registerCtrl">
		<h2>Register Person</h2>
		<form ng-submit="submit()">
			<input type="text" ng-model="person.name" placeholder="Full Name"><br />
			<select ng-model="person.gender">
				<option value="MALE">Male</option>
				<option value="FEMALE">Female</option>
			</select><br />
			<input type="text" ng-model="person.dateOfBirth" placeholder="Date of Birth (yyyy-MM-dd)"><br />
			<input type="text" ng-model="person.email" placeholder="Email Address"><br />
			<input type="text" ng-model="person.phone" placeholder="Phone Number"><br />
			<input type="submit" value="Register">
		</form>
		
		<h2>Register Person 2</h2>
		<form ng-submit="submit()">
			<input type="text" ng-model="person.name" placeholder="Full Name"><br />
			<select ng-model="person.gender">
				<option value="MALE">Male</option>
				<option value="FEMALE">Female</option>
			</select><br />
			<input type="text" ng-model="person.dateOfBirth" placeholder="Date of Birth (yyyy-MM-dd)"><br />
			<input type="text" ng-model="person.email" placeholder="Email Address"><br />
			<input type="text" ng-model="person.phone" placeholder="Phone Number"><br />
			<input type="submit" value="Register">
		</form>
	</div>
	<!-- End: Show register person form -->
```

Selanjutnya, buat controller baru ```registerCtrl``` dalam module ```tutorialApp```
```js
		// ... Other custom scripts omitted.
		app.controller('registerCtrl', function($scope, $http, $log) {
			$scope.person = {};
			
			$scope.submit = function() {
				var request = {
					url: '/people',
					method: 'POST',
					data: $scope.person
				};
				var successHandler = function(response) {
					$log.debug('Response data dari server :\n' + angular.toJson(response.data, true));
				};
				var errorHandler = function(errors) {
					$log.error('Errors :\n' + angular.toJson(errors, true));
				};
				$http(request).then(successHandler, errorHandler);
			};
		});
```
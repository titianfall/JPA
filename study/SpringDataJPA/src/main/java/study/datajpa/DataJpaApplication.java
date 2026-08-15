package study.datajpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @SpringBootApplication
 *
 * 		@ComponentScan -> 이 패키지 + 하위에서 @Component 붙은 "클래스"를 찾음
 * 		@EnableAutoConfiguration
 * 			@AutoConfiguationPackage -> 이 클래스의 패키지를 AutoConfigurationpackages에 등록
 * 		@SpringBootConfiguration
 */
@SpringBootApplication
//@EnableJpaRepositories(basePackages = "study.datajpa.repository")
public class DataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataJpaApplication.class, args);
	}

}

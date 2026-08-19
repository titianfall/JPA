package study.datajpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;
import java.util.UUID;

/**
 * @SpringBootApplication
 *
 * 		@ComponentScan -> 이 패키지 + 하위에서 @Component 붙은 "클래스"를 찾음
 * 		@EnableAutoConfiguration
 * 			@AutoConfiguationPackage -> 이 클래스의 패키지를 AutoConfigurationpackages에 등록
 * 		@SpringBootConfiguration
 */
@EnableJpaAuditing // 이게 없으면 @CreatedDate 등이 동작하지 않아 값이 전부 null 로 남는다
@SpringBootApplication
//@EnableJpaRepositories(basePackages = "study.datajpa.repository")
public class DataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataJpaApplication.class, args);
	}

	@Bean
	public AuditorAware<String> auditorProvider() {

		return () -> Optional.of(UUID.randomUUID().toString());
	}
}

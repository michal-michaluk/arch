package devices.configuration;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
@ConfigurationPropertiesScan
@EnableJpaRepositories(considerNestedRepositories = true)
class AppConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

}

package mingovvv.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@SpringBootApplication
@ConfigurationPropertiesScan
public class CommonJdk25Application {

    static void main(String[] args) {
        SpringApplication.run(CommonJdk25Application.class, args);
    }

}

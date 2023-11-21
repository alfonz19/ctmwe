//do NOT move to different package. Class is used for basePackage scan.
package ctmwe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        //we don't want to close it, otherwise app will end immediately.
        //noinspection resource
        SpringApplication.run(Main.class, args);
    }
}

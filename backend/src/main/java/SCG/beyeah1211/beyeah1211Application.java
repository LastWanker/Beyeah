package SCG.beyeah1211;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("SCG.beyeah1211.dao")
@EnableScheduling
public class beyeah1211Application {
    public static void main(String[] args) {
        SpringApplication.run(beyeah1211Application.class, args);
    }
}

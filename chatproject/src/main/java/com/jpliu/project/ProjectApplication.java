import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 处理流程 为 网页 request <-> controller <-> service <-> dao <-> database
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.jpliu")
public class ProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

}


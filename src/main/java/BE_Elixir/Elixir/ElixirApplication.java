package BE_Elixir.Elixir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootApplication
public class ElixirApplication {

	public static void main(String[] args) {
		// KST로 타임존 설정
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));

		SpringApplication.run(ElixirApplication.class, args);
	}

}

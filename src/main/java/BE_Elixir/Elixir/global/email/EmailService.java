package BE_Elixir.Elixir.global.email;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

import jakarta.mail.internet.MimeMessage;


@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {

    private final JavaMailSender emailSender;

    // 인증번호
    private String key;

    @Value("${naver.id}")
    private String id;

    @Getter
    @Setter
    public Instant mailSendTime;

    @Getter
    public Duration validityDuration = Duration.ofMinutes(5);

    // Key 생성 및 메일 전송
    public String sendMail(String to) throws MessagingException, UnsupportedEncodingException {
        key = createKey();

        MimeMessage message = createMessage(to);

        emailSender.send(message);

        return key;
    }

    // 랜덤 인증코드 생성
    private String createKey() {
        int length = 6;

        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // 이메일 본문
    private MimeMessage createMessage(String to) throws MessagingException, UnsupportedEncodingException {

        jakarta.mail.internet.MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(Message.RecipientType.TO, to);

        // 이메일 제목
        message.setSubject("[엘릭서] 비밀번호 초기화 인증번호");

        // 이메일 본문
        String msgg = "";
        msgg += "<div style='margin:20px;'>";
        msgg += "<h2>안녕하세요, Elixir입니다.</h2>";
        msgg += "<p>비밀번호 초기화를 위한 인증번호를 아래에 안내드립니다.</p>";
        msgg += "<br>";
        msgg += "<div style='border:1px solid #ccc; padding:20px; text-align:center;'>";
        msgg += "<h3 style='color:#333;'>인증번호</h3>";
        msgg += "<div style='font-size:24px; font-weight:bold; color:#007BFF;'>" + key + "</div>";
        msgg += "<p style='margin-top:10px;'>인증번호는 <strong>5분간</strong> 유효합니다.</p>";
        msgg += "</div>";
        msgg += "<br>";
        msgg += "<p>본인이 요청하지 않은 경우 이 메일을 무시하셔도 됩니다.</p>";
        msgg += "</div>";

        // HTML 형식으로 본문 설정
        message.setText(msgg, "UTF-8", "html");
        // 보내는 사람 이메일 주소
        message.setFrom(new InternetAddress(id, "엘릭서"));

        return message;
    }
}

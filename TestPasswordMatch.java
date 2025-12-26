import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswordMatch {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String hashedPassword = "$2a$10$7JB720yubVSOfvVWbfXCOOxjTOQcQjmrJF1ZM4nAVccp/.rkMlDWy";
        
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Hashed password: " + hashedPassword);
        System.out.println("Matches: " + encoder.matches(rawPassword, hashedPassword));
    }
}
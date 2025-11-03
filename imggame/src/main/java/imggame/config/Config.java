package imggame.config;
import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getHost() {
        return dotenv.get("HOST", "localhost");
    }

    public static int getPort() {
        return Integer.parseInt(dotenv.get("PORT", "8080"));
    }
}

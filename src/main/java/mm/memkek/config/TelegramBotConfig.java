package mm.memkek.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Telegram bot.
 * Loads settings from application.yml with prefix "telegram.bot".
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotConfig {

    /** Telegram bot token */
    private String token;

    /** Telegram bot username */
    private String username;
}
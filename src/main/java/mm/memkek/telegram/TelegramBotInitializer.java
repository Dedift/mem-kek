package mm.memkek.telegram;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class TelegramBotInitializer {

    private final MemeCollectorBot memeCollectorBot;

    public TelegramBotInitializer(MemeCollectorBot memeCollectorBot) {
        this.memeCollectorBot = memeCollectorBot;
    }

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(memeCollectorBot);
            log.info("Telegram bot '{}' successfully registered", memeCollectorBot.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram bot: {}", e.getMessage(), e);
        }
    }
}
package mm.memkek.telegram;

import lombok.extern.slf4j.Slf4j;
import mm.memkek.config.TelegramBotConfig;
import mm.memkek.service.ChannelService;
import mm.memkek.service.PostSaveService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class MemeCollectorBot extends TelegramLongPollingBot {

    private final TelegramBotConfig config;
    private final PostSaveService postSaveService;
    private final ChannelService channelService;

    public MemeCollectorBot(TelegramBotConfig config,
                            PostSaveService postSaveService,
                            ChannelService channelService) {
        super(config.getToken());
        this.config = config;
        this.postSaveService = postSaveService;
        this.channelService = channelService;

        log.info("MemeCollectorBot initialized with username: {}", config.getUsername());
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Received update: {}", update);

        if (update.hasChannelPost()) {
            Message message = update.getChannelPost();
            log.info("Channel post received from chat: {} (type: {})",
                    message.getChatId(), message.getChat().getType());
            handleChannelMessage(message);
            return;
        }

        if (update.hasMessage()) {
            Message message = update.getMessage();
            log.info("Message received from chat: {} (type: {})",
                    message.getChatId(), message.getChat().getType());

            if (message.getChat().isChannelChat()) {
                handleChannelMessage(message);
            } else if (message.getChat().isGroupChat() || message.getChat().isSuperGroupChat()) {
                handleGroupMessage(message);
            } else {
                log.debug("Message is not from channel or group (type: {}), ignoring",
                        message.getChat().getType());
            }
        } else {
            log.debug("Update has no message, ignoring");
        }
    }

    private void handleChannelMessage(Message message) {
        log.info("This is a CHANNEL message");
        String channelUserName = message.getChat().getUserName();
        Long chatId = message.getChatId();

        log.info("Channel username: {}, chatId: {}", channelUserName, chatId);

        String channelId = channelUserName != null ? "@" + channelUserName : chatId.toString();
        log.info("Looking for channel in DB: {}", channelId);

        channelService.isChannelActive(channelId)
                .flatMap(isActive -> {
                    log.info("Channel active status: {}", isActive);
                    if (!isActive) {
                        log.warn("Channel {} is not active or not registered, ignoring", channelId);
                        return Mono.empty();
                    }
                    return processMessage(message, channelId);
                })
                .subscribe();
    }

    private void handleGroupMessage(Message message) {
        log.info("This is a GROUP message");
        String groupUserName = message.getChat().getUserName();
        String groupId = groupUserName != null ? "@" + groupUserName : message.getChatId().toString();
        processMessage(message, groupId).subscribe();
    }

    private Mono<Void> processMessage(Message message, String sourceId) {
        log.info("Processing message: {}", message.getMessageId());

        Mono<Void> text = Mono.empty();
        if (message.hasText()) {
            log.info("Text message: {}", message.getText());
            text = postSaveService.saveTextPost(
                    sourceId,
                    message.getMessageId().longValue(),
                    message.getDate(),
                    message.getText()
            ).then();
        }

        Mono<Void> photo = Mono.empty();
        if (message.hasPhoto()) {
            log.info("Photo message with caption: {}", message.getCaption());
            photo = savePhotoMessage(message, sourceId);
        }

        return Mono.whenDelayError(text, photo);
    }

    private Mono<Void> savePhotoMessage(Message message, String sourceId) {
        List<PhotoSize> photos = message.getPhoto();
        if (photos == null || photos.isEmpty()) {
            log.warn("Photo list is empty for message {}", message.getMessageId());
            return Mono.empty();
        }

        PhotoSize bestPhoto = photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize, Comparator.nullsLast(Integer::compareTo)))
                .orElse(photos.getLast());

        String fileId = bestPhoto.getFileId();
        String fileUniqueId = bestPhoto.getFileUniqueId();

        return getTelegramFile(fileId)
                .flatMap(telegramFile -> {
                    String filePath = telegramFile.getFilePath();
                    String extension = extractExtension(filePath);
                    String fileName = "photo_" + message.getMessageId() + "." + extension;

                    return downloadTelegramFile(telegramFile)
                            .flatMap(data -> postSaveService.saveMediaPost(
                                    sourceId,
                                    message.getMessageId().longValue(),
                                    message.getDate(),
                                    fileUniqueId,
                                    data,
                                    fileName,
                                    message.getCaption()))
                            .then();
                })
                .onErrorResume(e -> {
                    log.error("Failed to process photo for message {}", message.getMessageId(), e);
                    return Mono.empty();
                });
    }

    private Mono<org.telegram.telegrambots.meta.api.objects.File> getTelegramFile(String fileId) {
        return Mono.fromCallable(() -> execute(new GetFile(fileId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<byte[]> downloadTelegramFile(org.telegram.telegrambots.meta.api.objects.File telegramFile) {
        return Mono.fromCallable(() -> downloadTelegramFileBlocking(telegramFile))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private byte[] downloadTelegramFileBlocking(org.telegram.telegrambots.meta.api.objects.File telegramFile)
            throws IOException, TelegramApiException {
        try (InputStream inputStream = downloadFileAsStream(telegramFile);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String extractExtension(String filePath) {
        if (filePath == null) {
            return "jpg";
        }
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filePath.length() - 1) {
            return "jpg";
        }
        return filePath.substring(lastDot + 1);
    }
}

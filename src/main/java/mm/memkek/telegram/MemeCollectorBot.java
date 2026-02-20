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
    public String getBotToken() {
        return config.getToken();
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

        boolean isActive = channelService.isChannelActive(channelId);
        log.info("Channel active status: {}", isActive);

        if (!isActive) {
            log.warn("Channel {} is not active or not registered, ignoring", channelId);
            return;
        }

        processMessage(message, channelId);
    }

    private void handleGroupMessage(Message message) {
        log.info("This is a GROUP message");
        String groupUserName = message.getChat().getUserName();
        String groupId = groupUserName != null ? "@" + groupUserName : message.getChatId().toString();
        processMessage(message, groupId);
    }

    private void processMessage(Message message, String sourceId) {
        log.info("Processing channel message: {}", message.getMessageId());

        if (message.hasText()) {
            log.info("Text message: {}", message.getText());
            postSaveService.saveTextPost(
                    sourceId,
                    message.getMessageId().longValue(),
                    message.getDate(),
                    message.getText()
            );
        }

        if (message.hasPhoto()) {
            log.info("Photo message with caption: {}", message.getCaption());
            savePhotoMessage(message, sourceId);
        }

        if (message.hasDocument()) {
            log.info("Document message: {}", message.getDocument().getFileName());
            saveDocumentMessage(message, sourceId);
        }
    }

    private void savePhotoMessage(Message message, String sourceId) {
        List<PhotoSize> photos = message.getPhoto();
        if (photos == null || photos.isEmpty()) {
            log.warn("Photo list is empty for message {}", message.getMessageId());
            return;
        }

        PhotoSize bestPhoto = photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize, Comparator.nullsLast(Integer::compareTo)))
                .orElse(photos.get(photos.size() - 1));

        String fileId = bestPhoto.getFileId();
        String fileUniqueId = bestPhoto.getFileUniqueId();
        try {
            org.telegram.telegrambots.meta.api.objects.File telegramFile =
                    execute(new GetFile(fileId));
            String filePath = telegramFile.getFilePath();
            String extension = extractExtension(filePath, "jpg");
            String fileName = "photo_" + message.getMessageId() + "." + extension;

            byte[] data = downloadTelegramFile(telegramFile);
            postSaveService.saveMediaPost(
                    sourceId,
                    message.getMessageId().longValue(),
                    message.getDate(),
                    fileId,
                    fileUniqueId,
                    data,
                    fileName,
                    message.getCaption()
            );
        } catch (TelegramApiException e) {
            log.error("Failed to fetch photo file info for message {}", message.getMessageId(), e);
        } catch (IOException e) {
            log.error("Failed to download photo for message {}", message.getMessageId(), e);
        }
    }

    private void saveDocumentMessage(Message message, String sourceId) {
        String fileId = message.getDocument().getFileId();
        String fileUniqueId = message.getDocument().getFileUniqueId();
        String fileName = message.getDocument().getFileName();
        try {
            org.telegram.telegrambots.meta.api.objects.File telegramFile =
                    execute(new GetFile(fileId));
            byte[] data = downloadTelegramFile(telegramFile);
            postSaveService.saveMediaPost(
                    sourceId,
                    message.getMessageId().longValue(),
                    message.getDate(),
                    fileId,
                    fileUniqueId,
                    data,
                    fileName,
                    message.getCaption()
            );
        } catch (TelegramApiException e) {
            log.error("Failed to fetch document file info for message {}", message.getMessageId(), e);
        } catch (IOException e) {
            log.error("Failed to download document for message {}", message.getMessageId(), e);
        }
    }

    private byte[] downloadTelegramFile(org.telegram.telegrambots.meta.api.objects.File telegramFile)
            throws IOException, TelegramApiException {
        try (InputStream inputStream = downloadFileAsStream(telegramFile);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String extractExtension(String filePath, String fallback) {
        if (filePath == null) {
            return fallback;
        }
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filePath.length() - 1) {
            return fallback;
        }
        return filePath.substring(lastDot + 1);
    }
}

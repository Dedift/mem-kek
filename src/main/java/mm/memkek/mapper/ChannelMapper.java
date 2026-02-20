package mm.memkek.mapper;

import mm.memkek.dao.entity.Channel;
import mm.memkek.dto.response.ChannelResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface ChannelMapper {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Mapping(target = "createdAt", expression = "java(formatCreatedAt(channel.getCreatedAt()))")
    ChannelResponse toResponse(Channel channel);

    default String formatCreatedAt(LocalDateTime createdAt) {
        return createdAt != null ? createdAt.format(FORMATTER) : null;
    }
}
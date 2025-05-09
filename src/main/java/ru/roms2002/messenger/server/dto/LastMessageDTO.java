package ru.roms2002.messenger.server.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.roms2002.messenger.server.utils.enums.MessageTypeEnum;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LastMessageDTO {

	private MessageTypeEnum type;

	private String text;

	private boolean seen;

	@JsonProperty("created_at")
	private Date createdAt;
}

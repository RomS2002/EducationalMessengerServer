package ru.roms2002.messenger.server.dto.ws;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.roms2002.messenger.server.utils.enums.MessageTypeEnum;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessagePayload {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String message;

	@JsonProperty("chat_id")
	private int chatId;

	@JsonProperty("user_id")
	private int userId;

	@JsonProperty("chat_name")
	private String chatName;

	private MessageTypeEnum type;

	@JsonProperty("created_at")
	private Date createdAt;
}

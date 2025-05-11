package ru.roms2002.messenger.server.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.roms2002.messenger.server.utils.enums.MessageTypeEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

	private int id;

	@JsonProperty("sender_id")
	private int senderId;

	@JsonProperty("chat_id")
	private int chatId;

	private MessageTypeEnum type;

	@JsonProperty("created_at")
	private Date createdAt;

	private String message;

	private String filename;

	@JsonProperty("sender_name")
	private String senderName;

	@JsonProperty("chat_name")
	private String chatName;

	private boolean seen;

	@JsonProperty("seen_by_me")
	private boolean seenByMe;
}

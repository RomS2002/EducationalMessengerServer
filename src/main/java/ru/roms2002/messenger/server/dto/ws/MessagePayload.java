package ru.roms2002.messenger.server.dto.ws;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class MessagePayload implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String message;

	@JsonProperty("chat_id")
	private int chatId;

	@JsonProperty("sender_id")
	private int senderId;

	@JsonProperty("sender_name")
	private String senderName;

	@JsonProperty("chat_name")
	private String chatName;

	private MessageTypeEnum type;

	private String filename;

	@JsonProperty("created_at")
	private Date createdAt;

	private boolean seen;

	@JsonProperty("seen_by_me")
	private boolean seenByMe;
}

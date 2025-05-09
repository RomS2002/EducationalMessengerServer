package ru.roms2002.messenger.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.roms2002.messenger.server.utils.enums.ChatTypeEnum;
import ru.roms2002.messenger.server.utils.enums.RoleEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatDTO {

	private int id;

	private String name;

	private ChatTypeEnum type;

	@JsonProperty("last_message")
	private LastMessageDTO lastMessage;

	@JsonProperty("message_from")
	private String messageFrom;

	@JsonProperty("user_type")
	private RoleEnum userType;

	@JsonProperty("not_read")
	private int notRead;

	@JsonProperty("user_id")
	private int userId;
}

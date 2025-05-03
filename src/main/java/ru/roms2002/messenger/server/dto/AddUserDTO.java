package ru.roms2002.messenger.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddUserDTO {

	@JsonProperty("user_id")
	private int userId;

	@JsonProperty("chat_id")
	private int chatId;
}

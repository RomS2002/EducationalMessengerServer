package ru.roms2002.messenger.server.dto.ws;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditMessagePayload {

	@JsonProperty("message_id")
	private int messageId;

	private String message;
}

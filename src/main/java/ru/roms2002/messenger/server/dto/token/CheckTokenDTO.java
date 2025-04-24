package ru.roms2002.messenger.server.dto.token;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckTokenDTO {

	private String token;
	
	@JsonProperty("last_name")
	private String lastName;
}

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
public class AuthUserDTO {

	@JsonProperty("reg_token")
	private String regToken;

	private String email;

	@JsonProperty("last_name")
	private String lastName;

	private String password;
}

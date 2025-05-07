package ru.roms2002.messenger.server.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChangeDepartmentDTO {

	@JsonProperty("user_id")
	private int userId;

	private String department;
}

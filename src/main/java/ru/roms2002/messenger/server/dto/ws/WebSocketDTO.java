package ru.roms2002.messenger.server.dto.ws;

import java.io.Serializable;

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
public class WebSocketDTO implements Serializable {

	private String type;

	private Object payload;
}

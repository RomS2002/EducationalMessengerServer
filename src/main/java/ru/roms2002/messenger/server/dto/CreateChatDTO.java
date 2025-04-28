package ru.roms2002.messenger.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.roms2002.messenger.server.utils.enums.ChatTypeEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatDTO {

	private String name;

	private ChatTypeEnum type;
}

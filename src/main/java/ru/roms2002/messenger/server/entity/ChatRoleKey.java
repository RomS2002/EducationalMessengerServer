package ru.roms2002.messenger.server.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoleKey implements Serializable {

	@Column(name = "chat_id")
	private int chatId;

	@Column(name = "user_id")
	private int userId;

	@Override
	public int hashCode() {
		return Objects.hash(chatId, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		ChatRoleKey groupRoleKey = (ChatRoleKey) obj;
		return chatId == groupRoleKey.chatId && userId == groupRoleKey.userId;
	}
}

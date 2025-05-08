package ru.roms2002.messenger.server.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_chat")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserChatEntity implements Serializable {

	@EmbeddedId
	private UserChatKey id;

	@ManyToOne(cascade = {})
	@MapsId("chatId")
	@JoinColumn(name = "chat_id")
	private ChatEntity chat;

	@ManyToOne(cascade = {})
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private UserEntity user;

	@Column(name = "is_admin")
	private boolean isAdmin;

	public UserChatEntity(UserEntity user, ChatEntity chat, boolean isAdmin) {
		this.id = new UserChatKey(chat.getId(), user.getId());
		this.user = user;
		this.chat = chat;
		this.isAdmin = isAdmin;
	}

	@Override
	public int hashCode() {
		return Objects.hash(chat, id, isAdmin, user);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserChatEntity other = (UserChatEntity) obj;
		return Objects.equals(chat, other.chat) && Objects.equals(user, other.user);
	}
}

package ru.roms2002.messenger.server.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_chat")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChatEntity implements Serializable {

	@EmbeddedId
	private UserChatKey id;

	@ManyToOne
	@MapsId("chatId")
	@JoinColumn(name = "chat_id")
	private ChatEntity chat;

	@ManyToOne
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private UserEntity user;

	@Column(name = "is_admin")
	private boolean isAdmin;
}

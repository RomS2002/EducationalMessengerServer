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
}

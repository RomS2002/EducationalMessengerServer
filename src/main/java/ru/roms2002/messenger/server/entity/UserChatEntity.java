package ru.roms2002.messenger.server.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_chat")
@IdClass(ChatRoleKey.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChatEntity implements Serializable {

	@Id
	private int chatId;

	@Id
	private int userId;

	@ManyToOne
	@MapsId("chatId")
	@JoinColumn(name = "chat_id")
	private ChatEntity chats;

	@ManyToOne
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private UserEntity users;

	@Column(name = "admin")
	private boolean isAdmin;
}

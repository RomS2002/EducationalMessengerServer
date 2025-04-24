package ru.roms2002.messenger.server.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.roms2002.messenger.server.utils.enums.MessageTypeEnum;

@Entity
@Table(name = "message")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageEntity {

	public MessageEntity(int userId, int groupId, MessageTypeEnum type, String message) {
		this.userId = userId;
		this.chatId = groupId;
		this.type = type;
		this.message = message;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String message;

	@Column(name = "chat_id")
	private int chatId;

	@Column(name = "user_id")
	private int userId;

	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private MessageTypeEnum type;

	@Column(name = "created_at")
	@CreationTimestamp
	private Timestamp createdAt;
}

package ru.roms2002.messenger.server.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.roms2002.messenger.server.utils.enums.MessageTypeEnum;

@Entity
@Table(name = "message")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageEntity {

	public MessageEntity(UserEntity user, ChatEntity chat, MessageTypeEnum type, String message) {
		this.user = user;
		this.chat = chat;
		this.type = type;
		this.message = message;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String message;

	@ManyToOne
	@JoinColumn(name = "chat_id")
	@JsonIgnore
	private ChatEntity chat;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonIgnore
	private UserEntity user;

	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private MessageTypeEnum type;

	@Column(name = "created_at")
	@CreationTimestamp
	private Timestamp createdAt;
}

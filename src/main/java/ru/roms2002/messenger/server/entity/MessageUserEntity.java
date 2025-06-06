package ru.roms2002.messenger.server.entity;

import java.io.Serializable;

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
@Table(name = "message_user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageUserEntity implements Serializable {

	@EmbeddedId
	private MessageUserKey id;

	@ManyToOne(cascade = {})
	@MapsId("messageId")
	@JoinColumn(name = "message_id")
	private MessageEntity message;

	@ManyToOne(cascade = {})
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private UserEntity user;

	private boolean seen;
}

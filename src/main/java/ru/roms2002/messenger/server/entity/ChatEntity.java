package ru.roms2002.messenger.server.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.roms2002.messenger.server.utils.enums.ChatTypeEnum;

@Entity
@Table(name = "chat")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "name")
	private String name;

	@Enumerated(value = EnumType.STRING)
	private ChatTypeEnum type;

	@Column(name = "created_at")
	@CreationTimestamp
	private Timestamp createdAt;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_chat", joinColumns = @JoinColumn(name = "chat_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
	@JsonIgnore
	private Set<UserEntity> users = new HashSet<>();

	@OneToMany(mappedBy = "chatId")
	@JsonIgnore
	private Set<UserChatEntity> userChats = new HashSet<>();
}

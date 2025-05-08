package ru.roms2002.messenger.server.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "file_storage")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int Id;

	@OneToOne(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "message_id")
	@JsonIgnore
	private MessageEntity message;

	private String filename;

	private String url;

	@Column(name = "created_at")
	@CreationTimestamp
	private Timestamp createdAt;
}

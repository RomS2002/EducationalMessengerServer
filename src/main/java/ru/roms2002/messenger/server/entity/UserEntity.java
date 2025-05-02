package ru.roms2002.messenger.server.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.roms2002.messenger.server.utils.enums.RoleEnum;

@Entity
@Table(name = "user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity implements UserDetails, Serializable {

	public UserEntity(int andinpanelId, String password, String regToken, String email,
			RoleEnum role) {

		this.adminpanelId = andinpanelId;
		this.password = password;
		this.regToken = regToken;
		this.email = email;
		this.role = role;
	}

	@Id
	private int id;

	@Column(name = "adminpanel_id")
	private int adminpanelId;

	private String password;

	@Column(name = "reg_token")
	private String regToken;

	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "users")
	@JsonIgnore
	private List<ChatEntity> chats = new CopyOnWriteArrayList<>();

	private String email;

	@Column(name = "role")
	@Enumerated(EnumType.STRING)
	private RoleEnum role;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return new ArrayList<>();
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserEntity other = (UserEntity) obj;
		return Objects.equals(email, other.email);
	}
}
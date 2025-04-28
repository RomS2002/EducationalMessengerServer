package ru.roms2002.messenger.server.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.roms2002.messenger.server.utils.enums.RoleEnum;

@Entity
@Table(name = "user")
@Data
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

	@ManyToMany(mappedBy = "users")
	private Set<ChatEntity> chats = new HashSet<>();

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
}
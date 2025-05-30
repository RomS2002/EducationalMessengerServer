package ru.roms2002.messenger.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ru.roms2002.messenger.server.entity.UserEntity;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserService userService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity user = userService.findByEmail(username);
		if (user == null) {
			return null;
		}
		return new org.springframework.security.core.userdetails.User(user.getUsername(),
				user.getPassword(), user.getAuthorities());
	}
}

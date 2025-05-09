package ru.roms2002.messenger.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import ru.roms2002.messenger.server.dto.AuthUserDTO;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.service.UserService;
import ru.roms2002.messenger.server.utils.enums.LoginUserStatus;
import ru.roms2002.messenger.server.utils.enums.RegisterUserStatus;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private UserService userService;

	@SuppressWarnings("incomplete-switch")
	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@RequestBody AuthUserDTO userDto,
			HttpServletResponse response) {
		RegisterUserStatus status = userService.registerUser(userDto);

		switch (status) {
		case SERVER_ERROR:
			return new ResponseEntity<>("Ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
		case TOKEN_ALREADY_USED:
			return new ResponseEntity<>("Токен уже был использован", HttpStatus.FORBIDDEN);
		case TOKEN_NOT_EXISTS:
			return new ResponseEntity<>("Токен не существует", HttpStatus.FORBIDDEN);
		case BLOCKED:
			return new ResponseEntity<>("Аккаунт заблокирован", HttpStatus.FORBIDDEN);
		case EXPIRED:
			return new ResponseEntity<>("Срок действия аккаунта истек", HttpStatus.FORBIDDEN);
		case NOT_ACTIVE:
			return new ResponseEntity<>("Аккаунт ещё не активен", HttpStatus.FORBIDDEN);
		case EMAIL_ALREADY_USED:
			return new ResponseEntity<>("Email уже был использован", HttpStatus.FORBIDDEN);
		}

		UserEntity user = userService.findByEmail(userDto.getEmail());

		Cookie jwtAuthToken = userService.generateJwtCookie(user);
		response.addCookie(jwtAuthToken);

		return new ResponseEntity<>("OK", HttpStatus.OK);
	}

	@SuppressWarnings("incomplete-switch")
	@PostMapping
	public ResponseEntity<String> login(@RequestBody AuthUserDTO userDto,
			HttpServletResponse response) {
		LoginUserStatus status = userService.loginUser(userDto);

		switch (status) {
		case SERVER_ERROR:
			return new ResponseEntity<>("Ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
		case INCORRECT_EMAIL:
			return new ResponseEntity<>("Пользователь с указанным email не существует",
					HttpStatus.FORBIDDEN);
		case INCORRECT_PASSWORD:
			return new ResponseEntity<>("Неверный пароль", HttpStatus.FORBIDDEN);
		case BLOCKED:
			return new ResponseEntity<>("Аккаунт заблокирован", HttpStatus.FORBIDDEN);
		case EXPIRED:
			return new ResponseEntity<>("Срок действия аккаунта истек", HttpStatus.FORBIDDEN);
		case NOT_ACTIVE:
			return new ResponseEntity<>("Аккаунт ещё не активен", HttpStatus.FORBIDDEN);
		}

		UserEntity user = userService.findByEmail(userDto.getEmail());

		Cookie jwtAuthToken = userService.generateJwtCookie(user);
		response.addCookie(jwtAuthToken);

		return new ResponseEntity<>("OK", HttpStatus.OK);
	}

	@PostMapping(value = "/logout")
	public ResponseEntity<?> fetchInformation(HttpServletResponse response) {
		Cookie cookie = new Cookie("JWT", null);
		cookie.setHttpOnly(true);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
		return ResponseEntity.ok().build();
	}
}

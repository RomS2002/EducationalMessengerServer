package ru.roms2002.messenger.server.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import jakarta.servlet.http.Cookie;
import ru.roms2002.messenger.server.dto.AuthUserDTO;
import ru.roms2002.messenger.server.dto.ChatDTO;
import ru.roms2002.messenger.server.dto.LastMessageDTO;
import ru.roms2002.messenger.server.dto.UserDetailsDTO;
import ru.roms2002.messenger.server.dto.UserInListDTO;
import ru.roms2002.messenger.server.dto.token.CheckTokenDTO;
import ru.roms2002.messenger.server.dto.token.TokenStatus;
import ru.roms2002.messenger.server.entity.ChatEntity;
import ru.roms2002.messenger.server.entity.MessageEntity;
import ru.roms2002.messenger.server.entity.UserChatEntity;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.repository.UserRepository;
import ru.roms2002.messenger.server.utils.JwtUtil;
import ru.roms2002.messenger.server.utils.StaticVariable;
import ru.roms2002.messenger.server.utils.enums.ChatTypeEnum;
import ru.roms2002.messenger.server.utils.enums.LoginUserStatus;
import ru.roms2002.messenger.server.utils.enums.MessageTypeEnum;
import ru.roms2002.messenger.server.utils.enums.RegisterUserStatus;
import ru.roms2002.messenger.server.utils.enums.RoleEnum;

@Service
public class UserService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private InfoServerService infoServerService;

	@Autowired
	private JwtUtil jwtTokenUtil;

	private Map<String, List<WebSocketSession>> wsSessions = new ConcurrentHashMap<>();

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private ChatService chatService;

	@Autowired
	private MessageService messageService;

	@Autowired
	private MessageUserService messageUserService;

	public UserEntity getCurrentUser() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return findByEmail(username);
	}

	public boolean haveChatWith(UserEntity curUser, UserEntity user) {
		ChatEntity chat = getChatWith(curUser, user);
		if (chat == null)
			return false;
		return true;
	}

	public ChatEntity getChatWith(UserEntity curUser, UserEntity user) {
		for (ChatEntity chat : curUser.getUserChats().stream().map(cu -> cu.getChat()).toList())
			if (chat.getUserChats().contains(new UserChatEntity(user, chat, false))
					&& chat.getType() == ChatTypeEnum.SINGLE)
				return chat;
		return null;
	}

	public boolean uploadAvatar(MultipartFile image) {
		try {
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			int id = findByEmail(username).getId();
			int indexOfPoint = image.getOriginalFilename().lastIndexOf(".");
			String ext = image.getOriginalFilename().substring(indexOfPoint);
			if (!ext.equals(".jpg")) {
				return false;
			}
			Path path = Path.of("./uploads/avatar");
			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}
			Path filepath = path.resolve(id + ext);
			Files.write(filepath, image.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public List<UserInListDTO> findUsers(String lastName) {
		List<UserInListDTO> users = infoServerService.getUsersByLastName(lastName);
		Iterator<UserInListDTO> it = users.iterator();
		while (it.hasNext()) {
			UserInListDTO user = it.next();
			Optional<UserEntity> userEntity = userRepository.findByAdminpanelId(user.getId());
			if (userEntity.isEmpty())
				it.remove();
			else
				user.setId(userEntity.get().getId());
		}
		return users;
	}

	public UserDetailsDTO getUserInfo(int id) {
		UserEntity user = userRepository.findById(id).get();
		UserDetailsDTO userDetails = infoServerService
				.getUserDetailsByAdminpanelId(user.getAdminpanelId());
		userDetails.setId(user.getId());
		userDetails.setEmail(user.getEmail());
		userDetails.setOnline(checkOnline(user));
		return userDetails;
	}

	public List<ChatDTO> getChatList() {
		UserEntity user = getCurrentUser();

		List<ChatDTO> resultList = new ArrayList<>();
		for (ChatEntity chat : user.getUserChats().stream().map(uc -> uc.getChat()).toList()) {
			ChatDTO chatDTO = new ChatDTO();
			chatDTO.setId(chat.getId());
			chatDTO.setType(chat.getType());
			if (chat.getType() == ChatTypeEnum.SINGLE) {
				UserEntity secondUser = chatService.getSecondUserInSingleChat(chat, user);
				chatDTO.setName(infoServerService.getFullName(secondUser));
				chatDTO.setUserType(secondUser.getRole());
			} else {
				chatDTO.setName(chat.getName());
			}

			MessageEntity message = messageService.getLastMessageInChat(chat.getId());
			if (message == null)
				chatDTO.setLastMessage(null);
			else {
				LastMessageDTO lastMessageDto = new LastMessageDTO();
				lastMessageDto.setCreatedAt(message.getCreatedAt());
				lastMessageDto.setType(message.getType());
				if (message.getType() == MessageTypeEnum.TEXT) {
					lastMessageDto.setText(message.getMessage());
				} else {
					lastMessageDto.setText("Файл: " + message.getFile().getFilename());
				}

				lastMessageDto.setSeen(messageUserService.isMessageSeen(message));
				chatDTO.setLastMessage(lastMessageDto);
			}

			if (chatDTO.getLastMessage() != null) {
				if (message.getUser().equals(user)) {
					chatDTO.setMessageFrom("Вы:");
				} else if (chatDTO.getType() == ChatTypeEnum.SINGLE) {
					chatDTO.setMessageFrom("");
				} else {
					UserDetailsDTO userDetailsDTO = infoServerService
							.getUserDetailsByAdminpanelId(message.getUser().getAdminpanelId());
					chatDTO.setMessageFrom(userDetailsDTO.getFirstName() + ":");
				}
			}
			chatDTO.setNotRead(
					messageUserService.getNotReadByUserInChat(user.getId(), chat.getId()));
			resultList.add(chatDTO);
		}

		resultList.sort((chat1, chat2) -> {
			Date date1 = null, date2 = null;

			if (chat1.getLastMessage() == null && chat2.getLastMessage() == null) {
				date1 = chatService.findById(chat1.getId()).getCreatedAt();
				date2 = chatService.findById(chat2.getId()).getCreatedAt();
			} else if (chat1.getLastMessage() == null) {
				date1 = chatService.findById(chat1.getId()).getCreatedAt();
				date2 = chat2.getLastMessage().getCreatedAt();
			} else if (chat2.getLastMessage() == null) {
				date1 = chat1.getLastMessage().getCreatedAt();
				date2 = chatService.findById(chat2.getId()).getCreatedAt();
			} else {
				date1 = chat1.getLastMessage().getCreatedAt();
				date2 = chat2.getLastMessage().getCreatedAt();
			}

			return date2.compareTo(date1);
		});

		return resultList;

	}

	public RegisterUserStatus registerUser(AuthUserDTO userDto) {
		try {
			CheckTokenDTO checkTokenDTO = new CheckTokenDTO(userDto.getRegToken(),
					userDto.getLastName());
			TokenStatus tokenStatus = infoServerService.checkToken(checkTokenDTO);

			if (tokenStatus.getTokenStatus().equals("ALREADY USED"))
				return RegisterUserStatus.TOKEN_ALREADY_USED;
			if (tokenStatus.getTokenStatus().equals("NOT EXISTS"))
				return RegisterUserStatus.TOKEN_NOT_EXISTS;

			if (!userRepository.findByEmail(userDto.getEmail()).isEmpty())
				return RegisterUserStatus.EMAIL_ALREADY_USED;

			String encodedPassword = passwordEncoder.encode(userDto.getPassword());

			int adminpanelID = infoServerService.getIdByToken(userDto.getRegToken());

			UserDetailsDTO userDetails = infoServerService
					.getUserDetailsByAdminpanelId(adminpanelID);
			RoleEnum role = (userDetails.getRole().equals("Студент")) ? RoleEnum.STUDENT
					: RoleEnum.PROFESSOR;

			if (userDetails.getIsBlocked())
				return RegisterUserStatus.BLOCKED;
			if (userDetails.getEnabledFrom().after(new Date()))
				return RegisterUserStatus.NOT_ACTIVE;
			if (userDetails.getEnabledUntil().before(new Date()))
				return RegisterUserStatus.EXPIRED;

			UserEntity newUser = new UserEntity(adminpanelID, encodedPassword,
					userDto.getRegToken(), userDto.getEmail(), role);
			newUser = userRepository.save(newUser);

			if (newUser.getRole() == RoleEnum.STUDENT)
				chatService.addUserToStudgroup(newUser, userDetails.getGroupName());
			if (newUser.getRole() == RoleEnum.PROFESSOR)
				chatService.addUserToDepartmentChat(newUser, userDetails.getDepartment());

			return RegisterUserStatus.OK;
		} catch (Exception e) {
			e.printStackTrace();
			return RegisterUserStatus.SERVER_ERROR;
		}
	}

	public LoginUserStatus loginUser(AuthUserDTO userDto) {
		try {
			List<UserEntity> resultList = userRepository.findByEmail(userDto.getEmail());
			if (resultList.isEmpty())
				return LoginUserStatus.INCORRECT_EMAIL;

			UserEntity user = resultList.get(0);
			if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword()))
				return LoginUserStatus.INCORRECT_PASSWORD;

			UserDetailsDTO userDetails = infoServerService
					.getUserDetailsByAdminpanelId(user.getAdminpanelId());

			if (userDetails.getIsBlocked())
				return LoginUserStatus.BLOCKED;
			if (userDetails.getEnabledFrom().after(new Date()))
				return LoginUserStatus.NOT_ACTIVE;
			if (userDetails.getEnabledUntil().before(new Date()))
				return LoginUserStatus.EXPIRED;

			return LoginUserStatus.OK;
		} catch (Exception e) {
			return LoginUserStatus.SERVER_ERROR;
		}
	}

	public UserEntity findByEmail(String email) {
		List<UserEntity> tmp = userRepository.findByEmail(email);
		if (!tmp.isEmpty()) {
			return userRepository.findByEmail(email).get(0);
		}
		return null;
	}

	public UserEntity findById(int id) {
		Optional<UserEntity> tmp = userRepository.findById(id);
		if (tmp.isPresent()) {
			return tmp.get();
		}
		return null;
	}

	public Cookie generateJwtCookie(UserEntity user) {
		String jwtToken = jwtTokenUtil.generateToken(user);

		Cookie jwtAuthToken = new Cookie("JWT", jwtToken);
		jwtAuthToken.setHttpOnly(true);
		jwtAuthToken.setSecure(false);
		jwtAuthToken.setPath("/");
		jwtAuthToken.setMaxAge(7 * 24 * 60 * 60);

		return jwtAuthToken;
	}

	public Map<String, List<WebSocketSession>> getWsSessions() {
		return wsSessions;
	}

	public void setWsSessions(Map<String, List<WebSocketSession>> wsSessions) {
		this.wsSessions = wsSessions;
	}

	public void authenticateUser(String jwtToken) {
		String username = jwtTokenUtil.getUserNameFromJwtToken(jwtToken);
		try {
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(auth);
				return;
			}
		} catch (UsernameNotFoundException e) {

		}
		SecurityContextHolder.clearContext();
	}

	public UserEntity findByAdminpanelId(int id) {
		Optional<UserEntity> tmp = userRepository.findByAdminpanelId(id);
		if (tmp.isPresent()) {
			return tmp.get();
		}
		return null;
	}

	public UserEntity save(UserEntity user) {
		return userRepository.save(user);
	}

	public void updateOnline(UserEntity user) {
		user.setLastActionTime(new Date());
		save(user);
	}

	public boolean checkOnline(UserEntity user) {
		Date lastActionTime = user.getLastActionTime();
		if (lastActionTime == null)
			return false;
		long timeDiff = new Date().getTime() - lastActionTime.getTime();
		return timeDiff < StaticVariable.SECONDS_SAVE_ONLINE_STATUS * 1000;
	}

	public void destroyAllWsSessions(String username) {
		if (getWsSessions().get(username) == null)
			return;

		for (WebSocketSession session : getWsSessions().get(username)) {
			try {
				session.close(CloseStatus.NOT_ACCEPTABLE);
			} catch (IOException e) {

			}
		}
	}

	public void deleteById(int userId) {
		userRepository.deleteById(userId);
	}
}

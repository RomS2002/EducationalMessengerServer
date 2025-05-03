package ru.roms2002.messenger.server.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.repository.UserRepository;
import ru.roms2002.messenger.server.utils.JwtUtil;
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

	private Map<Integer, String> wsSessions = new ConcurrentHashMap<>();

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private ChatService chatService;

	@Autowired
	private MessageService messageService;

//	@Autowired
//	private UserChatJoinService groupUserJoinService;

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
		for (ChatEntity chat : curUser.getChats())
			if (chat.getUsers().contains(user) && chat.getType() == ChatTypeEnum.SINGLE)
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
			Path path = Paths.get("./uploads/avatar");
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
			UserEntity userEntity = userRepository.findByAdminpanelId(user.getId());
			if (userEntity == null)
				it.remove();
			else
				user.setId(userEntity.getId());
		}
		return users;
	}

	public UserDetailsDTO getUserInfo(int id) {
		UserEntity user = userRepository.findById(id).get();
		UserDetailsDTO userDetails = infoServerService
				.getUserDetailsByAdminpanelId(user.getAdminpanelId());
		userDetails.setId(user.getId());
		userDetails.setEmail(user.getEmail());
		return userDetails;
	}

	public List<ChatDTO> getChatList() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		UserEntity user = findByEmail(username);

		List<ChatDTO> resultList = new ArrayList<>();
		for (ChatEntity chat : user.getChats()) {
			ChatDTO chatDTO = new ChatDTO();
			chatDTO.setId(chat.getId());
			chatDTO.setType(chat.getType());
			if (chat.getType() == ChatTypeEnum.SINGLE) {
				UserEntity secondUser = chatService.getSecondUserInSingleChat(chat, user);
				chatDTO.setName(infoServerService.getFullName(secondUser));
			} else {
				chatDTO.setName(chat.getName());
			}

			MessageEntity message = messageService.getLastMessageInChat(chat.getId());
			LastMessageDTO messageDto = new LastMessageDTO();
			messageDto.setCreatedAt(message.getCreatedAt());
			messageDto.setType(message.getType());
			if (message.getType() == MessageTypeEnum.TEXT) {
				messageDto.setText(message.getMessage());
			} else {
				messageDto.setText("Файл: " + message.getFile().getFilename());
			}

			// Заглушка
			messageDto.setSeen(false);
			chatDTO.setLastMessage(messageDto);

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

			userRepository.save(newUser);
			return RegisterUserStatus.OK;
		} catch (Exception e) {
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
		if (!tmp.isEmpty()) {
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

	public Map<Integer, String> getWsSessions() {
		return wsSessions;
	}

	public void setWsSessions(Map<Integer, String> wsSessions) {
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
				System.out.println(auth.isAuthenticated());
				return;
			}
		} catch (UsernameNotFoundException e) {

		}
		SecurityContextHolder.clearContext();
	}

//
//	public void deleteAll() {
//		userRepository.deleteAll();
//	}
//
//	public void flush() {
//		userRepository.flush();
//	}
//
//	public List<UserEntity> findAll() {
//		return userRepository.findAll();
//	}
//
//	@Transactional
//	public void save(UserEntity userEntity) {
//		userRepository.save(userEntity);
//	}
//
//	public List<GroupMemberDTO> fetchAllUsers(int[] ids) {
//		List<GroupMemberDTO> toSend = new ArrayList<>();
//		List<UserEntity> list = userRepository.getAllUsersNotAlreadyInConversation(ids);
//		list.forEach(user -> toSend.add(
//				new GroupMemberDTO(user.getId(), user.getFirstName(), user.getLastName(), false)));
//		return toSend;
//	}
//
//
//
//	public UserEntity findByNameOrEmail(String str0, String str1) {
//		return userRepository.getUserByFirstNameOrMail(str0, str1);
//	}
//
//	public boolean checkIfUserIsAdmin(int userId, int groupIdToCheck) {
//		ChatRoleKey id = new ChatRoleKey(groupIdToCheck, userId);
//		Optional<UserChat> optional = groupUserJoinService.findById(id);
//		if (optional.isPresent()) {
//			UserChat groupUser = optional.get();
//			return groupUser.getRole() == 1;
//		}
//		return false;
//	}
//
//	public String createShortUrl(String firstName, String lastName) {
//		StringBuilder sb = new StringBuilder();
//		sb.append(firstName);
//		sb.append(".");
//		sb.append(Normalizer.normalize(lastName.toLowerCase(), Normalizer.Form.NFD));
//		boolean isShortUrlAvailable = true;
//		int counter = 0;
//		while (isShortUrlAvailable) {
//			sb.append(counter);
//			if (userRepository.countAllByShortUrl(sb.toString()) == 0) {
//				isShortUrlAvailable = false;
//			}
//			counter++;
//		}
//		return sb.toString();
//	}
//
//	public String findUsernameById(int id) {
//		return userRepository.getUsernameByUserId(id);
//	}
//
//	public String findFirstNameById(int id) {
//		return userRepository.getFirstNameByUserId(id);
//	}
//
//	public UserEntity findById(int id) {
//		return userRepository.findById(id).orElse(null);
//	}
//
//	public String passwordEncoder(String str) {
//		return passwordEncoder.encode(str);
//	}
//
//	public boolean checkIfUserNameOrMailAlreadyUsed(String firstName, String mail) {
//		return userRepository.countAllByFirstNameOrMail(firstName, mail) > 0;
//	}
}

package ru.roms2002.messenger.server.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import ru.roms2002.messenger.server.dto.AuthUserDTO;
import ru.roms2002.messenger.server.dto.UserDetailsDTO;
import ru.roms2002.messenger.server.dto.token.CheckTokenDTO;
import ru.roms2002.messenger.server.dto.token.TokenStatus;
import ru.roms2002.messenger.server.entity.UserEntity;
import ru.roms2002.messenger.server.repository.UserRepository;
import ru.roms2002.messenger.server.utils.JwtUtil;
import ru.roms2002.messenger.server.utils.enums.LoginUserStatus;
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

//	@Autowired
//	private GroupUserJoinService groupUserJoinService;
//
//	private Map<Integer, String> wsSessions = new HashMap<>();

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

			UserDetailsDTO userDetails = infoServerService.getUserDetailsById(adminpanelID);
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
					.getUserDetailsById(user.getAdminpanelId());

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
		return userRepository.findByEmail(email).get(0);
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

//	public Map<Integer, String> getWsSessions() {
//		return wsSessions;
//	}
//
//	public void setWsSessions(Map<Integer, String> wsSessions) {
//		this.wsSessions = wsSessions;
//	}
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
//	public String findUsernameWithWsToken(String token) {
//		return userRepository.getUsernameWithWsToken(token);
//	}
//
//	public int findUserIdWithToken(String token) {
//		return userRepository.getUserIdWithWsToken(token);
//	}
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

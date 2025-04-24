package ru.roms2002.messenger.server.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(allowCredentials = "true", origins = "http://localhost:3000")
public class AuthenticationController {

//    private final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
//
//    @Autowired
//    private JwtUtil jwtTokenUtil;
//
//    @Autowired
//    private CustomUserDetailsService userDetailsService;
//
//    @Autowired
//    private UserMapper userMapper;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private GroupService groupService;
//
//    @Autowired
//    private GroupMapper groupMapper;
//
//    @PostMapping(value = "/auth")
//    public AuthUserDTO2 createAuthenticationToken(@RequestBody JwtDTO authenticationRequest, HttpServletResponse response) throws Exception {
//        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
//        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
//        UserEntity user = userService.findByNameOrEmail(authenticationRequest.getUsername(), authenticationRequest.getUsername());
//        String token = jwtTokenUtil.generateToken(userDetails);
//        Cookie jwtAuthToken = new Cookie(StaticVariable.SECURE_COOKIE, token);
//        jwtAuthToken.setHttpOnly(true);
//        jwtAuthToken.setSecure(false);
//        jwtAuthToken.setPath("/");
////        cookie.setDomain("http://localhost");
////         7 days
//        jwtAuthToken.setMaxAge(7 * 24 * 60 * 60);
//        response.addCookie(jwtAuthToken);
//        log.debug("User authenticated successfully");
//        return userMapper.toLightUserDTO(user);
//    }
//
//    @GetMapping(value = "/logout")
//    public ResponseEntity<?> fetchInformation(HttpServletResponse response) {
//        Cookie cookie = new Cookie(StaticVariable.SECURE_COOKIE, null);
//        cookie.setHttpOnly(true);
//        cookie.setSecure(false);
//        cookie.setPath("/");
//        cookie.setMaxAge(0);
//        response.addCookie(cookie);
//        return ResponseEntity.ok().build();
//    }
//
//	@GetMapping(value = "/csrf")
//	public CsrfToken getCsrfToken(CsrfToken token) {
//		return token;
//	}
//
//    @GetMapping(value = "/fetch")
//    public InitUserDTO fetchInformation(HttpServletRequest request) {
//        return userMapper.toUserDTO(getUserEntity(request));
//    }
//
//    private void authenticate(String username, String password) throws Exception {
//        try {
//            //authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
//        } catch (DisabledException e) {
//            throw new Exception("USER_DISABLED", e);
//        } catch (BadCredentialsException e) {
//            throw new Exception("INVALID_CREDENTIALS", e);
//        }
//    }
//
//    @PostMapping(value = "/create")
//    public GroupDTO createGroupChat(HttpServletRequest request, @RequestBody String payload) {
//        UserEntity user = getUserEntity(request);
//        Gson gson = new Gson();
//        GroupDTO groupDTO = gson.fromJson(payload, GroupDTO.class);
//        ChatEntity groupEntity = groupService.createGroup(user.getId(), groupDTO.getName());
//        return groupMapper.toGroupDTO(groupEntity, user.getId());
//    }
//
//    private UserEntity getUserEntity(HttpServletRequest request) {
//        String username;
//        String jwtToken;
//        UserEntity user = new UserEntity();
//        Cookie cookie = WebUtils.getCookie(request, StaticVariable.SECURE_COOKIE);
//        if (cookie != null) {
//            jwtToken = cookie.getValue();
//            username = jwtTokenUtil.getUserNameFromJwtToken(jwtToken);
//            user = userService.findByNameOrEmail(username, username);
//        }
//        return user;
//    }
}
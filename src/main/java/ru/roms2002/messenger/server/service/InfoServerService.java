package ru.roms2002.messenger.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import ru.roms2002.messenger.server.dto.UserDetailsDTO;
import ru.roms2002.messenger.server.dto.token.CheckTokenDTO;
import ru.roms2002.messenger.server.dto.token.TokenStatus;

@Service
public class InfoServerService {

	@Value("${infoserver.url}")
	private String infoserverURI;

	private final RestClient restClient;

	public InfoServerService(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.build();
	}

	public TokenStatus checkToken(CheckTokenDTO checkTokenDTO) {
//		ResponseSpec response = restClient.post().uri(infoserverURI + "/checktoken")
//				.contentType(MediaType.APPLICATION_JSON).body(checkTokenDTO).retrieve();
//		System.out.println(response.body(String.class));
//		return response.body(TokenStatus.class);
		return restClient.post().uri(infoserverURI + "/checktoken")
				.contentType(MediaType.APPLICATION_JSON).body(checkTokenDTO).retrieve()
				.body(TokenStatus.class);
	}

	public Integer getIdByToken(String token) {
		return restClient.post().uri(infoserverURI + "/getId").contentType(MediaType.TEXT_PLAIN)
				.body(token).retrieve().body(Integer.class);
	}

	public UserDetailsDTO getUserDetailsById(Integer id) {
		return restClient.get().uri(infoserverURI + "/getUserDetails?id={id}", id).retrieve()
				.body(UserDetailsDTO.class);
	}
}

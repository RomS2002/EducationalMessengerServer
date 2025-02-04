package ru.roms2002.messenger.server.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InitUserDTO {

    private UserDTO user;

    private List<GroupWrapperDTO> groupsWrapper;
}

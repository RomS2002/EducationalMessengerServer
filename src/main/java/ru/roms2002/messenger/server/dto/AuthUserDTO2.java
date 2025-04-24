package ru.roms2002.messenger.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.roms2002.messenger.server.dto.user.GroupDTO;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserDTO2 {

    private int id;

    private String username;

    private String firstGroupUrl;

    private String wsToken;

    private List<GroupDTO> groups;
}

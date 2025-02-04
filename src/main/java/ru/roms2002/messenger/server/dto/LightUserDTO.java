package ru.roms2002.messenger.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LightUserDTO {

    public int id;

    public String firstName;

    public String lastName;

    private int groupRole;

    private String wsToken;
}

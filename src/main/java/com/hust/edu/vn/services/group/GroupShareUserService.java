package com.hust.edu.vn.services.group;

import com.hust.edu.vn.dto.UserDto;

import java.util.List;

public interface GroupShareUserService {
    boolean inviteMemberGroup(Long groupId, List<String> emailUsers, String link);

    List<UserDto> getMembersGroup(Long groupId);

    boolean deleteMembersGroup(Long groupId, String username);

    boolean changePositionMemberGroup(Long groupId, String username);
}

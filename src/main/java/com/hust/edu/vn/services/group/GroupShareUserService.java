package com.hust.edu.vn.services.group;

import com.hust.edu.vn.dto.MemberGroupDto;
import com.hust.edu.vn.dto.TokenInviteGroupDto;

import java.util.List;

public interface GroupShareUserService {
    boolean inviteMemberGroup(Long groupId, List<String> emailUsers);

    List<MemberGroupDto> getMembersGroup(Long groupId);

    boolean deleteMembersGroup(Long groupId, String username);

    boolean changePositionMemberGroup(Long groupId, String username);

    boolean acceptInviteMember(Long groupId);

    int getPermissionGroup(Long groupId);

    List<TokenInviteGroupDto> getPendingInvites(Long groupId);

    boolean inviteResendMemberGroup(Long groupId, String emailUser);

    boolean cancelInviteMember(Long groupId, String emailUser);

    boolean leaveGroup(Long groupId, String emailUser);

    List<TokenInviteGroupDto> getAllPendingInvitations();

    boolean declineInviteMember(Long groupId);
}

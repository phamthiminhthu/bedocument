package com.hust.edu.vn.controller.group;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.MemberGroupDto;
import com.hust.edu.vn.dto.TokenInviteGroupDto;
import com.hust.edu.vn.services.group.GroupShareUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/management/group")
public class GroupShareUserController {
    private final GroupShareUserService groupShareUserService;

    public GroupShareUserController(GroupShareUserService groupShareUserService) {
        this.groupShareUserService = groupShareUserService;
    }

    // todo: waiting ~~ invite member, return list members accepted, send mail ~~ resend invitation
    @PostMapping("{groupId}/invite")
    public ResponseEntity<CustomResponse> inviteMember(@PathVariable(value="groupId") Long groupId, @RequestBody List<String> emailUsers){
        boolean status = groupShareUserService.inviteMemberGroup(groupId, emailUsers);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Invite Member successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Invite Member Failed");
    }

    @PostMapping("{groupId}/invite/resend")
    public ResponseEntity<CustomResponse> inviteResendMember(@PathVariable(value="groupId") Long groupId, @ModelAttribute(value="email") String emailUser){
        boolean status = groupShareUserService.inviteResendMemberGroup(groupId, emailUser);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Invite Member successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Invite Member Failed");
    }


    // todo: accept invite join group
    @PostMapping("{groupId}/accept/invite")
    public ResponseEntity<CustomResponse> acceptInviteMember(@PathVariable(value="groupId") Long groupId){
        boolean status = groupShareUserService.acceptInviteMember(groupId);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Accept invite successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Accept invite Failed");
    }

    @PostMapping("{groupId}/invite/cancel")
    public ResponseEntity<CustomResponse> cancelInviteMember(@PathVariable(value="groupId") Long groupId, @ModelAttribute(value="email") String emailUser){
        boolean status = groupShareUserService.cancelInviteMember(groupId, emailUser);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Cancel invite successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Cancel invite Failed");
    }

    @PostMapping("{groupId}/invite/decline")
    public ResponseEntity<CustomResponse> cancelInviteMember(@PathVariable(value="groupId") Long groupId){
        boolean status = groupShareUserService.declineInviteMember(groupId);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Decline invite successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Decline invite Failed");
    }

    @GetMapping("{groupId}/permission")
    public ResponseEntity<CustomResponse> getPermissionGroup(@PathVariable(value="groupId") Long groupId){
        int status = groupShareUserService.getPermissionGroup(groupId);
        if(status == 2){
            return CustomResponse.generateResponse(HttpStatus.OK, "Accept Invite", status);
        }
        if(status == 1){
            return CustomResponse.generateResponse(HttpStatus.OK, "You are a member", status);
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Can not access group", status);
    }

    @GetMapping("{groupId}/pending")
    public ResponseEntity<CustomResponse> getPendingInvites(@PathVariable(value="groupId") Long groupId){
        List<TokenInviteGroupDto> users = groupShareUserService.getPendingInvites(groupId);
        if(users == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Can not access group");
        }
        if(users.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Pending invites", users);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "No pending invites", users);
    }

    @GetMapping("invitations/pending")
    public ResponseEntity<CustomResponse> getAllPendingInvitations(){
        List<TokenInviteGroupDto> invitations = groupShareUserService.getAllPendingInvitations();
        if(invitations == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Can not access group");
        }
        if(invitations.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Pending invites", invitations);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "No pending invites", invitations);
    }



    // todo: check ~~ show all members in group
    @GetMapping("{groupId}/members/show")
    public ResponseEntity<CustomResponse> getMembersGroup(@PathVariable(value="groupId") Long groupId){
        List<MemberGroupDto> userDtosList = groupShareUserService.getMembersGroup(groupId);
        if(userDtosList != null && !userDtosList.isEmpty()){
            return CustomResponse.generateResponse(HttpStatus.OK, "No member in group", userDtosList);
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Cannot access group");
    }

    // todo: check ~~ delete member if u are owner
    @PostMapping("{groupId}/member/delete")
    public ResponseEntity<CustomResponse> deleteMemberGroup(@PathVariable(value="groupId") Long groupId, @ModelAttribute(value="username") String username){
        boolean status = groupShareUserService.deleteMembersGroup(groupId, username);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete members successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Delete members Failed");
    }

    // todo: check ~~ change position for member -> owner if u are owner
    @PostMapping("{groupId}/member/position/change")
    public ResponseEntity<CustomResponse> changePositionMemberGroup(@PathVariable(value="groupId") Long groupId, @RequestParam(value="username") String username){
        boolean status = groupShareUserService.changePositionMemberGroup(groupId, username);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Change position members successfully");
        }

        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Can not change position");
    }

    @PostMapping("{groupId}/leave")
    public ResponseEntity<CustomResponse> leaveGroup(@PathVariable(value="groupId") Long groupId, @ModelAttribute(value="email") String emailUser){
        boolean status = groupShareUserService.leaveGroup(groupId, emailUser);
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Cancel invite successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Cancel invite Failed");
    }



}

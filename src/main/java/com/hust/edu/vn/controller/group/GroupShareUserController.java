package com.hust.edu.vn.controller.group;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.services.group.GroupShareUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/management/group/{groupId}")
public class GroupShareUserController {
    private final GroupShareUserService groupShareUserService;

    public GroupShareUserController(GroupShareUserService groupShareUserService) {
        this.groupShareUserService = groupShareUserService;
    }

    // todo: waiting ~~ invite member, return list members accepted, send mail ~~ resend invitation
    @PostMapping("invite")
    public ResponseEntity<CustomResponse> inviteMember(@PathVariable(value="groupId") Long groupId, @RequestBody List<String> emailUsers, HttpServletRequest httpServletRequest){
        boolean status = groupShareUserService.inviteMemberGroup(groupId, emailUsers, applicationUrl(httpServletRequest));
        if (status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Invite Member successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Invite Member Failed");
    }

    // todo: check ~~ show all members in group
    @GetMapping("members/show")
    public ResponseEntity<CustomResponse> getMembersGroup(@PathVariable(value="groupId") Long groupId){
        List<UserDto> userDtosList = groupShareUserService.getMembersGroup(groupId);
        if(userDtosList != null && !userDtosList.isEmpty()){
            return CustomResponse.generateResponse(HttpStatus.OK, "No member in group", userDtosList);
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Cannot access group");
    }

    // todo: check ~~ delete member if u are owner
    @PostMapping("member/delete")
    public ResponseEntity<CustomResponse> deleteMemberGroup(@PathVariable(value="groupId") Long groupId, @ModelAttribute(value="username") String username){
        boolean status = groupShareUserService.deleteMembersGroup(groupId, username);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete members successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Delete members Failed");
    }

    // todo: check ~~ change position for member -> owner if u are owner
    @PostMapping("member/position/change")
    public ResponseEntity<CustomResponse> changePositionMemberGroup(@PathVariable(value="groupId") Long groupId, @RequestParam(value="username") String username){
        boolean status = groupShareUserService.changePositionMemberGroup(groupId, username);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Change position members successfully");
        }

        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Can not change position");
    }

    // todo: accept invite join group
    @PostMapping("member/invite/accept")
    public ResponseEntity<CustomResponse> acceptInviteMember(@PathVariable(value="groupId") Long groupId){
        return CustomResponse.generateResponse(HttpStatus.OK, "Accept members successfully");
    }


    private String applicationUrl(HttpServletRequest servletRequest ) {
        return "https://"
                + servletRequest.getServerName()
                + ":"
                + servletRequest.getServerPort()
                + servletRequest.getContextPath();
    }

}

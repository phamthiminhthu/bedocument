package com.hust.edu.vn.controller.group;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.GroupDocDto;
import com.hust.edu.vn.services.group.GroupDocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management/group")
@Slf4j
public class GroupController {

    // todo: checking delete waiting
    private final GroupDocService groupDocService;

    public GroupController(GroupDocService groupDocService) {
        this.groupDocService = groupDocService;
    }

    @PostMapping("create")
    public ResponseEntity<CustomResponse> createGroup(@ModelAttribute(value="groupName") String groupName){
        boolean status = groupDocService.createGroup(groupName);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Create group successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Create group Failed");
    }
    @GetMapping("owner/show/all")
    public ResponseEntity<CustomResponse> showAllGroupOwner(){
        List<GroupDocDto> groupDocDtoList = groupDocService.showAllGroup();
        if(groupDocDtoList != null  && !groupDocDtoList.isEmpty()){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all groups successfully", groupDocDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Group is empty", groupDocDtoList);
    }

    @GetMapping("member/show/all")
    public ResponseEntity<CustomResponse> showAllGroupMember(){
        List<GroupDocDto> groupDocDtoList = groupDocService.showAllGroupMember();
        if(groupDocDtoList != null && !groupDocDtoList.isEmpty()){
            return CustomResponse.generateResponse(HttpStatus.FOUND, "All Your group", groupDocDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Group is empty");
    }
    @GetMapping("show/details/{groupId}")
    public ResponseEntity<CustomResponse> showGroupByGroupId(@PathVariable(value="groupId") Long groupId){
        log.info("showGroupByGroupId");
        GroupDocDto groupDocDto = groupDocService.showGroupByGroupId(groupId);
        if(groupDocDto != null){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show detail group by id successfully", groupDocDto);
        }
        return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Group id doesn't existed");
    }

//    @GetMapping("member/show/details/{groupId}")
//    public ResponseEntity<CustomResponse> showGroupMemberByGroupId(@PathVariable(value="groupId") Long groupId){
//        GroupDocDto groupDocDto = groupDocService.showGroupMemberByGroupId(groupId);
//        if(groupDocDto != null){
//            return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Group existed", groupDocDto);
//        }
//        return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Group id doesn't existed");
//
//    }

    @PostMapping("update/{groupId}")
    public ResponseEntity<CustomResponse> updateGroupByGroupId(@PathVariable(value="groupId") Long groupId, @ModelAttribute(value = "groupName") String groupName ){
        boolean status = groupDocService.updateGroupByGroupId(groupId, groupName);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update information group successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update information group Failed");
    }

    //todo: check ~~ delete group by id ~~ chi co owner moi xoa dc
    @PostMapping("delete/{groupId}")
    public ResponseEntity<CustomResponse> deleteGroupByGroupId(@PathVariable(value="groupId") Long groupId){
        boolean status = groupDocService.deleteGroupByGroupId(groupId);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete group successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Delete group Failed");
    }



}

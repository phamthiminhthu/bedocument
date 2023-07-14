package com.hust.edu.vn.controller.group;

import com.hust.edu.vn.common.type.CustomResponse;
import com.hust.edu.vn.dto.GroupDocDto;
import com.hust.edu.vn.services.group.GroupDocService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management/group")
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
        List<GroupDocDto> groupDocDtoList = groupDocService.showAllGroupByOwner();
        if(groupDocDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if(groupDocDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all groups successfully", groupDocDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Empty", groupDocDtoList);
    }

    @GetMapping("member/show/all")
    public ResponseEntity<CustomResponse> showAllGroupMember(){
        List<GroupDocDto> groupDocDtoList = groupDocService.showAllGroupMember();
        if(groupDocDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if(groupDocDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all groups successfully", groupDocDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Empty", groupDocDtoList);
    }
    @GetMapping("show/details/{groupId}")
    public ResponseEntity<CustomResponse> showGroupByGroupId(@PathVariable(value="groupId") Long groupId){
        GroupDocDto groupDocDto = groupDocService.showGroupByGroupId(groupId);
        if(groupDocDto != null){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show detail group by id successfully", groupDocDto);
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Group id doesn't existed");
    }

    @GetMapping("show/name/by/{groupId}")
    public ResponseEntity<CustomResponse> showGroupNameById(@PathVariable(value="groupId") Long groupId){
        String name = groupDocService.showGroupNameById(groupId);
        if(name != null){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show groupName by id successfully", name);
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Group id doesn't existed");
    }

    @GetMapping("show/all")
    public ResponseEntity<CustomResponse> showAllGroups(){
        List<GroupDocDto> groupDocDtoList = groupDocService.getALLGroups();
        if(groupDocDtoList == null){
            return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Access denied");
        }
        if(groupDocDtoList.size() > 0){
            return CustomResponse.generateResponse(HttpStatus.OK, "Show all list groups", groupDocDtoList);
        }
        return CustomResponse.generateResponse(HttpStatus.OK, "Empty", groupDocDtoList);
    }

    @PostMapping("update/{groupId}")
    public ResponseEntity<CustomResponse> updateGroupByGroupId(@PathVariable(value="groupId") Long groupId, @ModelAttribute(value = "groupName") String groupName ){
        boolean status = groupDocService.updateGroupByGroupId(groupId, groupName);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Update information group successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.BAD_REQUEST, "Update information group Failed");
    }

    //todo: check ~~ delete group by id ~~ chi co owner moi xoa dc
    @Transactional
    @PostMapping("delete/{groupId}")
    public ResponseEntity<CustomResponse> deleteGroupByGroupId(@PathVariable(value="groupId") Long groupId){
        boolean status = groupDocService.deleteGroupByGroupId(groupId);
        if(status){
            return CustomResponse.generateResponse(HttpStatus.OK, "Delete group successfully");
        }
        return CustomResponse.generateResponse(HttpStatus.NOT_FOUND, "Delete group Failed");
    }

}

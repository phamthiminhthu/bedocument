package com.hust.edu.vn.services.group;

import com.hust.edu.vn.dto.GroupDocDto;

import java.util.List;

public interface GroupDocService {
    boolean createGroup(String groupDoc);

    GroupDocDto showGroupByGroupId(Long groupId);

    boolean updateGroupByGroupId(Long groupId, String groupName);

    boolean deleteGroupByGroupId(Long groupId);

    List<GroupDocDto> getALLGroups();

    String showGroupNameById(Long groupId);

}

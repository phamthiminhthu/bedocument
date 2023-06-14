package com.hust.edu.vn.services.impl.user;

import com.hust.edu.vn.dto.UserDto;
import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.model.UserModel;
import com.hust.edu.vn.repository.UserRepository;
import com.hust.edu.vn.services.user.UserService;
import com.hust.edu.vn.utils.AwsS3Utils;
import com.hust.edu.vn.utils.BaseUtils;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapperUtils modelMapperUtils;
    private final AwsS3Utils awsS3Utils;

    private final BaseUtils baseUtils;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ModelMapperUtils modelMapperUtils, AwsS3Utils awsS3Utils, BaseUtils baseUtils) {
        this.userRepository = userRepository;
        this.modelMapperUtils = modelMapperUtils;
        this.awsS3Utils = awsS3Utils;
        this.baseUtils = baseUtils;
    }

    @Override
    public UserDto getInfoProfile(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null){
            return modelMapperUtils.mapAllProperties(user, UserDto.class);
        }
        return null;
    }

    @Override
    public boolean updateProfile(UserModel userModel) {
        if(userModel.getEmail() == null || userModel.getUsername() == null) return false;
        User user = baseUtils.getUser();
        if(user != null){
            User updateUser = modelMapperUtils.mapAllProperties(userModel, User.class);
            if(userModel.getAvatar() == null) updateUser.setImage(user.getImage());
            else {
                if(user.getImage() == null) {
                    String url = awsS3Utils.uploadAvatar(userModel.getAvatar(), getRootPath() + "avatar/");
                    updateUser.setImage(url);
                }
                else{
                    awsS3Utils.deleteAvatarFromS3Bucket(user.getImage());
                    String url = awsS3Utils.uploadAvatar(userModel.getAvatar(), getRootPath() + "avatar/");
                    updateUser.setImage(url);
                }
            }
            updateUser.setPassword(user.getPassword());
            updateUser.setId(user.getId());
            updateUser.setRootPath(user.getRootPath());
            updateUser.setUpdatedAt(new Date());
            userRepository.save(updateUser);
            return true;
        }
        return false;

    }
    public String getRootPath() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        return user.getRootPath();
    }
}

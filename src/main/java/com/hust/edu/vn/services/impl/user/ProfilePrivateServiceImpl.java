package com.hust.edu.vn.services.impl.user;

import com.hust.edu.vn.entity.User;
import com.hust.edu.vn.model.ProfilePrivateModel;
import com.hust.edu.vn.repository.UserRepository;
import com.hust.edu.vn.services.user.ProfilePrivateService;
import com.hust.edu.vn.utils.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@Slf4j
public class ProfilePrivateServiceImpl implements ProfilePrivateService {
    private final UserRepository userRepository;
    private final ModelMapperUtils modelMapperUtils;

    @Autowired
    public ProfilePrivateServiceImpl(UserRepository userRepository, ModelMapperUtils modelMapperUtils) {
        this.userRepository = userRepository;
        this.modelMapperUtils = modelMapperUtils;
    }

    @Override
    public ProfilePrivateModel getInfoProfile(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null){
            return null;
        }
        return modelMapperUtils.mapAllProperties(user, ProfilePrivateModel.class);
    }

    @Override
    public boolean updateProfile(Long id, ProfilePrivateModel profilePrivateModel) {
        if(profilePrivateModel.getEmail() == null || profilePrivateModel.getUsername() == null) return false;
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return false;
        User updateUser = modelMapperUtils.mapAllProperties(profilePrivateModel, User.class);
        updateUser.setPassword(user.getPassword());
        updateUser.setId(user.getId());
        updateUser.setRootPath(user.getRootPath());
        updateUser.setUpdatedAt(new Date());
        userRepository.save(updateUser);
        return true;
    }
}
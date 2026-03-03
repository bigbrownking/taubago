package org.app.courseapp.service;

import org.app.courseapp.dto.request.ChangePasswordRequest;
import org.app.courseapp.dto.request.RegisterCuratorRequest;
import org.app.courseapp.dto.request.RegisterSpecialistRequest;
import org.app.courseapp.dto.request.UpdateProfileRequest;
import org.app.courseapp.dto.response.userProfile.BaseUserProfileDto;
import org.app.courseapp.model.users.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    User getCurrentUser();
    BaseUserProfileDto getMyProfile();
    BaseUserProfileDto getUserProfile(String email);
    BaseUserProfileDto updateMyProfile(UpdateProfileRequest request, MultipartFile photo) throws IOException;    void deactivateMyAccount();
    BaseUserProfileDto registerCurator(RegisterCuratorRequest request);
    BaseUserProfileDto registerSpecialist(RegisterSpecialistRequest request);
    void changeMyPassword(ChangePasswordRequest request);
}
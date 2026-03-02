package org.app.courseapp.service;

import org.app.courseapp.dto.request.ChangePasswordRequest;
import org.app.courseapp.dto.request.RegisterCuratorRequest;
import org.app.courseapp.dto.request.RegisterSpecialistRequest;
import org.app.courseapp.dto.request.UpdateProfileRequest;
import org.app.courseapp.dto.response.userProfile.BaseUserProfileDto;
import org.app.courseapp.model.users.User;

public interface UserService {
    User getCurrentUser();
    BaseUserProfileDto getMyProfile();
    BaseUserProfileDto getUserProfile(String email);
    BaseUserProfileDto updateMyProfile(UpdateProfileRequest request);
    void deactivateMyAccount();
    BaseUserProfileDto registerCurator(RegisterCuratorRequest request);
    BaseUserProfileDto registerSpecialist(RegisterSpecialistRequest request);
    void changeMyPassword(ChangePasswordRequest request);
}
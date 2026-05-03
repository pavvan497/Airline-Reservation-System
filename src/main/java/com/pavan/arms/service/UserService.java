package com.pavan.arms.service;

import com.pavan.arms.User.Role;
import com.pavan.arms.dto.AuthenticationRequest;
import com.pavan.arms.entity.User;
import com.pavan.arms.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private  UserRepository userRepository;

    public void save(User u){userRepository.save(u);}
    
    public List<User> getAllUsers() {
        List<User> users =userRepository.findAll();
        ResponseEntity.ok();
        return users;
    }

    public boolean checkUserRoleIsAdmin(AuthenticationRequest request) {
        User admin = userRepository.findByEmail(request.getEmail()).orElseThrow();
        if(admin.getRole().toString().equals("ROLE_ADMIN")){
            return true;
        }else{
            return false;
        }
    }
}

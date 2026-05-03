package com.pavan.arms.controller;

import com.pavan.arms.dto.AuthenticationRequest;
import com.pavan.arms.dto.AuthenticationResponse;
import com.pavan.arms.dto.RegisterRequest;
import com.pavan.arms.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;


    @GetMapping("/login")
    public String login(){
        return "User/login";
    }

    @GetMapping("/registration")
    public String registration() {
        return "User/registration";}


    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody RegisterRequest request){
        AuthenticationResponse response = service.registerUser(request);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(response);
    }



  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
    AuthenticationResponse response = service.authenticate(request);
      if(response !=null) {
          return ResponseEntity.ok(response);
      }
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
}


}

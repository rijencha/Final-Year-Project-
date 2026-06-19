package com.example.photoGroupe.controller.photographer;

import com.example.photoGroupe.model.event.EventType;
import com.example.photoGroupe.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/specializations")
@RequiredArgsConstructor
public class PhotographerController {

}
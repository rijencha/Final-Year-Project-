package com.example.photoGroupe.controller.user;

import com.example.photoGroupe.dto.pins.SharePinResponse;
import com.example.photoGroupe.dto.share.ShareResponse;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.album.AlbumService;
import com.example.photoGroupe.service.event.EventRequestService;
import com.example.photoGroupe.service.upload.PinsService;
import com.example.photoGroupe.service.user.UserService;
import com.example.photoGroupe.service.workshop.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/share")
@RequiredArgsConstructor
public class ShareController {

    private final PinsService pinsService;
    private final WorkshopService workshopService;
    private final AlbumService albumService;
    private final EventRequestService eventRequestService;
    private final UserService userService;

    @PostMapping("/pin/{id}")
    public SharePinResponse sharePin(@PathVariable Long id,
                                     @AuthenticationPrincipal CustomUserDetails currentUser) {
        return pinsService.sharePin(id, currentUser.getUser().getId());
    }

    @PostMapping("/workshop/{id}")
    public ShareResponse shareWorkshop(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        return workshopService.shareWorkshop(id, currentUser);
    }

    @PostMapping("/album/{id}")
    public ShareResponse shareAlbum(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        return albumService.shareAlbum(id, currentUser.getUser().getId());
    }

    @PostMapping("/event/{id}")
    public ShareResponse shareEvent(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        return eventRequestService.shareEvent(id, currentUser.getUser());
    }

    @PostMapping("/profile/{id}")
    public ShareResponse shareProfile(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetails currentUser) {
        return userService.shareProfile(id, currentUser.getUser());
    }
}
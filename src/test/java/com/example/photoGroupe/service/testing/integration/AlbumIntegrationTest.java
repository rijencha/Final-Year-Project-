package com.example.photoGroupe.service.testing.integration;

import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.repo.pins.AlbumRepository;
import com.example.photoGroupe.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlbumIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired AlbumRepository albumRepository;
    @Autowired UserRepository userRepository;

    private User owner;

    @BeforeEach
    void setUp() {
        albumRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User("Test Photographer", "photographer@test.com", "hashed", "testphotographer", Role.PHOTOGRAPHER);
        owner = userRepository.save(owner);
    }

    @Test
    void createAlbum_persistsToDatabase() throws Exception {
        mockMvc.perform(multipart("/api/users/albums")
                        .param("title", "Wedding Highlights")
                        .param("description", "Best moments from Sharma wedding")
                        .param("visibility", "PUBLIC")
                        .with(user(new CustomUserDetails(owner)))
                        .with(req -> { req.setMethod("POST"); return req; }))
                .andExpect(status().isOk());

        assertThat(albumRepository.findAll()).hasSize(1);
        assertThat(albumRepository.findAll().get(0).getTitle()).isEqualTo("Wedding Highlights");
        assertThat(albumRepository.findAll().get(0).getUser().getId()).isEqualTo(owner.getId());
    }

    @Test
    void createAlbum_withoutCoverImage_stillSucceeds() throws Exception {
        mockMvc.perform(multipart("/api/users/albums")
                        .param("title", "No Cover Album")
                        .with(user(new CustomUserDetails(owner)))
                        .with(req -> { req.setMethod("POST"); return req; }))
                .andExpect(status().isOk());

        assertThat(albumRepository.findAll()).hasSize(1);
        assertThat(albumRepository.findAll().get(0).getCoverImageUrl()).isNull();
    }
}
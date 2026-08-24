package com.example.photoGroupe.service.testing.integration;

import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.repo.BookingRepository;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired BookingRepository bookingRepository;
    @Autowired UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private User client;
    private User photographer;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        userRepository.deleteAll();

        client = new User("Test Client", "client@test.com", "hashed", "testclient", Role.USER);
        client = userRepository.save(client);

        photographer = new User("Test Photographer", "photographer@test.com", "hashed", "testphotographer", Role.PHOTOGRAPHER);
        photographer = userRepository.save(photographer);
    }

    @Test
    void createBooking_persistsToDatabase() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("photographerId", photographer.getId());
        body.put("eventTitle", "Wedding Shoot");
        body.put("eventType", "WEDDING");
        body.put("eventDate", LocalDateTime.now().plusDays(5).toString());
        body.put("location", "Patan, Lalitpur");
        body.put("price", BigDecimal.valueOf(15000));

        mockMvc.perform(post("/api/users/bookings")
                        .with(user(new CustomUserDetails(client)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        assertThat(bookingRepository.findAll()).hasSize(1);
        assertThat(bookingRepository.findAll().get(0).getEventTitle()).isEqualTo("Wedding Shoot");
    }
}
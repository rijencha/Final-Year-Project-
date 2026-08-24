package com.example.photoGroupe.service.testing;

import com.example.photoGroupe.dto.booking.BookingRequest;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.booking.*;
import com.example.photoGroupe.model.event.EventType;
import com.example.photoGroupe.repo.BookingRepository;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.repo.payment.BookingPackageRepository;
import com.example.photoGroupe.repo.payment.PaymentRepository;
import com.example.photoGroupe.repo.payment.PayoutRepository;
import com.example.photoGroupe.service.booking.BookingServiceImpl;
import com.example.photoGroupe.service.booking.PhotographerPackageService;
import com.example.photoGroupe.service.chatting.MessageServiceImpl;
import com.example.photoGroupe.service.notification.NotificationService;
import com.example.photoGroupe.service.restrict.UserRestrictionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private PayoutRepository payoutRepository;
    @Mock private BookingPackageRepository packageRepository;
    @Mock private MessageServiceImpl messageService;
    @Mock private PhotographerPackageService photographerPackageService;
    @Mock private PaymentRepository paymentRepository;
    @Mock private UserRestrictionService userRestrictionService;

    @InjectMocks private BookingServiceImpl bookingService;

    private User client;
    private User photographer;
    private BookingRequest req;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setId(1L);
        client.setFullName("Test Client");

        photographer = new User();
        photographer.setId(2L);
        photographer.setRole(Role.PHOTOGRAPHER);
        photographer.setFullName("Test Photographer");

        req = new BookingRequest();
        req.setPhotographerId(2L);
        req.setEventTitle("Wedding Shoot");
        req.setEventDate(LocalDateTime.now().plusDays(10));
        req.setPrice(new BigDecimal("5000"));
    }

    // ── create() ──────────────────────────────────────────────

    @Test
    void create_throwsWhenTargetUserIsNotPhotographer() {
        photographer.setRole(Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(photographer));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookingService.create(client, req));
        assertEquals("User is not a photographer", ex.getMessage());
    }

    @Test
    void create_throwsWhenClientIsRestricted() {
        client.setBookingRestrictedUntil(LocalDateTime.now().plusDays(3));
        when(userRepository.findById(2L)).thenReturn(Optional.of(photographer));

        assertThrows(RuntimeException.class, () -> bookingService.create(client, req));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_throwsOnDateConflict() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(photographer));
        when(bookingRepository.isPhotographerBooked(2L, req.getEventDate())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookingService.create(client, req));
        assertEquals("Photographer is already booked on this date", ex.getMessage());
    }

    @Test
    void create_throwsWhenNoEventTypeProvided() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(photographer));
        when(bookingRepository.isPhotographerBooked(2L, req.getEventDate())).thenReturn(false);
        // eventType and customEventType both left null

        assertThrows(RuntimeException.class, () -> bookingService.create(client, req));
    }

    @Test
    void create_savesAndNotifiesOnSuccess() {
        req.setEventType(EventType.WEDDING);
        when(userRepository.findById(2L)).thenReturn(Optional.of(photographer));
        when(bookingRepository.isPhotographerBooked(2L, req.getEventDate())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = bookingService.create(client, req);

        assertNotNull(response);
        verify(notificationService).create(eq(photographer), eq(client), eq("BOOKING_REQUEST"), anyString(), anyString());
    }

    // ── cancel() ──────────────────────────────────────────────

    @Test
    void cancel_throwsIfAlreadyCompleted() {
        Booking booking = bookingOwnedByClient(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(RuntimeException.class, () -> bookingService.cancel(1L, client, "changed my mind"));
    }

    @Test
    void cancel_throwsIfNotTheOwningClient() {
        Booking booking = bookingOwnedByClient(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        User someoneElse = new User();
        someoneElse.setId(999L);

        assertThrows(RuntimeException.class, () -> bookingService.cancel(1L, someoneElse, "not mine"));
    }

    @Test
    void cancel_succeedsAndSetsStatus() {
        Booking booking = bookingOwnedByClient(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancel(1L, client, "schedule conflict");

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals("schedule conflict", booking.getCancellationReason());
    }

    // ── complete() ────────────────────────────────────────────

    @Test
    void complete_throwsIfEscrowNotHeld() {
        // escrowStatus defaults to null via builder — null != HELD, so this should throw
        Booking booking = bookingOwnedByClient(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(RuntimeException.class, () -> bookingService.complete(1L, photographer));
    }

    // ── releasePayment() — escrow split math ─────────────────

    @Test
    void releasePayment_calculates12PercentCommissionCorrectly() {
        Booking booking = bookingOwnedByClient(BookingStatus.COMPLETED);
        booking.setEscrowStatus(EscrowStatus.HELD);
        booking.setPrice(new BigDecimal("10000"));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.releasePayment(1L, client);

        ArgumentCaptor<Payout> captor = ArgumentCaptor.forClass(Payout.class);
        verify(payoutRepository).save(captor.capture());

        Payout payout = captor.getValue();
        assertEquals(new BigDecimal("1200.00"), payout.getCommissionAmount());
        assertEquals(new BigDecimal("8800.00"), payout.getPhotographerAmount());
        assertEquals(EscrowStatus.RELEASE, booking.getEscrowStatus());
    }

    @Test
    void releasePayment_throwsIfSessionNotCompleted() {
        Booking booking = bookingOwnedByClient(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(RuntimeException.class, () -> bookingService.releasePayment(1L, client));
    }

    // ── helpers ───────────────────────────────────────────────

    private Booking bookingOwnedByClient(BookingStatus status) {
        return Booking.builder()
                .id(1L)
                .client(client)
                .photographer(photographer)
                .status(status)
                .eventTitle("Wedding Shoot")
                .price(new BigDecimal("5000"))
                .build();
    }
}
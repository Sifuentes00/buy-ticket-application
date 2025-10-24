package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.CacheKeys;
import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.entities.Ticket;
import com.matvey.cinema.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticket = new Ticket();
        ticket.setId(1L);
    }

    @Test
    void testFindById_TicketFoundInCache() {
        String cacheKey = CacheKeys.TICKET_PREFIX + ticket.getId();
        when(cache.get(cacheKey)).thenReturn(Optional.of(ticket));

        Optional<Ticket> foundTicket = ticketService.findById(ticket.getId());

        assertTrue(foundTicket.isPresent());
        assertEquals(ticket, foundTicket.get());
        verify(cache, times(1)).get(cacheKey);
        verify(ticketRepository, never()).findById(anyLong());
    }

    @Test
    void testFindById_TicketFoundInRepository() {
        when(cache.get(CacheKeys.TICKET_PREFIX + ticket.getId())).thenReturn(Optional.empty());
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        Optional<Ticket> foundTicket = ticketService.findById(ticket.getId());

        assertTrue(foundTicket.isPresent());
        assertEquals(ticket, foundTicket.get());
        verify(ticketRepository, times(1)).findById(ticket.getId());
        verify(cache, times(1)).put(CacheKeys.TICKET_PREFIX + ticket.getId(), ticket);
    }

    @Test
    void testFindById_TicketNotFound() {
        when(cache.get(CacheKeys.TICKET_PREFIX + ticket.getId())).thenReturn(Optional.empty());
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.empty());

        Long ticketId = ticket.getId(); // Получаем ID билета
        assertThrows(CustomNotFoundException.class, () -> ticketService.findById(ticketId));
        verify(ticketRepository, times(1)).findById(ticket.getId());
    }

    @Test
    void testFindAll_TicketsFoundInCache() {
        String cacheKey = CacheKeys.TICKETS_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(ticket)));

        List<Ticket> tickets = ticketService.findAll();

        assertEquals(1, tickets.size());
        assertEquals(ticket, tickets.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(ticketRepository, never()).findAll();
    }

    @Test
    void testFindAll_TicketsNotFoundInCache() {
        String cacheKey = CacheKeys.TICKETS_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(ticketRepository.findAll()).thenReturn(Collections.singletonList(ticket));

        List<Ticket> tickets = ticketService.findAll();

        assertEquals(1, tickets.size());
        assertEquals(ticket, tickets.get(0));
        verify(ticketRepository, times(1)).findAll();
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(ticket));
    }

    @Test
    void testSave_TicketSuccessfullySaved() {
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        Ticket savedTicket = ticketService.save(ticket);

        assertEquals(ticket, savedTicket);
        verify(ticketRepository, times(1)).save(ticket);
        verify(cache, times(1)).evict(CacheKeys.TICKETS_ALL);
        verify(cache, times(1)).evict(CacheKeys.TICKET_PREFIX + savedTicket.getId());
    }

    @Test
    void testDeleteById_TicketExists() {
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        ticketService.deleteById(ticket.getId());

        verify(ticketRepository, times(1)).deleteById(ticket.getId());
        verify(cache, times(1)).evict(CacheKeys.TICKETS_ALL);
        verify(cache, times(1)).evict(CacheKeys.TICKET_PREFIX + ticket.getId());
    }

    @Test
    void testDeleteById_TicketNotFound() {
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.empty());

        Long ticketId = ticket.getId(); // Получаем ID билета
        assertThrows(CustomNotFoundException.class, () -> ticketService.deleteById(ticketId));
        verify(ticketRepository, never()).deleteById(ticket.getId());
    }

    @Test
    void testFindTicketsByUserUsername_TicketsFoundInCache() {
        String cacheKey = CacheKeys.TICKETS_USER_PREFIX + "TestUser";
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(ticket)));

        List<Ticket> tickets = ticketService.findTicketsByUserUsername("TestUser");

        assertEquals(1, tickets.size());
        assertEquals(ticket, tickets.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(ticketRepository, never()).findTicketsByUserUsername(anyString());
    }

    @Test
    void testFindTicketsByUserUsername_TicketsNotFoundInCache() {
        String cacheKey = CacheKeys.TICKETS_USER_PREFIX + "TestUser";
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(ticketRepository.findTicketsByUserUsername("TestUser")).thenReturn(Collections.singletonList(ticket));

        List<Ticket> tickets = ticketService.findTicketsByUserUsername("TestUser");

        assertEquals(1, tickets.size());
        assertEquals(ticket, tickets.get(0));
        verify(ticketRepository, times(1)).findTicketsByUserUsername("TestUser");
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(ticket));
    }

    @Test
    void testFindTicketsByShowtimeDateTime_TicketsFoundInCache() {
        String cacheKey = CacheKeys.TICKETS_SHOWTIME_PREFIX + "2023-10-10T10:00:00";
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(ticket)));

        List<Ticket> tickets = ticketService.findTicketsByShowtimeDateTime("2023-10-10T10:00:00");

        assertEquals(1, tickets.size());
        assertEquals(ticket, tickets.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(ticketRepository, never()).findTicketsByShowtimeDateTime(anyString());
    }

    @Test
    void testFindTicketsByShowtimeDateTime_TicketsNotFoundInCache() {
        String cacheKey = CacheKeys.TICKETS_SHOWTIME_PREFIX + "2023-10-10T10:00:00";
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(ticketRepository.findTicketsByShowtimeDateTime("2023-10-10T10:00:00")).thenReturn(Collections.singletonList(ticket));

        List<Ticket> tickets = ticketService.findTicketsByShowtimeDateTime("2023-10-10T10:00:00");

        assertEquals(1, tickets.size());
        assertEquals(ticket, tickets.get(0));
        verify(ticketRepository, times(1)).findTicketsByShowtimeDateTime("2023-10-10T10:00:00");
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(ticket));
    }

    @Test
    void testFindTicketsBySeatId_TicketsFoundInCache() {
        Long seatId = 1L;
        String cacheKey = CacheKeys.TICKETS_SEAT_PREFIX + seatId;
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(ticket)));

        List<Ticket> tickets = ticketService.findTicketsBySeatId(seatId);

        assertEquals(1, tickets.size());
        assertEquals(ticket, tickets.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(ticketRepository, never()).findTicketsBySeatId(anyLong());
    }

    @Test
    void testFindTicketsBySeatId_TicketsNotFoundInCache() {
        Long seatId = 1L;
        String cacheKey = CacheKeys.TICKETS_SEAT_PREFIX + seatId;
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(ticketRepository.findTicketsBySeatId(seatId)).thenReturn(Collections.singletonList(ticket));

        List<Ticket> tickets = ticketService.findTicketsBySeatId(seatId);

        assertEquals(1, tickets.size());
        assertEquals(ticket, tickets.get(0));
        verify(ticketRepository, times(1)).findTicketsBySeatId(seatId);
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(ticket));
    }
}


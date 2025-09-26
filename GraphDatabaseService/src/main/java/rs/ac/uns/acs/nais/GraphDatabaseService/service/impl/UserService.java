package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.mapper.UserMapper;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.*;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository users;
    private final BookRepository books;
    private final UserMapper mapper;

    public Page<UserDto> list(int page, int size) {
        var p = PageRequest.of(page, size, Sort.by("name").ascending());
        return users.findAll(p).map(mapper::toDto);
    }

    public UserDto get(String id) {
        return mapper.toDto(users.findById(id).orElseThrow(() -> new NotFoundException("User not found")));
    }

    public UserDto create(UserDto d) {
        var e = mapper.toEntity(d);
        e.setRegisteredAt(e.getRegisteredAt() == null ? Instant.now() : e.getRegisteredAt());
        return mapper.toDto(users.save(e));
    }

    public UserDto update(String id, UserDto d) {
        var existing = users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        existing.setName(d.getName());
        existing.setEmail(d.getEmail());
        existing.setPreferences(d.getPreferences());
        return mapper.toDto(users.save(existing));
    }

    public void delete(String id) {
        users.deleteById(id);
    }

    // RELACIJE

    public void addRead(String uid, String bid, ReadRequest req) {
        var user = users.findById(uid).orElseThrow(() -> new NotFoundException("User not found"));
        var book = books.findById(bid).orElseThrow(() -> new NotFoundException("Book not found"));

        var rel = ReadRel.builder()
                .book(book)
                .startedAt(req.getStartedAt() != null ? req.getStartedAt() : Instant.now())
                .finishedAt(req.getFinishedAt())
                .progress(req.getProgress() != null ? req.getProgress() : 0)
                .build();

        user.getRead().add(rel);
        users.save(user);
    }

    public void updateReadProgress(String uid, String bid, Integer progress) {
        books.updateReadProgress(uid, bid, progress);
    }

    public void addRating(String uid, String bid, RateRequest req) {
        var user = users.findById(uid).orElseThrow(() -> new NotFoundException("User not found"));
        var book = books.findById(bid).orElseThrow(() -> new NotFoundException("Book not found"));

        var rel = RatedRel.builder()
                .book(book)
                .stars(req.getStars())
                .comment(req.getComment())
                .build();

        user.getRated().add(rel);
        users.save(user);
    }

    public void addToWishlist(String uid, String bid, WishRequest req) {
        var user = users.findById(uid).orElseThrow(() -> new NotFoundException("User not found"));
        var book = books.findById(bid).orElseThrow(() -> new NotFoundException("Book not found"));

        var rel = WishRel.builder()
                .book(book)
                .note(req.getNote())
                .build();

        user.getWishlist().add(rel);
        users.save(user);
    }

    public void removeFromWishlist(String uid, String bid) {
        books.removeWish(uid, bid);
    }
}

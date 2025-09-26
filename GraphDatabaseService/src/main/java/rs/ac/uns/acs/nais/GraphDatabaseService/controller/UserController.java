package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.impl.UserService;

@RestController
@RequestMapping("/api/graph/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public Page<UserDto> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable String id) { return service.get(id); }

    @PostMapping
    public UserDto create(@RequestBody @Valid UserDto dto) { return service.create(dto); }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable String id, @RequestBody @Valid UserDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { service.delete(id); }

    // RELACIJE
    @PostMapping("/{uid}/read/{bid}")
    public void addRead(@PathVariable String uid, @PathVariable String bid, @RequestBody ReadRequest req) {
        service.addRead(uid, bid, req);
    }

    @PatchMapping("/{uid}/read/{bid}/progress/{progress}")
    public void updateReadProgress(@PathVariable String uid, @PathVariable String bid, @PathVariable Integer progress) {
        service.updateReadProgress(uid, bid, progress);
    }

    @PostMapping("/{uid}/rate/{bid}")
    public void rate(@PathVariable String uid, @PathVariable String bid, @RequestBody RateRequest req) {
        service.addRating(uid, bid, req);
    }

    @PostMapping("/{uid}/wishlist/{bid}")
    public void wish(@PathVariable String uid, @PathVariable String bid, @RequestBody WishRequest req) {
        service.addToWishlist(uid, bid, req);
    }

    @DeleteMapping("/{uid}/wishlist/{bid}")
    public void removeWish(@PathVariable String uid, @PathVariable String bid) {
        service.removeFromWishlist(uid, bid);
    }
}

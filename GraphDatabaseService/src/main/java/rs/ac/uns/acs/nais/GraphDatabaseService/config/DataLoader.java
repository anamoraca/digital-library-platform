package rs.ac.uns.acs.nais.GraphDatabaseService.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.*;

import java.time.Instant;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    @Bean
    CommandLineRunner seed(AuthorRepository authors, GenreRepository genres,
                           PublisherRepository publishers, BookRepository books,
                           UserRepository users) {
        return args -> {
            if (authors.count() > 0 || users.count() > 0) return; // već popunjeno

            // Genres
            var gFantasy = genres.save(Genre.builder().id("fantasy").name("Fantasy").build());
            var gSF = genres.save(Genre.builder().id("sf").name("Science Fiction").build());
            var gDrama = genres.save(Genre.builder().id("drama").name("Drama").build());
            var gEdu = genres.save(Genre.builder().id("edu").name("Education").build());

            // Authors
            var aTolkien = authors.save(Author.builder().name("J. R. R. Tolkien").country("UK").birthYear(1892).build());
            var aAsimov  = authors.save(Author.builder().name("Isaac Asimov").country("USA").birthYear(1920).build());
            var aIvo     = authors.save(Author.builder().name("Ivo Andrić").country("Serbia").birthYear(1892).build());

            // Publishers
            var p1 = publishers.save(Publisher.builder().name("HarperCollins").country("USA").build());
            var p2 = publishers.save(Publisher.builder().name("Penguin").country("UK").build());

            // Books
            var b1 = books.save(Book.builder().title("The Hobbit").year(1937).language("en").pages(310)
                    .author(aTolkien).genres(List.of(gFantasy)).publisher(p1).build());
            var b2 = books.save(Book.builder().title("The Lord of the Rings").year(1954).language("en").pages(1178)
                    .author(aTolkien).genres(List.of(gFantasy)).publisher(p1).build());
            var b3 = books.save(Book.builder().title("Foundation").year(1951).language("en").pages(255)
                    .author(aAsimov).genres(List.of(gSF)).publisher(p2).build());
            var b4 = books.save(Book.builder().title("Na Drini ćuprija").year(1945).language("sr").pages(320)
                    .author(aIvo).genres(List.of(gDrama)).publisher(p2).build());
            var b5 = books.save(Book.builder().title("Matematika 1").year(2020).language("sr").pages(200)
                    .author(aAsimov).genres(List.of(gEdu)).publisher(p1).build());

            // Users
            var u1 = User.builder().name("Ana Morača").email("ana@example.com")
                    .preferences(new ArrayList<>(List.of("fantasy","sf"))).registeredAt(Instant.now())
                    .read(new ArrayList<>()).rated(new ArrayList<>()).wishlist(new ArrayList<>()).build();
            var u2 = User.builder().name("Anja Vujačić").email("anja@example.com")
                    .preferences(new ArrayList<>(List.of("edu","drama"))).registeredAt(Instant.now())
                    .read(new ArrayList<>()).rated(new ArrayList<>()).wishlist(new ArrayList<>()).build();

            // Relacije
            u1.getRead().add(ReadRel.builder().book(b1).startedAt(Instant.now()).progress(100).build());
            u1.getRated().add(RatedRel.builder().book(b1).stars(5).comment("Savršeno").build());
            u1.getRead().add(ReadRel.builder().book(b3).startedAt(Instant.now()).progress(80).build());
            u1.getRated().add(RatedRel.builder().book(b3).stars(4).build());
            u1.getWishlist().add(WishRel.builder().book(b2).build());

            u2.getRead().add(ReadRel.builder().book(b4).startedAt(Instant.now()).progress(100).build());
            u2.getRated().add(RatedRel.builder().book(b4).stars(5).comment("Remek-delo").build());
            u2.getWishlist().add(WishRel.builder().book(b5).build());

            users.saveAll(List.of(u1, u2));
        };
    }
}

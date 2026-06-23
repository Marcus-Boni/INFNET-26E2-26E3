package br.com.musicstreamer;

import br.com.musicstreamer.streaming.domain.Song;
import br.com.musicstreamer.streaming.domain.SongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final SongRepository songRepository;

    public DatabaseSeeder(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (songRepository.count() == 0) {
            // Seed a default song with the fixed UUID used in api-tests.http
            UUID testSongId = UUID.fromString("a5f3cbda-3ff6-455b-b9ab-76f5b9d3bdf9");
            Song song1 = new Song(testSongId, "Bohemian Rhapsody", "Queen");
            songRepository.save(song1);

            // Seed another song with the custom UUID also used in api-tests.http
            UUID testSongId2 = UUID.fromString("afcafeaa-2a7e-4b41-9f56-c9d542e30b78");
            Song song2 = new Song(testSongId2, "Imagine", "John Lennon");
            songRepository.save(song2);

            logger.info("=== Database Seeded ===");
            logger.info("Seeded Song 1: {} - ID: {}", song1.getTitle(), song1.getId());
            logger.info("Seeded Song 2: {} - ID: {}", song2.getTitle(), song2.getId());
            logger.info("=======================");
        }
    }
}

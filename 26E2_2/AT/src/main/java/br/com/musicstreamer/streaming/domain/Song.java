package br.com.musicstreamer.streaming.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Song {
    @Id
    private UUID id;
    private String title;
    private String artist;

    public Song(String title, String artist) {
        this(UUID.randomUUID(), title, artist);
    }

    public Song(UUID id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }
}

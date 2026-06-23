package br.com.musicstreamer.streaming.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Listener {
    @Id
    private UUID id;

    @ManyToMany
    @JoinTable(name = "listener_favorite_songs",
            joinColumns = @JoinColumn(name = "listener_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id"))
    private List<Song> favoriteSongs = new ArrayList<>();

    public Listener(UUID id) {
        this.id = id;
    }

    public void favoriteSong(Song song) {
        if (!favoriteSongs.contains(song)) {
            favoriteSongs.add(song);
        }
    }
    
    public void unfavoriteSong(Song song) {
        favoriteSongs.remove(song);
    }
}

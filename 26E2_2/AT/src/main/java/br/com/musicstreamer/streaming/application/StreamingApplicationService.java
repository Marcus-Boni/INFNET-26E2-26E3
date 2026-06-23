package br.com.musicstreamer.streaming.application;

import br.com.musicstreamer.streaming.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class StreamingApplicationService {
    private final ListenerRepository listenerRepository;
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    public StreamingApplicationService(ListenerRepository listenerRepository, PlaylistRepository playlistRepository, SongRepository songRepository) {
        this.listenerRepository = listenerRepository;
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
    }

    public void favoriteSong(UUID listenerId, UUID songId) {
        Listener listener = listenerRepository.findById(listenerId).orElseGet(() -> new Listener(listenerId));
        Song song = songRepository.findById(songId).orElseThrow(() -> new IllegalArgumentException("Song not found"));
        listener.favoriteSong(song);
        listenerRepository.save(listener);
    }

    public UUID createPlaylist(UUID listenerId, String name) {
        Playlist playlist = new Playlist(listenerId, name);
        return playlistRepository.save(playlist).getId();
    }

    public void addSongToPlaylist(UUID playlistId, UUID songId) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> new IllegalArgumentException("Playlist not found"));
        Song song = songRepository.findById(songId).orElseThrow(() -> new IllegalArgumentException("Song not found"));
        playlist.addSong(song);
        playlistRepository.save(playlist);
    }
}

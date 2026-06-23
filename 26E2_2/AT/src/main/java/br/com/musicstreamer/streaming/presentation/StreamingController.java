package br.com.musicstreamer.streaming.presentation;

import br.com.musicstreamer.streaming.application.StreamingApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/streaming")
public class StreamingController {
    private final StreamingApplicationService service;

    public StreamingController(StreamingApplicationService service) {
        this.service = service;
    }

    @PostMapping("/listeners/{listenerId}/favorites/{songId}")
    public ResponseEntity<Void> favoriteSong(@PathVariable UUID listenerId, @PathVariable UUID songId) {
        service.favoriteSong(listenerId, songId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/listeners/{listenerId}/playlists")
    public ResponseEntity<Void> createPlaylist(@PathVariable UUID listenerId, @RequestParam String name) {
        UUID playlistId = service.createPlaylist(listenerId, name);
        return ResponseEntity.created(URI.create("/api/streaming/playlists/" + playlistId)).build();
    }

    @PostMapping("/playlists/{playlistId}/songs/{songId}")
    public ResponseEntity<Void> addSongToPlaylist(@PathVariable UUID playlistId, @PathVariable UUID songId) {
        service.addSongToPlaylist(playlistId, songId);
        return ResponseEntity.ok().build();
    }
}

package br.com.musicstreamer.streaming.domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface SongRepository extends JpaRepository<Song, UUID> {}

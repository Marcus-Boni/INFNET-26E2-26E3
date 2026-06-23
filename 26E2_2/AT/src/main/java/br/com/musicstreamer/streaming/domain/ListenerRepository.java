package br.com.musicstreamer.streaming.domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface ListenerRepository extends JpaRepository<Listener, UUID> {}

package com.infnet.academia.service;

import com.infnet.academia.repository.AlunoRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CatracaService {

    private final StringRedisTemplate redisTemplate;
    private final AlunoRepository alunoRepository;

    private static final String KEY_PREFIX = "catraca:token:";

    public CatracaService(StringRedisTemplate redisTemplate, AlunoRepository alunoRepository) {
        this.redisTemplate = redisTemplate;
        this.alunoRepository = alunoRepository;
    }

    public String gerarTokenAcesso(Long alunoId) {
        // Validação: Verifica se o aluno existe no banco relacional
        if (!alunoRepository.existsById(alunoId)) {
            throw new IllegalArgumentException("Aluno com ID " + alunoId + " não existe.");
        }

        // Gerar token único (UUID)
        String token = UUID.randomUUID().toString();
        String key = KEY_PREFIX + token;

        // Armazenar no Redis com expiração de 5 minutos (300 segundos)
        redisTemplate.opsForValue().set(key, alunoId.toString(), 5, TimeUnit.MINUTES);

        return token;
    }

    public String obterAlunoIdDoToken(String token) {
        String key = KEY_PREFIX + token;
        return redisTemplate.opsForValue().get(key);
    }

    public boolean validarToken(String token) {
        String key = KEY_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }
}

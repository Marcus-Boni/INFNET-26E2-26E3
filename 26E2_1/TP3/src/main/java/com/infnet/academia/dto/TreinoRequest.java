package com.infnet.academia.dto;

import lombok.Data;

@Data
public class TreinoRequest {
    private String nome;
    private String focoPrincipal;
    private Long instrutorId;
}

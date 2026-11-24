package com.finance.transaction.infraestructure.entry_points.dto;

import com.finance.transaction.domain.model.enumns.TransaccionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoriaResponseDTO {

    private String id;
    private String userId;
    private String name;
    private TransaccionType type;

}

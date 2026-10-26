package com.finance.transaction.domain.model;

import com.finance.transaction.domain.model.enumns.TransaccionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Categoria {

    private String id;
    private String externalUserId;
    private String name;
    private TransaccionType type;
    private String icon;

}

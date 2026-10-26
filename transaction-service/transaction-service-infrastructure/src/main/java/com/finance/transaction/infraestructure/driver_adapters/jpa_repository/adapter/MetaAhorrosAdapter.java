package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.adapter;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.MetaAhorrosEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository.MetaAhorrosJpaRepository;
import com.finance.transaction.infraestructure.mapper.MetaAhorrosMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO: Queries have to be made with the ID, externalID, or both?

@Repository
@RequiredArgsConstructor
public class MetaAhorrosAdapter implements MetaAhorrosGateway {

    private final MetaAhorrosJpaRepository mJpaRepository;
    private final MetaAhorrosMapper Mmapper;

    @Override
    public MetaAhorro guardarMetaAhorros (MetaAhorro metaAhorro) {
        MetaAhorrosEntity entidad = Mmapper.toMetaAhorrosEntity(metaAhorro);
        MetaAhorrosEntity entidadGuardada = mJpaRepository.save(entidad);

        return Mmapper.toMetaAhorro(entidadGuardada);
    }

    @Override
    public MetaAhorro obtenerMetaAhorros (String id, String externalId) {
        Optional<MetaAhorrosEntity> entidad = mJpaRepository.findById(externalId);

        return entidad.map(Mmapper::toMetaAhorro).orElse(null);
    }

    //   TODO: Ask user id?
    //         Error handling could be better
    @Override
    public MetaAhorro actualizarMetaAhorros (MetaAhorro metaAhorro) {
        MetaAhorrosEntity existente = mJpaRepository.findById(metaAhorro.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada"));

        MetaAhorrosEntity datosNuevo = Mmapper.toMetaAhorrosEntity(metaAhorro);
        BeanUtils.copyProperties(existente, datosNuevo, "id");

        MetaAhorrosEntity transaccionActualizada =  mJpaRepository.save(datosNuevo);

        return Mmapper.toMetaAhorro(transaccionActualizada);
    }

    @Override
    public void eliminarMetaAhorros(String id, String externalId) {
        mJpaRepository.deleteById(id);
    }

    @Override
    public List<MetaAhorro> obtenerMetasPorUsuario(String externalId) {
        return mJpaRepository.findByExternalUserId(externalId)
                .stream()
                .map(Mmapper::toMetaAhorro)
                .collect(Collectors.toList());
    }
}
package com.finance.transaction.infraestructure.driver_adapters.jpa_repository.adapter;

import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.TransaccionEntity;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.repository.TransaccionJpaRepository;
import com.finance.transaction.infraestructure.mapper.TransaccionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO: Queries have to be made with the ID, externalID, or both?

@Repository
@RequiredArgsConstructor
public class TransaccionAdapter implements TransaccionGateway {

    private final TransaccionJpaRepository tJpaRepository;
    private final TransaccionMapper transaccionMapper;

    @Override
    public Transaccion guardarTransaccion(Transaccion transaction) {
        TransaccionEntity entity = transaccionMapper.transaccionToEntity(transaction);
        TransaccionEntity savedEntity = tJpaRepository.save(entity);

        return transaccionMapper.entityToTransaccion(savedEntity);
    }

    @Override
    public Transaccion obtenerTransaccion(String id, String externalId) {
        Optional<TransaccionEntity> transaccionEntity = tJpaRepository.findById(externalId);

        return transaccionEntity.map(transaccionMapper::entityToTransaccion).orElse(null);
    }

//   TODO: Ask user id?
//         Error handling could be better
    @Override
    public Transaccion actualizarTransaccion(Transaccion transaccion) {
        TransaccionEntity existente = tJpaRepository.findById(transaccion.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada"));

        TransaccionEntity datosNuevo = transaccionMapper.transaccionToEntity(transaccion);
        BeanUtils.copyProperties(existente, datosNuevo, "id");

        TransaccionEntity transaccionActualizada =  tJpaRepository.save(datosNuevo);

        return transaccionMapper.entityToTransaccion(transaccionActualizada);
    }

    @Override
    public void eliminarTransaccion(String id, String externalId) {
        tJpaRepository.deleteById(externalId);
    }

    @Override
    public List<Transaccion> obtenerTransaccionesPorUsuario(String externalId) {
        return tJpaRepository.findByExternalUserId(externalId)
                .stream()
                .map(transaccionMapper::entityToTransaccion)
                .collect(Collectors.toList());
    }
}

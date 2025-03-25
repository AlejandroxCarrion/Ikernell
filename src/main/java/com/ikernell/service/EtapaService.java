package com.ikernell.service;

import com.ikernell.model.Etapa;
import com.ikernell.repository.ActividadRepositorio;
import com.ikernell.repository.EtapaRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class EtapaService {

    @Autowired
    private EtapaRepositorio etapaRepositorio;

    @Autowired
    private ActividadRepositorio actividadRepositorio;

    @Transactional
    public boolean eliminarEtapa(Integer id) {
        Optional<Etapa> etapaOpt = etapaRepositorio.findById(id);
        if (etapaOpt.isPresent()) {
            actividadRepositorio.deleteByEtapaId(id);
            etapaRepositorio.delete(etapaOpt.get());
            return true;
        }
        return false;
    }
}

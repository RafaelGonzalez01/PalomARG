package com.palomarg.service;

import com.palomarg.model.entity.AliasColombofilo;
import com.palomarg.model.entity.Colombofilo;
import com.palomarg.model.entity.ReclamoPalomar;
import com.palomarg.model.entity.Usuario;
import com.palomarg.model.enums.EstadoReclamo;
import com.palomarg.repository.AliasColombofiloRepository;
import com.palomarg.repository.ColombofiloRepository;
import com.palomarg.repository.PalomaRepository;
import com.palomarg.repository.ReclamoPalomarRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de reclamos de palomar.
 * Orquesta la creación, aprobación y rechazo de reclamos de vinculación.
 */
public class ReclamoService {

    private final ReclamoPalomarRepository reclamoRepo;
    private final ColombofiloRepository colombofiloRepo;
    private final AliasColombofiloRepository aliasRepo;
    private final BusquedaColombofiloService busquedaService;
    private final PalomaRepository palomaRepo;

    public ReclamoService(ReclamoPalomarRepository reclamoRepo,
                          ColombofiloRepository colombofiloRepo,
                          AliasColombofiloRepository aliasRepo,
                          PalomaRepository palomaRepo) {
        this.reclamoRepo = reclamoRepo;
        this.colombofiloRepo = colombofiloRepo;
        this.aliasRepo = aliasRepo;
        this.palomaRepo = palomaRepo;
        this.busquedaService = new BusquedaColombofiloService(colombofiloRepo, palomaRepo);
    }

    /**
     * Crea un reclamo PENDIENTE de vinculación entre un usuario y un colombófilo.
     * Valida que no haya otro reclamo PENDIENTE del mismo usuario.
     *
     * @param usuario     el usuario que reclama
     * @param colombofilo el colombófilo candidato
     * @return el reclamo creado
     * @throws IllegalArgumentException si ya existe un reclamo pendiente del mismo usuario
     */
    public ReclamoPalomar crearReclamo(Usuario usuario, Colombofilo colombofilo) {
        if (usuario == null) throw new IllegalArgumentException("El usuario no puede ser nulo.");
        if (colombofilo == null) throw new IllegalArgumentException("El colombófilo no puede ser nulo.");

        // Verificar que no haya otro reclamo PENDIENTE del mismo usuario
        boolean tienePendiente = reclamoRepo.findByUsuarioId(usuario.getId()).stream()
            .anyMatch(r -> r.getEstado() == EstadoReclamo.PENDIENTE);

        if (tienePendiente) {
            throw new IllegalArgumentException(
                "El usuario '" + usuario.getNombreUsuario() + "' ya tiene un reclamo pendiente de revisión. " +
                "Aguarde la resolución del MASTER antes de realizar otro reclamo."
            );
        }

        ReclamoPalomar reclamo = new ReclamoPalomar(usuario, colombofilo, null);
        return reclamoRepo.save(reclamo);
    }

    /**
     * Aprueba un reclamo: vincula el colombófilo al usuario y registra alias si se provee.
     *
     * @param reclamo    el reclamo a aprobar
     * @param nombreWeb  alias del colombófilo en la web (puede ser null)
     */
    public void aprobarReclamo(ReclamoPalomar reclamo, AliasColombofilo nombreWeb) {
        if (reclamo == null) throw new IllegalArgumentException("El reclamo no puede ser nulo.");
        Colombofilo col = reclamo.getColombofilo();
        Usuario usr = reclamo.getUsuario();
        if (col == null || usr == null) {
            throw new IllegalStateException("El reclamo no tiene colombófilo o usuario asignado.");
        }

        // Vincular colombófilo al usuario
        col.setUsuario(usr);
        colombofiloRepo.save(col);

        // Guardar alias si se provee
        if (nombreWeb != null && nombreWeb.getNombreWeb() != null && !nombreWeb.getNombreWeb().isBlank()) {
            nombreWeb.setColombofilo(col);
            aliasRepo.save(nombreWeb);
        }

        // Actualizar estado del reclamo
        reclamo.setEstado(EstadoReclamo.APROBADO);
        reclamoRepo.save(reclamo);
    }

    /**
     * Rechaza un reclamo guardando el motivo en el campo comentario.
     *
     * @param reclamo el reclamo a rechazar
     * @param motivo  motivo del rechazo (se guarda en comentario)
     */
    public void rechazarReclamo(ReclamoPalomar reclamo, String motivo) {
        if (reclamo == null) throw new IllegalArgumentException("El reclamo no puede ser nulo.");
        reclamo.setEstado(EstadoReclamo.RECHAZADO);
        reclamo.setComentario(motivo);
        reclamoRepo.save(reclamo);
    }

    /**
     * Obtiene reclamos filtrados por estado.
     *
     * @param estado estado a filtrar
     * @return lista de reclamos
     */
    public List<ReclamoPalomar> getReclamos(EstadoReclamo estado) {
        return reclamoRepo.findByEstado(estado);
    }

    /**
     * Busca colombófilos candidatos usando el servicio de búsqueda difusa.
     *
     * @param query texto de búsqueda
     * @return lista de candidatos con score
     */
    public List<BusquedaColombofiloService.CandidatoPalomar> buscarCandidatos(String query) {
        return busquedaService.buscarCandidatos(query);
    }

    /**
     * Devuelve la lista de colombófilos sin usuario asignado (para notificar al MASTER).
     *
     * @return lista de colombófilos sin usuario
     */
    public List<Colombofilo> colombofilosNuevosSinUsuario() {
        return colombofiloRepo.findAll().stream()
            .filter(c -> c.getUsuario() == null)
            .collect(Collectors.toList());
    }

    /**
     * Fusiona dos colombófilos: reasigna las palomas del origen al destino y elimina el origen.
     *
     * @param origen  colombófilo que se eliminará
     * @param destino colombófilo que recibirá las palomas y resultados
     */
    public void fusionarColombofilos(Colombofilo origen, Colombofilo destino) {
        if (origen == null || destino == null) throw new IllegalArgumentException("Los colombófilos no pueden ser nulos.");
        if (origen.getId().equals(destino.getId())) throw new IllegalArgumentException("No se puede fusionar un colombófilo consigo mismo.");

        // Reasignar palomas del origen al destino
        if (palomaRepo != null) {
            palomaRepo.findByPropietarioId(origen.getId()).forEach(paloma -> {
                paloma.setPropietario(destino);
                palomaRepo.save(paloma);
            });
        }

        // Eliminar origen
        colombofiloRepo.deleteById(origen.getId());
    }

    public BusquedaColombofiloService getBusquedaService() {
        return busquedaService;
    }
}

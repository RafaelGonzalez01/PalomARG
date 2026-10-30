package com.palomarg.service;

import com.palomarg.model.entity.Usuario;
import com.palomarg.model.enums.RolUsuario;
import com.palomarg.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

/**
 * Servicio de autenticación: registro, login y gestión de sesión en memoria.
 */
public class AuthService {

    /** Usuario actualmente autenticado (sesión en memoria). */
    private static Usuario usuarioActual = null;

    private final UsuarioRepository usuarioRepo;

    public AuthService(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    /**
     * Registra un nuevo usuario hasheando la contraseña con BCrypt.
     *
     * @param nombreUsuario nombre de usuario único
     * @param password      contraseña en texto plano
     * @param rol           rol del usuario
     * @return el Usuario guardado
     * @throws IllegalArgumentException si el nombre o password están vacíos, o si el usuario ya existe
     */
    public Usuario registrar(String nombreUsuario, String password, RolUsuario rol) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres.");
        }
        if (usuarioRepo.findByNombreUsuario(nombreUsuario.trim()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario '" + nombreUsuario.trim() + "' ya está en uso.");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        Usuario usuario = new Usuario(nombreUsuario.trim(), hash, rol != null ? rol : RolUsuario.USUARIO);
        return usuarioRepo.save(usuario);
    }

    /**
     * Verifica las credenciales con BCrypt.
     *
     * @param nombreUsuario nombre de usuario
     * @param password      contraseña en texto plano
     * @return Optional con el Usuario si las credenciales son correctas, vacío si no
     */
    public Optional<Usuario> login(String nombreUsuario, String password) {
        if (nombreUsuario == null || password == null) return Optional.empty();
        Optional<Usuario> opt = usuarioRepo.findByNombreUsuario(nombreUsuario.trim());
        if (opt.isEmpty()) return Optional.empty();
        Usuario u = opt.get();
        try {
            if (BCrypt.checkpw(password, u.getHashPassword())) {
                return Optional.of(u);
            }
        } catch (Exception e) {
            // Hash inválido o formato incorrecto (datos de ejemplo sin BCrypt)
            System.err.println("AuthService: hash inválido para usuario '" + nombreUsuario + "': " + e.getMessage());
        }
        return Optional.empty();
    }

    /** @return el usuario actualmente autenticado, o null si no hay sesión */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /** Establece el usuario de la sesión actual. */
    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    /** Cierra la sesión actual. */
    public static void cerrarSesion() {
        usuarioActual = null;
    }
}

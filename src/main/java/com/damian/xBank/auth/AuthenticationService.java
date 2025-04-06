package com.damian.xBank.auth;

import com.damian.xBank.auth.exception.AuthenticationException;
import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.utils.JWTUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(JWTUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Metodo que contiene la logica principal del servicio de autenticacion.
     *
     * @param request Peticion que contiene los datos de usuario que intenta autenticarse (usuario y password)
     * @return La respuesta que contiene los datos del usuario autentificado.
     * @throws AuthenticationException Excepcion con el mensaje del fallo arrojado durante la autenticacion
     */
    public AuthenticationResponse login(AuthenticationRequest request) {
        String email = request.email();
        String password = request.password();

        Authentication auth;

        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email, password)
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new AuthenticationException("Bad credentials"); // 403 Forbidden
        }

        // Creamos el token utilizado para validar al usuario
        String token = jwtUtil.generateToken(email);

        // Usuario autenticado
        Customer customer = (Customer) auth.getPrincipal();

        // Si necesitasemos mantener la sesion podriamos almacenar los datos
        // Pero esto no tiene sentido en si usamos sesiones sin estado basado tokens
        // SecurityContextHolder.getContext().setAuth entication(auth);

        // Enviamos al usuario de vuelta los datos necesarios para el cliente
        return new AuthenticationResponse(
                customer.getId(), customer.getEmail(), token
        );
    }



}

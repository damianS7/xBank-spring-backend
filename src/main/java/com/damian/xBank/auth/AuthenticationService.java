package com.damian.xBank.auth;

import com.damian.xBank.auth.exception.AuthenticationException;
import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerService;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import com.damian.xBank.utils.JWTUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomerService customerService;

    public AuthenticationService(JWTUtil jwtUtil, AuthenticationManager authenticationManager, CustomerService customerService) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.customerService = customerService;
    }

    public Customer register(CustomerRegistrationRequest request) {
        return customerService.createCustomer(request);
    }

    /**
     * Metodo que contiene la logica principal del servicio de autenticacion.
     *
     * @param request Peticion que contiene los datos de usuario que intenta autenticarse (usuario y password)
     * @return La respuesta que contiene los datos del usuario autentificado.
     * @throws AuthenticationException Excepcion con el mensaje del fallo arrojado durante la autenticacion
     */
    public AuthenticationResponse login(AuthenticationRequest request) {
        final String email = request.email();
        final String password = request.password();

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
        final String token = jwtUtil.generateToken(email);

        // id usuario autenticado
        Long customerId = ((Customer) auth.getPrincipal()).getId();

        // should return Customer from database
        Customer customer = customerService.getCustomer(customerId);

        // Enviamos al usuario de vuelta los datos necesarios para el cliente
        return new AuthenticationResponse(
                customer.toDTO(), token
        );
    }


}

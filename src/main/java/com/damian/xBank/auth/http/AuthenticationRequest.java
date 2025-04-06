package com.damian.xBank.auth.http;

/**
 * Plantilla para los datos que debe enviar el usuario en su peticion.
 */
public record AuthenticationRequest(String email, String password) {

}

package com.pm.bookingservice.security;


import java.util.UUID;

public interface SecurityService {

    /**
     * Returns the UUID of the currently authenticated user.
     * Throws an error if no user is authenticated.
     */
    UUID getAuthenticatedUserId();


}

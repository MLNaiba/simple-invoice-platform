package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.dto.AuthenticationRequest;
import io.github.MLNaiba.simpleinvoice.dto.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
}

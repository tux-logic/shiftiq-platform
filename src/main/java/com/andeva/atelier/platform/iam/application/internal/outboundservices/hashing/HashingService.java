package com.andeva.atelier.platform.iam.application.internal.outboundservices.hashing;

public interface HashingService {
    String encode(CharSequence rawPassword);
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean matches(CharSequence rawPassword, String encodedPassword);
}

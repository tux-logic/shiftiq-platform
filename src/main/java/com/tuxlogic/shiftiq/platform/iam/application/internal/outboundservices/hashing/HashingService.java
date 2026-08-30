package com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.hashing;

public interface HashingService {
    String encode(CharSequence rawPassword);
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean matches(CharSequence rawPassword, String encodedPassword);
}

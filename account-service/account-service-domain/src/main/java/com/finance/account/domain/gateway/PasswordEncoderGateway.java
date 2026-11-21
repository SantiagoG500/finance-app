package com.finance.account.domain.gateway;

public interface PasswordEncoderGateway {

    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);

}

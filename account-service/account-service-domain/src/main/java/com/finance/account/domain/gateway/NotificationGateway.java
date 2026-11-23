package com.finance.account.domain.gateway;

public interface NotificationGateway {

    void notifyUserRegistration(String email);

    void notifyLoginAlert(String email);

    void notifyPasswordChanged(String email);

    void notifyAccountStatusChanged(String email, String status);

}

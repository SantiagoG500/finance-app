package com.finance.transaction.domain.model.gateway;

import com.finance.transaction.domain.model.VerifiedUserSession;

public interface AccountVerificationGateway {

    VerifiedUserSession verifyLoggedUser(String userId, String bearerToken);

}

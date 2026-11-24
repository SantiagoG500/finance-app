package com.finance.transaction.domain.model.gateway;

public interface NotificationGateway {

    void notifyTransactionCreated(String email, String transactionType, String formattedAmount, String description);

    void notifySavingsGoalAchieved(String email, String goalTitle, String progressSummary, String percentage);

    void notifyProjectionDeficit(String email, String monthsHorizon, String projectedBalance);

}

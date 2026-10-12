package com.utegear.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class TransactionCallbacksTest {

    @AfterEach
    void cleanUp() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void runsRollbackCleanupOnlyWhenTransactionRollsBack() {
        AtomicBoolean cleaned = new AtomicBoolean();
        TransactionSynchronizationManager.initSynchronization();
        new TransactionCallbacks().afterRollback(() -> cleaned.set(true));

        TransactionSynchronizationManager.getSynchronizations().forEach(
                synchronization -> synchronization.afterCompletion(
                        TransactionSynchronization.STATUS_ROLLED_BACK));

        assertThat(cleaned).isTrue();
    }

    @Test
    void runsCommitCleanupAfterCommit() {
        AtomicBoolean cleaned = new AtomicBoolean();
        TransactionSynchronizationManager.initSynchronization();
        new TransactionCallbacks().afterCommit(() -> cleaned.set(true));

        TransactionSynchronizationManager.getSynchronizations().forEach(
                TransactionSynchronization::afterCommit);

        assertThat(cleaned).isTrue();
    }
}

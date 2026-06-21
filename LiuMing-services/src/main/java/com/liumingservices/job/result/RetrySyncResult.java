package com.liumingservices.job.result;

import java.util.List;

public class RetrySyncResult {

    private final int retrySuccessCount;

    private final List<EquipmentSyncResult> finalFailedResults;

    public RetrySyncResult(int retrySuccessCount, List<EquipmentSyncResult> finalFailedResults) {
        this.retrySuccessCount = retrySuccessCount;
        this.finalFailedResults = finalFailedResults;
    }

    public int getRetrySuccessCount() {
        return retrySuccessCount;
    }

    public List<EquipmentSyncResult> getFinalFailedResults() {
        return finalFailedResults;
    }
}

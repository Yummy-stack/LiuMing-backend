package com.liumingservices.job.result;


import java.util.List;

public class BatchSyncResult {

    private final int successCount;

    private final List<EquipmentSyncResult> failedResults;

    public BatchSyncResult(int successCount, List<EquipmentSyncResult> failedResults) {
        this.successCount = successCount;
        this.failedResults = failedResults;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public List<EquipmentSyncResult> getFailedResults() {
        return failedResults;
    }
}

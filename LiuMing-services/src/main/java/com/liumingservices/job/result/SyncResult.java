package com.liumingservices.job.result;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Data
public class SyncResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -8102023429159431453L;

    private Boolean result;

    private int successCount;

    private int failedCount;

}

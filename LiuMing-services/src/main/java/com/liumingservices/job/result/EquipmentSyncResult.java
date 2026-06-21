package com.liumingservices.job.result;

import com.liumingservices.entity.Equipment;


public class EquipmentSyncResult {

    private final boolean success;

    private final Equipment equipment;

    private final String errorMessage;

    public EquipmentSyncResult(boolean success, Equipment equipment, String errorMessage) {
        this.success = success;
        this.equipment = equipment;
        this.errorMessage = errorMessage;
    }

    public static EquipmentSyncResult success(Equipment equipment) {
        return new EquipmentSyncResult(true, equipment, null);
    }

    public static EquipmentSyncResult fail(Equipment equipment, String errorMessage) {
        return new EquipmentSyncResult(false, equipment, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

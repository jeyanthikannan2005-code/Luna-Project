package com.lunette.gifts.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "business_settings")
public class BusinessSetting {

    @Id
    @Column(name = "setting_key", nullable = false, length = 60)
    private String settingKey;

    @Column(name = "setting_value", nullable = false, columnDefinition = "TEXT")
    private String settingValue;

    @Column(length = 255)
    private String description;

    public BusinessSetting() {}

    public BusinessSetting(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
    }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

package com.songjin.usum.entities;

import com.kth.baasio.utils.JsonUtils;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;

public class ReservedCategoryEntity {
    public int schoolId;
    public int category;
    public long lastCheckedTimestamp;

    public ReservedCategoryEntity(int schoolId, int category, long lastCheckedTimestamp) {
        this.schoolId = schoolId;
        this.category = category;
        this.lastCheckedTimestamp = lastCheckedTimestamp;
    }

    public ReservedCategoryEntity() {

    }

    @JsonCreator
    public static ReservedCategoryEntity createObject(String jsonString) {
        return (ReservedCategoryEntity) JsonUtils.fromJsonString(jsonString, ReservedCategoryEntity.class);
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public long getLastCheckedTimestamp() {
        return lastCheckedTimestamp;
    }

    public void setLastCheckedTimestamp(long lastCheckedTimestamp) {
        this.lastCheckedTimestamp = lastCheckedTimestamp;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int schoolId) {
        this.schoolId = schoolId;
    }

    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}

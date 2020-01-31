
package com.ora.android.eyecup.json;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ProtocolRevEventActivityId",
    "ProtocolRevEventId",
    "ActivitySeq",
    "ActivityId",
    "ProtRevEvtApplyTo",
    "ActivityTypeId",
    "ActivityTypeCode",
    "ActivityText",
    "ActivityResponseTypeId",
    "ActivityResponseTypeCode",
    "ActivityResponseCnt",
    "ActivityResponses",
    "MinRange",
    "MaxRange",
    "ActivityPictureCode"
})
public class EventActivity implements Serializable
{

    @JsonProperty("ProtocolRevEventActivityId")
    private Long protocolRevEventActivityId;
    @JsonProperty("ProtocolRevEventId")
    private Long protocolRevEventId;
    @JsonProperty("ActivitySeq")
    private Long activitySeq;
    @JsonProperty("ActivityId")
    private Long activityId;
    @JsonProperty("ProtRevEvtApplyTo")
    private String protRevEvtApplyTo;
    @JsonProperty("ActivityTypeId")
    private Long activityTypeId;
    @JsonProperty("ActivityTypeCode")
    private String activityTypeCode;
    @JsonProperty("ActivityText")
    private String activityText;
    @JsonProperty("ActivityResponseTypeId")
    private Long activityResponseTypeId;
    @JsonProperty("ActivityResponseTypeCode")
    private String activityResponseTypeCode;
    @JsonProperty("ActivityResponseCnt")
    private Long activityResponseCnt;
    @JsonProperty("ActivityResponses")
    private List<ActivityResponse> activityResponses = null;
    @JsonProperty("MinRange")
    private Long minRange;
    @JsonProperty("MaxRange")
    private Long maxRange;
    @JsonProperty("ActivityPictureCode")
    private String activityPictureCode;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = -6744488314917921757L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public EventActivity() {
    }

    /**
     * 
     * @param protocolRevEventActivityId
     * @param activityPictureCode
     * @param activitySeq
     * @param activityResponses
     * @param activityResponseCnt
     * @param activityText
     * @param maxRange
     * @param activityResponseTypeId
     * @param activityId
     * @param protocolRevEventId
     * @param protRevEvtApplyTo
     * @param activityResponseTypeCode
     * @param activityTypeCode
     * @param activityTypeId
     * @param minRange
     */
    public EventActivity(Long protocolRevEventActivityId, Long protocolRevEventId, Long activitySeq, Long activityId, String protRevEvtApplyTo, Long activityTypeId, String activityTypeCode, String activityText, Long activityResponseTypeId, String activityResponseTypeCode, Long activityResponseCnt, List<ActivityResponse> activityResponses, Long minRange, Long maxRange, String activityPictureCode) {
        super();
        this.protocolRevEventActivityId = protocolRevEventActivityId;
        this.protocolRevEventId = protocolRevEventId;
        this.activitySeq = activitySeq;
        this.activityId = activityId;
        this.protRevEvtApplyTo = protRevEvtApplyTo;
        this.activityTypeId = activityTypeId;
        this.activityTypeCode = activityTypeCode;
        this.activityText = activityText;
        this.activityResponseTypeId = activityResponseTypeId;
        this.activityResponseTypeCode = activityResponseTypeCode;
        this.activityResponseCnt = activityResponseCnt;
        this.activityResponses = activityResponses;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.activityPictureCode = activityPictureCode;
    }

    @JsonProperty("ProtocolRevEventActivityId")
    public Long getProtocolRevEventActivityId() {
        return protocolRevEventActivityId;
    }

    @JsonProperty("ProtocolRevEventActivityId")
    public void setProtocolRevEventActivityId(Long protocolRevEventActivityId) {
        this.protocolRevEventActivityId = protocolRevEventActivityId;
    }

    public EventActivity withProtocolRevEventActivityId(Long protocolRevEventActivityId) {
        this.protocolRevEventActivityId = protocolRevEventActivityId;
        return this;
    }

    @JsonProperty("ProtocolRevEventId")
    public Long getProtocolRevEventId() {
        return protocolRevEventId;
    }

    @JsonProperty("ProtocolRevEventId")
    public void setProtocolRevEventId(Long protocolRevEventId) {
        this.protocolRevEventId = protocolRevEventId;
    }

    public EventActivity withProtocolRevEventId(Long protocolRevEventId) {
        this.protocolRevEventId = protocolRevEventId;
        return this;
    }

    @JsonProperty("ActivitySeq")
    public Long getActivitySeq() {
        return activitySeq;
    }

    @JsonProperty("ActivitySeq")
    public void setActivitySeq(Long activitySeq) {
        this.activitySeq = activitySeq;
    }

    public EventActivity withActivitySeq(Long activitySeq) {
        this.activitySeq = activitySeq;
        return this;
    }

    @JsonProperty("ActivityId")
    public Long getActivityId() {
        return activityId;
    }

    @JsonProperty("ActivityId")
    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public EventActivity withActivityId(Long activityId) {
        this.activityId = activityId;
        return this;
    }

    @JsonProperty("ProtRevEvtApplyTo")
    public String getProtRevEvtApplyTo() {
        return protRevEvtApplyTo;
    }

    @JsonProperty("ProtRevEvtApplyTo")
    public void setProtRevEvtApplyTo(String protRevEvtApplyTo) {
        this.protRevEvtApplyTo = protRevEvtApplyTo;
    }

    public EventActivity withProtRevEvtApplyTo(String protRevEvtApplyTo) {
        this.protRevEvtApplyTo = protRevEvtApplyTo;
        return this;
    }

    @JsonProperty("ActivityTypeId")
    public Long getActivityTypeId() {
        return activityTypeId;
    }

    @JsonProperty("ActivityTypeId")
    public void setActivityTypeId(Long activityTypeId) {
        this.activityTypeId = activityTypeId;
    }

    public EventActivity withActivityTypeId(Long activityTypeId) {
        this.activityTypeId = activityTypeId;
        return this;
    }

    @JsonProperty("ActivityTypeCode")
    public String getActivityTypeCode() {
        return activityTypeCode;
    }

    @JsonProperty("ActivityTypeCode")
    public void setActivityTypeCode(String activityTypeCode) {
        this.activityTypeCode = activityTypeCode;
    }

    public EventActivity withActivityTypeCode(String activityTypeCode) {
        this.activityTypeCode = activityTypeCode;
        return this;
    }

    @JsonProperty("ActivityText")
    public String getActivityText() {
        return activityText;
    }

    @JsonProperty("ActivityText")
    public void setActivityText(String activityText) {
        this.activityText = activityText;
    }

    public EventActivity withActivityText(String activityText) {
        this.activityText = activityText;
        return this;
    }

    @JsonProperty("ActivityResponseTypeId")
    public Long getActivityResponseTypeId() {
        return activityResponseTypeId;
    }

    @JsonProperty("ActivityResponseTypeId")
    public void setActivityResponseTypeId(Long activityResponseTypeId) {
        this.activityResponseTypeId = activityResponseTypeId;
    }

    public EventActivity withActivityResponseTypeId(Long activityResponseTypeId) {
        this.activityResponseTypeId = activityResponseTypeId;
        return this;
    }

    @JsonProperty("ActivityResponseTypeCode")
    public String getActivityResponseTypeCode() {
        return activityResponseTypeCode;
    }

    @JsonProperty("ActivityResponseTypeCode")
    public void setActivityResponseTypeCode(String activityResponseTypeCode) {
        this.activityResponseTypeCode = activityResponseTypeCode;
    }

    public EventActivity withActivityResponseTypeCode(String activityResponseTypeCode) {
        this.activityResponseTypeCode = activityResponseTypeCode;
        return this;
    }

    @JsonProperty("ActivityResponseCnt")
    public Long getActivityResponseCnt() {
        return activityResponseCnt;
    }

    @JsonProperty("ActivityResponseCnt")
    public void setActivityResponseCnt(Long activityResponseCnt) {
        this.activityResponseCnt = activityResponseCnt;
    }

    public EventActivity withActivityResponseCnt(Long activityResponseCnt) {
        this.activityResponseCnt = activityResponseCnt;
        return this;
    }

    @JsonProperty("ActivityResponses")
    public List<ActivityResponse> getActivityResponses() {
        return activityResponses;
    }

    @JsonProperty("ActivityResponses")
    public void setActivityResponses(List<ActivityResponse> activityResponses) {
        this.activityResponses = activityResponses;
    }

    public EventActivity withActivityResponses(List<ActivityResponse> activityResponses) {
        this.activityResponses = activityResponses;
        return this;
    }

    @JsonProperty("MinRange")
    public Long getMinRange() {
        return minRange;
    }

    @JsonProperty("MinRange")
    public void setMinRange(Long minRange) {
        this.minRange = minRange;
    }

    public EventActivity withMinRange(Long minRange) {
        this.minRange = minRange;
        return this;
    }

    @JsonProperty("MaxRange")
    public Long getMaxRange() {
        return maxRange;
    }

    @JsonProperty("MaxRange")
    public void setMaxRange(Long maxRange) {
        this.maxRange = maxRange;
    }

    public EventActivity withMaxRange(Long maxRange) {
        this.maxRange = maxRange;
        return this;
    }

    @JsonProperty("ActivityPictureCode")
    public String getActivityPictureCode() {
        return activityPictureCode;
    }

    @JsonProperty("ActivityPictureCode")
    public void setActivityPictureCode(String activityPictureCode) {
        this.activityPictureCode = activityPictureCode;
    }

    public EventActivity withActivityPictureCode(String activityPictureCode) {
        this.activityPictureCode = activityPictureCode;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public EventActivity withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("protocolRevEventActivityId", protocolRevEventActivityId).append("protocolRevEventId", protocolRevEventId).append("activitySeq", activitySeq).append("activityId", activityId).append("protRevEvtApplyTo", protRevEvtApplyTo).append("activityTypeId", activityTypeId).append("activityTypeCode", activityTypeCode).append("activityText", activityText).append("activityResponseTypeId", activityResponseTypeId).append("activityResponseTypeCode", activityResponseTypeCode).append("activityResponseCnt", activityResponseCnt).append("activityResponses", activityResponses).append("minRange", minRange).append("maxRange", maxRange).append("activityPictureCode", activityPictureCode).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(protocolRevEventActivityId).append(activityPictureCode).append(activitySeq).append(activityResponses).append(activityResponseCnt).append(activityText).append(maxRange).append(activityResponseTypeId).append(activityId).append(protocolRevEventId).append(protRevEvtApplyTo).append(activityResponseTypeCode).append(activityTypeCode).append(additionalProperties).append(activityTypeId).append(minRange).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EventActivity)) {
            return false;
        }
        EventActivity rhs = ((EventActivity) other);
        return new EqualsBuilder().append(protocolRevEventActivityId, rhs.protocolRevEventActivityId).append(activityPictureCode, rhs.activityPictureCode).append(activitySeq, rhs.activitySeq).append(activityResponses, rhs.activityResponses).append(activityResponseCnt, rhs.activityResponseCnt).append(activityText, rhs.activityText).append(maxRange, rhs.maxRange).append(activityResponseTypeId, rhs.activityResponseTypeId).append(activityId, rhs.activityId).append(protocolRevEventId, rhs.protocolRevEventId).append(protRevEvtApplyTo, rhs.protRevEvtApplyTo).append(activityResponseTypeCode, rhs.activityResponseTypeCode).append(activityTypeCode, rhs.activityTypeCode).append(additionalProperties, rhs.additionalProperties).append(activityTypeId, rhs.activityTypeId).append(minRange, rhs.minRange).isEquals();
    }

}

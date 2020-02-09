
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
    "ProtocolRevEventId",
    "ProtocolRevId",
    "ProtocolRevEventName",
    "FrequencyCode",
    "EventDayStart",
    "EventDaysDuration",
    "EventTimeOpen",
    "EventTimeWarn",
    "EventTimeClose",
    "EventActivityCnt",
    "EventActivities"
})
public class ProtocolRevEvent implements Serializable
{

    @JsonProperty("ProtocolRevEventId")
    private Long protocolRevEventId;
    @JsonProperty("ProtocolRevId")
    private Long protocolRevId;
    @JsonProperty("ProtocolRevEventName")
    private String protocolRevEventName;
    @JsonProperty("FrequencyCode")
    private String frequencyCode;
    @JsonProperty("EventDayStart")
    private Long eventDayStart;
    @JsonProperty("EventDaysDuration")
    private Long eventDaysDuration;
    @JsonProperty("EventTimeOpen")
    private String eventTimeOpen;
    @JsonProperty("EventTimeWarn")
    private String eventTimeWarn;
    @JsonProperty("EventTimeClose")
    private String eventTimeClose;
    @JsonProperty("EventActivityCnt")
    private Long eventActivityCnt;
    @JsonProperty("EventActivities")
    private List<EventActivity> eventActivities = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = -1959107308008190751L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProtocolRevEvent() {
    }

    /**
     * 
     * @param frequencyCode
     * @param protocolRevEventName
     * @param protocolRevEventId
     * @param protocolRevId
     * @param eventTimeOpen
     * @param eventTimeWarn
     * @param eventActivities
     * @param eventActivityCnt
     * @param eventTimeClose
     * @param eventDayStart
     * //todo add eventDaysDuration
     */
    public ProtocolRevEvent(Long protocolRevEventId, Long protocolRevId, String protocolRevEventName, String frequencyCode, Long eventDayStart, Long eventDaysDuration, String eventTimeOpen, String eventTimeWarn, String eventTimeClose, Long eventActivityCnt, List<EventActivity> eventActivities) {
        super();
        this.protocolRevEventId = protocolRevEventId;
        this.protocolRevId = protocolRevId;
        this.protocolRevEventName = protocolRevEventName;
        this.frequencyCode = frequencyCode;
        this.eventDayStart = eventDayStart;
        this.eventDaysDuration = eventDaysDuration;
        this.eventTimeOpen = eventTimeOpen;
        this.eventTimeWarn = eventTimeWarn;
        this.eventTimeClose = eventTimeClose;
        this.eventActivityCnt = eventActivityCnt;
        this.eventActivities = eventActivities;
    }

    @JsonProperty("ProtocolRevEventId")
    public Long getProtocolRevEventId() {
        return protocolRevEventId;
    }

    @JsonProperty("ProtocolRevEventId")
    public void setProtocolRevEventId(Long protocolRevEventId) {
        this.protocolRevEventId = protocolRevEventId;
    }

    public ProtocolRevEvent withProtocolRevEventId(Long protocolRevEventId) {
        this.protocolRevEventId = protocolRevEventId;
        return this;
    }

    @JsonProperty("ProtocolRevId")
    public Long getProtocolRevId() {
        return protocolRevId;
    }

    @JsonProperty("ProtocolRevId")
    public void setProtocolRevId(Long protocolRevId) {
        this.protocolRevId = protocolRevId;
    }

    public ProtocolRevEvent withProtocolRevId(Long protocolRevId) {
        this.protocolRevId = protocolRevId;
        return this;
    }

    @JsonProperty("ProtocolRevEventName")
    public String getProtocolRevEventName() {
        return protocolRevEventName;
    }

    @JsonProperty("ProtocolRevEventName")
    public void setProtocolRevEventName(String protocolRevEventName) {
        this.protocolRevEventName = protocolRevEventName;
    }

    public ProtocolRevEvent withProtocolRevEventName(String protocolRevEventName) {
        this.protocolRevEventName = protocolRevEventName;
        return this;
    }

    @JsonProperty("FrequencyCode")
    public String getFrequencyCode() {
        return frequencyCode;
    }

    @JsonProperty("FrequencyCode")
    public void setFrequencyCode(String frequencyCode) {
        this.frequencyCode = frequencyCode;
    }

    public ProtocolRevEvent withFrequencyCode(String frequencyCode) {
        this.frequencyCode = frequencyCode;
        return this;
    }

    @JsonProperty("EventDayStart")
    public Long getEventDayStart() {
        return eventDayStart;
    }

    @JsonProperty("EventDayStart")
    public void setEventDayStart(Long eventDayStart) {
        this.eventDayStart = eventDayStart;
    }

    public ProtocolRevEvent withEventDayStart(Long eventDayStart) {
        this.eventDayStart = eventDayStart;
        return this;
    }

    @JsonProperty("EventDaysDuration")
    public Long getEventDaysDuration() {
        return eventDaysDuration;
    }

    @JsonProperty("EventDaysDuration")
    public void setEventDaysDuration(Long eventDaysDuration) {
        this.eventDaysDuration = eventDaysDuration;
    }

    public ProtocolRevEvent withEventDaysDuration(Long eventDaysDuration) {
        this.eventDaysDuration = eventDaysDuration;
        return this;
    }

    @JsonProperty("EventTimeOpen")
    public String getEventTimeOpen() {
        return eventTimeOpen;
    }

    @JsonProperty("EventTimeOpen")
    public void setEventTimeOpen(String eventTimeOpen) {
        this.eventTimeOpen = eventTimeOpen;
    }

    public ProtocolRevEvent withEventTimeOpen(String eventTimeOpen) {
        this.eventTimeOpen = eventTimeOpen;
        return this;
    }

    @JsonProperty("EventTimeWarn")
    public String getEventTimeWarn() {
        return eventTimeWarn;
    }

    @JsonProperty("EventTimeWarn")
    public void setEventTimeWarn(String eventTimeWarn) {
        this.eventTimeWarn = eventTimeWarn;
    }

    public ProtocolRevEvent withEventTimeWarn(String eventTimeWarn) {
        this.eventTimeWarn = eventTimeWarn;
        return this;
    }

    @JsonProperty("EventTimeClose")
    public String getEventTimeClose() {
        return eventTimeClose;
    }

    @JsonProperty("EventTimeClose")
    public void setEventTimeClose(String eventTimeClose) {
        this.eventTimeClose = eventTimeClose;
    }

    public ProtocolRevEvent withEventTimeClose(String eventTimeClose) {
        this.eventTimeClose = eventTimeClose;
        return this;
    }

    @JsonProperty("EventActivityCnt")
    public Long getEventActivityCnt() {
        return eventActivityCnt;
    }

    @JsonProperty("EventActivityCnt")
    public void setEventActivityCnt(Long eventActivityCnt) {
        this.eventActivityCnt = eventActivityCnt;
    }

    public ProtocolRevEvent withEventActivityCnt(Long eventActivityCnt) {
        this.eventActivityCnt = eventActivityCnt;
        return this;
    }

    @JsonProperty("EventActivities")
    public List<EventActivity> getEventActivities() {
        return eventActivities;
    }

    @JsonProperty("EventActivities")
    public void setEventActivities(List<EventActivity> eventActivities) {
        this.eventActivities = eventActivities;
    }

    public ProtocolRevEvent withEventActivities(List<EventActivity> eventActivities) {
        this.eventActivities = eventActivities;
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

    public ProtocolRevEvent withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("protocolRevEventId", protocolRevEventId).append("protocolRevId", protocolRevId).append("protocolRevEventName", protocolRevEventName).append("frequencyCode", frequencyCode).append("eventDayStart", eventDayStart).append("eventTimeOpen", eventTimeOpen).append("eventTimeWarn", eventTimeWarn).append("eventTimeClose", eventTimeClose).append("eventActivityCnt", eventActivityCnt).append("eventActivities", eventActivities).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(frequencyCode).append(protocolRevEventName).append(protocolRevEventId).append(protocolRevId).append(eventTimeOpen).append(eventTimeWarn).append(eventActivities).append(eventActivityCnt).append(eventTimeClose).append(additionalProperties).append(eventDayStart).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ProtocolRevEvent)) {
            return false;
        }
        ProtocolRevEvent rhs = ((ProtocolRevEvent) other);
        return new EqualsBuilder().append(frequencyCode, rhs.frequencyCode).append(protocolRevEventName, rhs.protocolRevEventName).append(protocolRevEventId, rhs.protocolRevEventId).append(protocolRevId, rhs.protocolRevId).append(eventTimeOpen, rhs.eventTimeOpen).append(eventTimeWarn, rhs.eventTimeWarn).append(eventActivities, rhs.eventActivities).append(eventActivityCnt, rhs.eventActivityCnt).append(eventTimeClose, rhs.eventTimeClose).append(additionalProperties, rhs.additionalProperties).append(eventDayStart, rhs.eventDayStart).isEquals();
    }

}

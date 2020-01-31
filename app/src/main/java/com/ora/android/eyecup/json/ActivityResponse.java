
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
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ProtocolRevEventActivityId",
    "ActRspId",
    "ActId",
    "ActRspSeq",
    "ActRspValue",
    "ActRspText"
})
public class ActivityResponse implements Serializable
{

    @JsonProperty("ProtocolRevEventActivityId")
    private Long protocolRevEventActivityId;
    @JsonProperty("ActRspId")
    private Long actRspId;
    @JsonProperty("ActId")
    private Long actId;
    @JsonProperty("ActRspSeq")
    private Long actRspSeq;
    @JsonProperty("ActRspValue")
    private Long actRspValue;
    @JsonProperty("ActRspText")
    private String actRspText;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = 355088460277004280L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ActivityResponse() {
    }

    /**
     * 
     * @param protocolRevEventActivityId
     * @param actRspValue
     * @param actId
     * @param actRspSeq
     * @param actRspText
     * @param actRspId
     */
    public ActivityResponse(Long protocolRevEventActivityId, Long actRspId, Long actId, Long actRspSeq, Long actRspValue, String actRspText) {
        super();
        this.protocolRevEventActivityId = protocolRevEventActivityId;
        this.actRspId = actRspId;
        this.actId = actId;
        this.actRspSeq = actRspSeq;
        this.actRspValue = actRspValue;
        this.actRspText = actRspText;
    }

    @JsonProperty("ProtocolRevEventActivityId")
    public Long getProtocolRevEventActivityId() {
        return protocolRevEventActivityId;
    }

    @JsonProperty("ProtocolRevEventActivityId")
    public void setProtocolRevEventActivityId(Long protocolRevEventActivityId) {
        this.protocolRevEventActivityId = protocolRevEventActivityId;
    }

    public ActivityResponse withProtocolRevEventActivityId(Long protocolRevEventActivityId) {
        this.protocolRevEventActivityId = protocolRevEventActivityId;
        return this;
    }

    @JsonProperty("ActRspId")
    public Long getActRspId() {
        return actRspId;
    }

    @JsonProperty("ActRspId")
    public void setActRspId(Long actRspId) {
        this.actRspId = actRspId;
    }

    public ActivityResponse withActRspId(Long actRspId) {
        this.actRspId = actRspId;
        return this;
    }

    @JsonProperty("ActId")
    public Long getActId() {
        return actId;
    }

    @JsonProperty("ActId")
    public void setActId(Long actId) {
        this.actId = actId;
    }

    public ActivityResponse withActId(Long actId) {
        this.actId = actId;
        return this;
    }

    @JsonProperty("ActRspSeq")
    public Long getActRspSeq() {
        return actRspSeq;
    }

    @JsonProperty("ActRspSeq")
    public void setActRspSeq(Long actRspSeq) {
        this.actRspSeq = actRspSeq;
    }

    public ActivityResponse withActRspSeq(Long actRspSeq) {
        this.actRspSeq = actRspSeq;
        return this;
    }

    @JsonProperty("ActRspValue")
    public Long getActRspValue() {
        return actRspValue;
    }

    @JsonProperty("ActRspValue")
    public void setActRspValue(Long actRspValue) {
        this.actRspValue = actRspValue;
    }

    public ActivityResponse withActRspValue(Long actRspValue) {
        this.actRspValue = actRspValue;
        return this;
    }

    @JsonProperty("ActRspText")
    public String getActRspText() {
        return actRspText;
    }

    @JsonProperty("ActRspText")
    public void setActRspText(String actRspText) {
        this.actRspText = actRspText;
    }

    public ActivityResponse withActRspText(String actRspText) {
        this.actRspText = actRspText;
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

    public ActivityResponse withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("protocolRevEventActivityId", protocolRevEventActivityId).append("actRspId", actRspId).append("actId", actId).append("actRspSeq", actRspSeq).append("actRspValue", actRspValue).append("actRspText", actRspText).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(protocolRevEventActivityId).append(actRspValue).append(actId).append(actRspSeq).append(actRspText).append(additionalProperties).append(actRspId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ActivityResponse)) {
            return false;
        }
        ActivityResponse rhs = ((ActivityResponse) other);
        return new EqualsBuilder().append(protocolRevEventActivityId, rhs.protocolRevEventActivityId).append(actRspValue, rhs.actRspValue).append(actId, rhs.actId).append(actRspSeq, rhs.actRspSeq).append(actRspText, rhs.actRspText).append(additionalProperties, rhs.additionalProperties).append(actRspId, rhs.actRspId).isEquals();
    }

}

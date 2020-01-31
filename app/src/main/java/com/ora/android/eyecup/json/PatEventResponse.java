
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
    "ActivityId",
    "ActivityTxt",
    "ProtRevEvtApplyTo",
    "ResponseTypeId",
    "ResponseTypeCode",
    "ActivityResponseId",
    "ResponseVal",
    "ResponseTxt",
    "ResponseDt"
})
public class PatEventResponse implements Serializable
{

    @JsonProperty("ProtocolRevEventActivityId")
    private Long protocolRevEventActivityId;
    @JsonProperty("ActivityId")
    private Long activityId;
    @JsonProperty("ActivityTxt")
    private String activityTxt;
    @JsonProperty("ProtRevEvtApplyTo")
    private String protRevEvtApplyTo;
    @JsonProperty("ResponseTypeId")
    private Long responseTypeId;
    @JsonProperty("ResponseTypeCode")
    private String responseTypeCode;
    @JsonProperty("ActivityResponseId")
    private Long activityResponseId;
    @JsonProperty("ResponseVal")
    private Long responseVal;
    @JsonProperty("ResponseTxt")
    private String responseTxt;
    @JsonProperty("ResponseDt")
    private String responseDt;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = -5635973480008566286L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PatEventResponse() {
    }

    /**
     * 
     * @param responseTxt
     * @param activityId
     * @param protocolRevEventActivityId
     * @param activityTxt
     * @param responseDt
     * @param protRevEvtApplyTo
     * @param responseTypeId
     * @param activityResponseId
     * @param responseTypeCode
     * @param responseVal
     */
    public PatEventResponse(Long protocolRevEventActivityId, Long activityId, String activityTxt, String protRevEvtApplyTo, Long responseTypeId, String responseTypeCode, Long activityResponseId, Long responseVal, String responseTxt, String responseDt) {
        super();
        this.protocolRevEventActivityId = protocolRevEventActivityId;
        this.activityId = activityId;
        this.activityTxt = activityTxt;
        this.protRevEvtApplyTo = protRevEvtApplyTo;
        this.responseTypeId = responseTypeId;
        this.responseTypeCode = responseTypeCode;
        this.activityResponseId = activityResponseId;
        this.responseVal = responseVal;
        this.responseTxt = responseTxt;
        this.responseDt = responseDt;
    }

    @JsonProperty("ProtocolRevEventActivityId")
    public Long getProtocolRevEventActivityId() {
        return protocolRevEventActivityId;
    }

    @JsonProperty("ProtocolRevEventActivityId")
    public void setProtocolRevEventActivityId(Long protocolRevEventActivityId) {
        this.protocolRevEventActivityId = protocolRevEventActivityId;
    }

    public PatEventResponse withProtocolRevEventActivityId(Long protocolRevEventActivityId) {
        this.protocolRevEventActivityId = protocolRevEventActivityId;
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

    public PatEventResponse withActivityId(Long activityId) {
        this.activityId = activityId;
        return this;
    }

    @JsonProperty("ActivityTxt")
    public String getActivityTxt() {
        return activityTxt;
    }

    @JsonProperty("ActivityTxt")
    public void setActivityTxt(String activityTxt) {
        this.activityTxt = activityTxt;
    }

    public PatEventResponse withActivityTxt(String activityTxt) {
        this.activityTxt = activityTxt;
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

    public PatEventResponse withProtRevEvtApplyTo(String protRevEvtApplyTo) {
        this.protRevEvtApplyTo = protRevEvtApplyTo;
        return this;
    }

    @JsonProperty("ResponseTypeId")
    public Long getResponseTypeId() {
        return responseTypeId;
    }

    @JsonProperty("ResponseTypeId")
    public void setResponseTypeId(Long responseTypeId) {
        this.responseTypeId = responseTypeId;
    }

    public PatEventResponse withResponseTypeId(Long responseTypeId) {
        this.responseTypeId = responseTypeId;
        return this;
    }

    @JsonProperty("ResponseTypeCode")
    public String getResponseTypeCode() {
        return responseTypeCode;
    }

    @JsonProperty("ResponseTypeCode")
    public void setResponseTypeCode(String responseTypeCode) {
        this.responseTypeCode = responseTypeCode;
    }

    public PatEventResponse withResponseTypeCode(String responseTypeCode) {
        this.responseTypeCode = responseTypeCode;
        return this;
    }

    @JsonProperty("ActivityResponseId")
    public Long getActivityResponseId() {
        return activityResponseId;
    }

    @JsonProperty("ActivityResponseId")
    public void setActivityResponseId(Long activityResponseId) {
        this.activityResponseId = activityResponseId;
    }

    public PatEventResponse withActivityResponseId(Long activityResponseId) {
        this.activityResponseId = activityResponseId;
        return this;
    }

    @JsonProperty("ResponseVal")
    public Long getResponseVal() {
        return responseVal;
    }

    @JsonProperty("ResponseVal")
    public void setResponseVal(Long responseVal) {
        this.responseVal = responseVal;
    }

    public PatEventResponse withResponseVal(Long responseVal) {
        this.responseVal = responseVal;
        return this;
    }

    @JsonProperty("ResponseTxt")
    public String getResponseTxt() {
        return responseTxt;
    }

    @JsonProperty("ResponseTxt")
    public void setResponseTxt(String responseTxt) {
        this.responseTxt = responseTxt;
    }

    public PatEventResponse withResponseTxt(String responseTxt) {
        this.responseTxt = responseTxt;
        return this;
    }

    @JsonProperty("ResponseDt")
    public String getResponseDt() {
        return responseDt;
    }

    @JsonProperty("ResponseDt")
    public void setResponseDt(String responseDt) {
        this.responseDt = responseDt;
    }

    public PatEventResponse withResponseDt(String responseDt) {
        this.responseDt = responseDt;
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

    public PatEventResponse withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("protocolRevEventActivityId", protocolRevEventActivityId).append("activityId", activityId).append("activityTxt", activityTxt).append("protRevEvtApplyTo", protRevEvtApplyTo).append("responseTypeId", responseTypeId).append("responseTypeCode", responseTypeCode).append("activityResponseId", activityResponseId).append("responseVal", responseVal).append("responseTxt", responseTxt).append("responseDt", responseDt).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(responseTxt).append(activityId).append(protocolRevEventActivityId).append(activityTxt).append(responseDt).append(protRevEvtApplyTo).append(responseTypeId).append(activityResponseId).append(additionalProperties).append(responseTypeCode).append(responseVal).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PatEventResponse)) {
            return false;
        }
        PatEventResponse rhs = ((PatEventResponse) other);
        return new EqualsBuilder().append(responseTxt, rhs.responseTxt).append(activityId, rhs.activityId).append(protocolRevEventActivityId, rhs.protocolRevEventActivityId).append(activityTxt, rhs.activityTxt).append(responseDt, rhs.responseDt).append(protRevEvtApplyTo, rhs.protRevEvtApplyTo).append(responseTypeId, rhs.responseTypeId).append(activityResponseId, rhs.activityResponseId).append(additionalProperties, rhs.additionalProperties).append(responseTypeCode, rhs.responseTypeCode).append(responseVal, rhs.responseVal).isEquals();
    }

}

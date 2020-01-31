
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
    "ProtocolRevId",
    "ProtocolName",
    "ProtocolRevName",
    "ProtocolRevDt",
    "ProtocolRevEventCnt",
    "ProtocolRevEvents"
})
public class ProtocolRevision implements Serializable
{

    @JsonProperty("ProtocolRevId")
    private Long protocolRevId;
    @JsonProperty("ProtocolName")
    private String protocolName;
    @JsonProperty("ProtocolRevName")
    private String protocolRevName;
    @JsonProperty("ProtocolRevDt")
    private String protocolRevDt;
    @JsonProperty("ProtocolRevEventCnt")
    private Long protocolRevEventCnt;
    @JsonProperty("ProtocolRevEvents")
    private List<ProtocolRevEvent> protocolRevEvents = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = -4809631908937066561L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProtocolRevision() {
    }

    /**
     * 
     * @param protocolRevId
     * @param protocolRevEventCnt
     * @param protocolName
     * @param protocolRevDt
     * @param protocolRevEvents
     * @param protocolRevName
     */
    public ProtocolRevision(Long protocolRevId, String protocolName, String protocolRevName, String protocolRevDt, Long protocolRevEventCnt, List<ProtocolRevEvent> protocolRevEvents) {
        super();
        this.protocolRevId = protocolRevId;
        this.protocolName = protocolName;
        this.protocolRevName = protocolRevName;
        this.protocolRevDt = protocolRevDt;
        this.protocolRevEventCnt = protocolRevEventCnt;
        this.protocolRevEvents = protocolRevEvents;
    }

    @JsonProperty("ProtocolRevId")
    public Long getProtocolRevId() {
        return protocolRevId;
    }

    @JsonProperty("ProtocolRevId")
    public void setProtocolRevId(Long protocolRevId) {
        this.protocolRevId = protocolRevId;
    }

    public ProtocolRevision withProtocolRevId(Long protocolRevId) {
        this.protocolRevId = protocolRevId;
        return this;
    }

    @JsonProperty("ProtocolName")
    public String getProtocolName() {
        return protocolName;
    }

    @JsonProperty("ProtocolName")
    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public ProtocolRevision withProtocolName(String protocolName) {
        this.protocolName = protocolName;
        return this;
    }

    @JsonProperty("ProtocolRevName")
    public String getProtocolRevName() {
        return protocolRevName;
    }

    @JsonProperty("ProtocolRevName")
    public void setProtocolRevName(String protocolRevName) {
        this.protocolRevName = protocolRevName;
    }

    public ProtocolRevision withProtocolRevName(String protocolRevName) {
        this.protocolRevName = protocolRevName;
        return this;
    }

    @JsonProperty("ProtocolRevDt")
    public String getProtocolRevDt() {
        return protocolRevDt;
    }

    @JsonProperty("ProtocolRevDt")
    public void setProtocolRevDt(String protocolRevDt) {
        this.protocolRevDt = protocolRevDt;
    }

    public ProtocolRevision withProtocolRevDt(String protocolRevDt) {
        this.protocolRevDt = protocolRevDt;
        return this;
    }

    @JsonProperty("ProtocolRevEventCnt")
    public Long getProtocolRevEventCnt() {
        return protocolRevEventCnt;
    }

    @JsonProperty("ProtocolRevEventCnt")
    public void setProtocolRevEventCnt(Long protocolRevEventCnt) {
        this.protocolRevEventCnt = protocolRevEventCnt;
    }

    public ProtocolRevision withProtocolRevEventCnt(Long protocolRevEventCnt) {
        this.protocolRevEventCnt = protocolRevEventCnt;
        return this;
    }

    @JsonProperty("ProtocolRevEvents")
    public List<ProtocolRevEvent> getProtocolRevEvents() {
        return protocolRevEvents;
    }

    @JsonProperty("ProtocolRevEvents")
    public void setProtocolRevEvents(List<ProtocolRevEvent> protocolRevEvents) {
        this.protocolRevEvents = protocolRevEvents;
    }

    public ProtocolRevision withProtocolRevEvents(List<ProtocolRevEvent> protocolRevEvents) {
        this.protocolRevEvents = protocolRevEvents;
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

    public ProtocolRevision withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("protocolRevId", protocolRevId).append("protocolName", protocolName).append("protocolRevName", protocolRevName).append("protocolRevDt", protocolRevDt).append("protocolRevEventCnt", protocolRevEventCnt).append("protocolRevEvents", protocolRevEvents).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(protocolRevId).append(protocolRevEventCnt).append(protocolName).append(protocolRevDt).append(protocolRevEvents).append(additionalProperties).append(protocolRevName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ProtocolRevision)) {
            return false;
        }
        ProtocolRevision rhs = ((ProtocolRevision) other);
        return new EqualsBuilder().append(protocolRevId, rhs.protocolRevId).append(protocolRevEventCnt, rhs.protocolRevEventCnt).append(protocolName, rhs.protocolName).append(protocolRevDt, rhs.protocolRevDt).append(protocolRevEvents, rhs.protocolRevEvents).append(additionalProperties, rhs.additionalProperties).append(protocolRevName, rhs.protocolRevName).isEquals();
    }

}

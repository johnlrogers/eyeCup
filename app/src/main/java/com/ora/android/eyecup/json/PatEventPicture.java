
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
    "PictureFileName",
    "PictureDt"
})
public class PatEventPicture implements Serializable
{

    @JsonProperty("ProtocolRevEventActivityId")
    private Long protocolRevEventActivityId;
    @JsonProperty("ActivityId")
    private Long activityId;
    @JsonProperty("PictureFileName")
    private String pictureFileName;
    @JsonProperty("PictureDt")
    private String pictureDt;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = 2185047259796091839L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PatEventPicture() {
    }

    /**
     * 
     * @param activityId
     * @param protocolRevEventActivityId
     * @param pictureDt
     * @param pictureFileName
     */
    public PatEventPicture(Long protocolRevEventActivityId, Long activityId, String pictureFileName, String pictureDt) {
        super();
        this.protocolRevEventActivityId = protocolRevEventActivityId;
        this.activityId = activityId;
        this.pictureFileName = pictureFileName;
        this.pictureDt = pictureDt;
    }

    @JsonProperty("ProtocolRevEventActivityId")
    public Long getProtocolRevEventActivityId() {
        return protocolRevEventActivityId;
    }

    @JsonProperty("ProtocolRevEventActivityId")
    public void setProtocolRevEventActivityId(Long protocolRevEventActivityId) {
        this.protocolRevEventActivityId = protocolRevEventActivityId;
    }

    public PatEventPicture withProtocolRevEventActivityId(Long protocolRevEventActivityId) {
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

    public PatEventPicture withActivityId(Long activityId) {
        this.activityId = activityId;
        return this;
    }

    @JsonProperty("PictureFileName")
    public String getPictureFileName() {
        return pictureFileName;
    }

    @JsonProperty("PictureFileName")
    public void setPictureFileName(String pictureFileName) {
        this.pictureFileName = pictureFileName;
    }

    public PatEventPicture withPictureFileName(String pictureFileName) {
        this.pictureFileName = pictureFileName;
        return this;
    }

    @JsonProperty("PictureDt")
    public String getPictureDt() {
        return pictureDt;
    }

    @JsonProperty("PictureDt")
    public void setPictureDt(String pictureDt) {
        this.pictureDt = pictureDt;
    }

    public PatEventPicture withPictureDt(String pictureDt) {
        this.pictureDt = pictureDt;
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

    public PatEventPicture withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("protocolRevEventActivityId", protocolRevEventActivityId).append("activityId", activityId).append("pictureFileName", pictureFileName).append("pictureDt", pictureDt).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(activityId).append(protocolRevEventActivityId).append(pictureDt).append(additionalProperties).append(pictureFileName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PatEventPicture)) {
            return false;
        }
        PatEventPicture rhs = ((PatEventPicture) other);
        return new EqualsBuilder().append(activityId, rhs.activityId).append(protocolRevEventActivityId, rhs.protocolRevEventActivityId).append(pictureDt, rhs.pictureDt).append(additionalProperties, rhs.additionalProperties).append(pictureFileName, rhs.pictureFileName).isEquals();
    }

}

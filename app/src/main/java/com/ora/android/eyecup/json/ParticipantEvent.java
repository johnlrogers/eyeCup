
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
    "PatEventId",
    "ProtocolRevId",
    "ProtocolRevEventId",
    "ParticipantId",
    "YearId",
    "DepartmentId",
    "StudyId",
    "LocationId",
    "SubjectId",
    "DeviceId",
    "DeviceAppId",
    "EventId",
    "PatEventDtStart",
    "PatEventDtEnd",
    "PatEventDtUpload",
    "PatEventResponseCnt",
    "PatEventPictureCnt",
    "PatEventResponses",
    "PatEventPictures"
})
public class ParticipantEvent implements Serializable
{

    @JsonProperty("PatEventId")
    private Long patEventId;
    @JsonProperty("ProtocolRevId")
    private Long protocolRevId;
    @JsonProperty("ProtocolRevEventId")
    private Long protocolRevEventId;
    @JsonProperty("ParticipantId")
    private String participantId;
    @JsonProperty("YearId")
    private String yearId;
    @JsonProperty("DepartmentId")
    private String departmentId;
    @JsonProperty("StudyId")
    private String studyId;
    @JsonProperty("LocationId")
    private String locationId;
    @JsonProperty("SubjectId")
    private String subjectId;
    @JsonProperty("DeviceId")
    private String deviceId;
    @JsonProperty("DeviceAppId")
    private String deviceAppId;
    @JsonProperty("EventId")
    private Long eventId;
    @JsonProperty("PatEventDtStart")
    private String patEventDtStart;
    @JsonProperty("PatEventDtEnd")
    private String patEventDtEnd;
    @JsonProperty("PatEventDtUpload")
    private String patEventDtUpload;
    @JsonProperty("PatEventResponseCnt")
    private Long patEventResponseCnt;
    @JsonProperty("PatEventPictureCnt")
    private Long patEventPictureCnt;
    @JsonProperty("PatEventResponses")
    private List<PatEventResponse> patEventResponses = null;
    @JsonProperty("PatEventPictures")
    private List<PatEventPicture> patEventPictures = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = 1126823942830769475L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ParticipantEvent() {
    }

    /**
     * 
     * @param eventId
     * @param patEventResponseCnt
     * @param patEventDtStart
     * @param deviceAppId
     * @param departmentId
     * @param patEventPictures
     * @param deviceId
     * @param subjectId
     * @param yearId
     * @param participantId
     * @param patEventDtEnd
     * @param protocolRevEventId
     * @param protocolRevId
     * @param patEventDtUpload
     * @param patEventPictureCnt
     * @param patEventResponses
     * @param patEventId
     * @param locationId
     * @param studyId
     */
    public ParticipantEvent(Long patEventId, Long protocolRevId, Long protocolRevEventId, String participantId, String yearId, String departmentId, String studyId, String locationId, String subjectId, String deviceId, String deviceAppId, Long eventId, String patEventDtStart, String patEventDtEnd, String patEventDtUpload, Long patEventResponseCnt, Long patEventPictureCnt, List<PatEventResponse> patEventResponses, List<PatEventPicture> patEventPictures) {
        super();
        this.patEventId = patEventId;
        this.protocolRevId = protocolRevId;
        this.protocolRevEventId = protocolRevEventId;
        this.participantId = participantId;
        this.yearId = yearId;
        this.departmentId = departmentId;
        this.studyId = studyId;
        this.locationId = locationId;
        this.subjectId = subjectId;
        this.deviceId = deviceId;
        this.deviceAppId = deviceAppId;
        this.eventId = eventId;
        this.patEventDtStart = patEventDtStart;
        this.patEventDtEnd = patEventDtEnd;
        this.patEventDtUpload = patEventDtUpload;
        this.patEventResponseCnt = patEventResponseCnt;
        this.patEventPictureCnt = patEventPictureCnt;
        this.patEventResponses = patEventResponses;
        this.patEventPictures = patEventPictures;
    }

    @JsonProperty("PatEventId")
    public Long getPatEventId() {
        return patEventId;
    }

    @JsonProperty("PatEventId")
    public void setPatEventId(Long patEventId) {
        this.patEventId = patEventId;
    }

    public ParticipantEvent withPatEventId(Long patEventId) {
        this.patEventId = patEventId;
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

    public ParticipantEvent withProtocolRevId(Long protocolRevId) {
        this.protocolRevId = protocolRevId;
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

    public ParticipantEvent withProtocolRevEventId(Long protocolRevEventId) {
        this.protocolRevEventId = protocolRevEventId;
        return this;
    }

    @JsonProperty("ParticipantId")
    public String getParticipantId() {
        return participantId;
    }

    @JsonProperty("ParticipantId")
    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public ParticipantEvent withParticipantId(String participantId) {
        this.participantId = participantId;
        return this;
    }

    @JsonProperty("YearId")
    public String getYearId() {
        return yearId;
    }

    @JsonProperty("YearId")
    public void setYearId(String yearId) {
        this.yearId = yearId;
    }

    public ParticipantEvent withYearId(String yearId) {
        this.yearId = yearId;
        return this;
    }

    @JsonProperty("DepartmentId")
    public String getDepartmentId() {
        return departmentId;
    }

    @JsonProperty("DepartmentId")
    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public ParticipantEvent withDepartmentId(String departmentId) {
        this.departmentId = departmentId;
        return this;
    }

    @JsonProperty("StudyId")
    public String getStudyId() {
        return studyId;
    }

    @JsonProperty("StudyId")
    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public ParticipantEvent withStudyId(String studyId) {
        this.studyId = studyId;
        return this;
    }

    @JsonProperty("LocationId")
    public String getLocationId() {
        return locationId;
    }

    @JsonProperty("LocationId")
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public ParticipantEvent withLocationId(String locationId) {
        this.locationId = locationId;
        return this;
    }

    @JsonProperty("SubjectId")
    public String getSubjectId() {
        return subjectId;
    }

    @JsonProperty("SubjectId")
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public ParticipantEvent withSubjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    @JsonProperty("DeviceId")
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty("DeviceId")
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public ParticipantEvent withDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    @JsonProperty("DeviceAppId")
    public String getDeviceAppId() {
        return deviceAppId;
    }

    @JsonProperty("DeviceAppId")
    public void setDeviceAppId(String deviceAppId) {
        this.deviceAppId = deviceAppId;
    }

    public ParticipantEvent withDeviceAppId(String deviceAppId) {
        this.deviceAppId = deviceAppId;
        return this;
    }

    @JsonProperty("EventId")
    public Long getEventId() {
        return eventId;
    }

    @JsonProperty("EventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public ParticipantEvent withEventId(Long eventId) {
        this.eventId = eventId;
        return this;
    }

    @JsonProperty("PatEventDtStart")
    public String getPatEventDtStart() {
        return patEventDtStart;
    }

    @JsonProperty("PatEventDtStart")
    public void setPatEventDtStart(String patEventDtStart) {
        this.patEventDtStart = patEventDtStart;
    }

    public ParticipantEvent withPatEventDtStart(String patEventDtStart) {
        this.patEventDtStart = patEventDtStart;
        return this;
    }

    @JsonProperty("PatEventDtEnd")
    public String getPatEventDtEnd() {
        return patEventDtEnd;
    }

    @JsonProperty("PatEventDtEnd")
    public void setPatEventDtEnd(String patEventDtEnd) {
        this.patEventDtEnd = patEventDtEnd;
    }

    public ParticipantEvent withPatEventDtEnd(String patEventDtEnd) {
        this.patEventDtEnd = patEventDtEnd;
        return this;
    }

    @JsonProperty("PatEventDtUpload")
    public String getPatEventDtUpload() {
        return patEventDtUpload;
    }

    @JsonProperty("PatEventDtUpload")
    public void setPatEventDtUpload(String patEventDtUpload) {
        this.patEventDtUpload = patEventDtUpload;
    }

    public ParticipantEvent withPatEventDtUpload(String patEventDtUpload) {
        this.patEventDtUpload = patEventDtUpload;
        return this;
    }

    @JsonProperty("PatEventResponseCnt")
    public Long getPatEventResponseCnt() {
        return patEventResponseCnt;
    }

    @JsonProperty("PatEventResponseCnt")
    public void setPatEventResponseCnt(Long patEventResponseCnt) {
        this.patEventResponseCnt = patEventResponseCnt;
    }

    public ParticipantEvent withPatEventResponseCnt(Long patEventResponseCnt) {
        this.patEventResponseCnt = patEventResponseCnt;
        return this;
    }

    @JsonProperty("PatEventPictureCnt")
    public Long getPatEventPictureCnt() {
        return patEventPictureCnt;
    }

    @JsonProperty("PatEventPictureCnt")
    public void setPatEventPictureCnt(Long patEventPictureCnt) {
        this.patEventPictureCnt = patEventPictureCnt;
    }

    public ParticipantEvent withPatEventPictureCnt(Long patEventPictureCnt) {
        this.patEventPictureCnt = patEventPictureCnt;
        return this;
    }

    @JsonProperty("PatEventResponses")
    public List<PatEventResponse> getPatEventResponses() {
        return patEventResponses;
    }

    @JsonProperty("PatEventResponses")
    public void setPatEventResponses(List<PatEventResponse> patEventResponses) {
        this.patEventResponses = patEventResponses;
    }

    public ParticipantEvent withPatEventResponses(List<PatEventResponse> patEventResponses) {
        this.patEventResponses = patEventResponses;
        return this;
    }

    @JsonProperty("PatEventPictures")
    public List<PatEventPicture> getPatEventPictures() {
        return patEventPictures;
    }

    @JsonProperty("PatEventPictures")
    public void setPatEventPictures(List<PatEventPicture> patEventPictures) {
        this.patEventPictures = patEventPictures;
    }

    public ParticipantEvent withPatEventPictures(List<PatEventPicture> patEventPictures) {
        this.patEventPictures = patEventPictures;
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

    public ParticipantEvent withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("patEventId", patEventId).append("protocolRevId", protocolRevId).append("protocolRevEventId", protocolRevEventId).append("participantId", participantId).append("yearId", yearId).append("departmentId", departmentId).append("studyId", studyId).append("locationId", locationId).append("subjectId", subjectId).append("deviceId", deviceId).append("deviceAppId", deviceAppId).append("eventId", eventId).append("patEventDtStart", patEventDtStart).append("patEventDtEnd", patEventDtEnd).append("patEventDtUpload", patEventDtUpload).append("patEventResponseCnt", patEventResponseCnt).append("patEventPictureCnt", patEventPictureCnt).append("patEventResponses", patEventResponses).append("patEventPictures", patEventPictures).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(patEventResponseCnt).append(patEventDtStart).append(deviceAppId).append(departmentId).append(patEventPictures).append(deviceId).append(subjectId).append(yearId).append(participantId).append(patEventDtEnd).append(protocolRevEventId).append(protocolRevId).append(patEventDtUpload).append(patEventPictureCnt).append(patEventResponses).append(patEventId).append(locationId).append(studyId).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ParticipantEvent)) {
            return false;
        }
        ParticipantEvent rhs = ((ParticipantEvent) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(patEventResponseCnt, rhs.patEventResponseCnt).append(patEventDtStart, rhs.patEventDtStart).append(deviceAppId, rhs.deviceAppId).append(departmentId, rhs.departmentId).append(patEventPictures, rhs.patEventPictures).append(deviceId, rhs.deviceId).append(subjectId, rhs.subjectId).append(yearId, rhs.yearId).append(participantId, rhs.participantId).append(patEventDtEnd, rhs.patEventDtEnd).append(protocolRevEventId, rhs.protocolRevEventId).append(protocolRevId, rhs.protocolRevId).append(patEventDtUpload, rhs.patEventDtUpload).append(patEventPictureCnt, rhs.patEventPictureCnt).append(patEventResponses, rhs.patEventResponses).append(patEventId, rhs.patEventId).append(locationId, rhs.locationId).append(studyId, rhs.studyId).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}

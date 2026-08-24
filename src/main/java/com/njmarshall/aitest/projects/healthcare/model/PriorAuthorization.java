package com.njmarshall.aitest.projects.healthcare.model;

/**
 * Prior authorization resource / request payload. Kept as a mutable POJO so factory mutators can
 * invalidate a single field for negative-path fixtures, and so Jackson can serialize it as a
 * request body.
 */
public class PriorAuthorization {

    private String id;
    private String patientId;
    private String memberId;
    private String procedureCode;
    private String diagnosisCode;
    private PriorAuthStatus status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getProcedureCode() {
        return procedureCode;
    }

    public void setProcedureCode(String procedureCode) {
        this.procedureCode = procedureCode;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }

    public PriorAuthStatus getStatus() {
        return status;
    }

    public void setStatus(PriorAuthStatus status) {
        this.status = status;
    }
}

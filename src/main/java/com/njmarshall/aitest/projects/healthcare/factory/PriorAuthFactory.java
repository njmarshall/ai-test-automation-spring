package com.njmarshall.aitest.projects.healthcare.factory;

import com.njmarshall.aitest.projects.healthcare.model.PriorAuthorization;
import com.njmarshall.aitest.projects.healthcare.model.PriorAuthStatus;
import com.njmarshall.aitest.shared.crtp.AbstractEntityFactory;

import java.util.UUID;

/**
 * Builds valid-by-default {@link PriorAuthorization} fixtures. Extends the CRTP base, so its own
 * fluent setters and the base ones ({@code with}, {@code build}, {@code buildMany}) all return
 * {@code PriorAuthFactory} and chain without casts.
 */
public class PriorAuthFactory
        extends AbstractEntityFactory<PriorAuthorization, PriorAuthFactory> {

    private String patientId = "patient-" + UUID.randomUUID();
    private String memberId = "M" + (100000 + (int) (Math.random() * 899999));
    private String procedureCode = "27447";      // TKA, a commonly pre-authorized procedure
    private String diagnosisCode = "M17.11";     // unilateral primary osteoarthritis, right knee
    private PriorAuthStatus status = PriorAuthStatus.RECEIVED;

    @Override
    protected PriorAuthFactory self() {
        return this;
    }

    public PriorAuthFactory withPatientId(String patientId) {
        this.patientId = patientId;
        return this;
    }

    public PriorAuthFactory withMemberId(String memberId) {
        this.memberId = memberId;
        return this;
    }

    public PriorAuthFactory withProcedureCode(String procedureCode) {
        this.procedureCode = procedureCode;
        return this;
    }

    public PriorAuthFactory withDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
        return this;
    }

    public PriorAuthFactory withStatus(PriorAuthStatus status) {
        this.status = status;
        return this;
    }

    @Override
    protected PriorAuthorization create() {
        PriorAuthorization pa = new PriorAuthorization();
        pa.setPatientId(patientId);
        pa.setMemberId(memberId);
        pa.setProcedureCode(procedureCode);
        pa.setDiagnosisCode(diagnosisCode);
        pa.setStatus(status);
        return pa;
    }
}

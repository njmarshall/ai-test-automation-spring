package com.njmarshall.aitest.projects.healthcare.validator;

import com.njmarshall.aitest.projects.healthcare.model.PriorAuthorization;
import com.njmarshall.aitest.projects.healthcare.model.PriorAuthStatus;
import com.njmarshall.aitest.shared.crtp.AbstractValidator;

/**
 * Domain validator for prior authorizations. Adds workflow-specific checks on top of the CRTP
 * base. Because the base returns {@code PriorAuthValidator}, these subclass methods stay reachable
 * anywhere in a fluent chain.
 */
public class PriorAuthValidator
        extends AbstractValidator<PriorAuthorization, PriorAuthValidator> {

    public PriorAuthValidator(PriorAuthorization subject) {
        super(subject);
    }

    @Override
    protected PriorAuthValidator self() {
        return this;
    }

    public PriorAuthValidator hasId() {
        return expect(subject.getId() != null && !subject.getId().isBlank(), "prior auth id is present");
    }

    public PriorAuthValidator hasStatus(PriorAuthStatus expected) {
        return expectEquals(subject.getStatus(), expected, "status");
    }

    public PriorAuthValidator isTerminal() {
        return expect(
                subject.getStatus() != null && subject.getStatus().isTerminal(),
                "status is terminal but was " + subject.getStatus());
    }

    public PriorAuthValidator hasValidProcedureCode() {
        return expect(
                subject.getProcedureCode() != null && subject.getProcedureCode().matches("\\d{5}"),
                "procedure code is a 5-digit CPT code");
    }
}

package com.njmarshall.aitest.projects.insurance.validator;

import com.njmarshall.aitest.projects.insurance.model.Claim;
import com.njmarshall.aitest.projects.insurance.model.ClaimStatus;
import com.njmarshall.aitest.shared.crtp.AbstractValidator;

import java.math.BigDecimal;

/** Domain validator for insurance claims. */
public class ClaimValidator extends AbstractValidator<Claim, ClaimValidator> {

    public ClaimValidator(Claim subject) {
        super(subject);
    }

    @Override
    protected ClaimValidator self() {
        return this;
    }

    public ClaimValidator hasId() {
        return expect(subject.getId() != null && !subject.getId().isBlank(), "claim id is present");
    }

    public ClaimValidator hasStatus(ClaimStatus expected) {
        return expectEquals(subject.getStatus(), expected, "status");
    }

    public ClaimValidator isTerminal() {
        return expect(
                subject.getStatus() != null && subject.getStatus().isTerminal(),
                "status is terminal but was " + subject.getStatus());
    }

    public ClaimValidator hasPositiveBilledAmount() {
        return expect(
                subject.getBilledAmount() != null
                        && subject.getBilledAmount().compareTo(BigDecimal.ZERO) > 0,
                "billed amount is positive");
    }
}

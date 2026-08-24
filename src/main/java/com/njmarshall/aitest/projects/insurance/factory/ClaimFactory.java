package com.njmarshall.aitest.projects.insurance.factory;

import com.njmarshall.aitest.projects.insurance.model.Claim;
import com.njmarshall.aitest.projects.insurance.model.ClaimStatus;
import com.njmarshall.aitest.shared.crtp.AbstractEntityFactory;

import java.math.BigDecimal;
import java.util.UUID;

/** Builds valid-by-default {@link Claim} fixtures via the shared CRTP factory base. */
public class ClaimFactory extends AbstractEntityFactory<Claim, ClaimFactory> {

    private String memberId = "M" + (100000 + (int) (Math.random() * 899999));
    private String providerId = "provider-" + UUID.randomUUID();
    private String cptCode = "99213";                 // office visit, established patient
    private BigDecimal billedAmount = new BigDecimal("184.00");
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    @Override
    protected ClaimFactory self() {
        return this;
    }

    public ClaimFactory withMemberId(String memberId) {
        this.memberId = memberId;
        return this;
    }

    public ClaimFactory withProviderId(String providerId) {
        this.providerId = providerId;
        return this;
    }

    public ClaimFactory withCptCode(String cptCode) {
        this.cptCode = cptCode;
        return this;
    }

    public ClaimFactory withBilledAmount(BigDecimal billedAmount) {
        this.billedAmount = billedAmount;
        return this;
    }

    public ClaimFactory withStatus(ClaimStatus status) {
        this.status = status;
        return this;
    }

    @Override
    protected Claim create() {
        Claim claim = new Claim();
        claim.setMemberId(memberId);
        claim.setProviderId(providerId);
        claim.setCptCode(cptCode);
        claim.setBilledAmount(billedAmount);
        claim.setStatus(status);
        return claim;
    }
}

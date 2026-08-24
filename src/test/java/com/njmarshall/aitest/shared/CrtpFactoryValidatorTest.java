package com.njmarshall.aitest.shared;

import com.njmarshall.aitest.projects.healthcare.factory.PriorAuthFactory;
import com.njmarshall.aitest.projects.healthcare.model.PriorAuthStatus;
import com.njmarshall.aitest.projects.healthcare.model.PriorAuthorization;
import com.njmarshall.aitest.projects.healthcare.validator.PriorAuthValidator;
import com.njmarshall.aitest.shared.crtp.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the CRTP factory and validator hierarchies. The key thing these tests prove, beyond
 * behavior, is that base methods return the concrete subclass: every chain below mixes base and
 * subclass calls in any order and still compiles.
 */
class CrtpFactoryValidatorTest {

    @Test
    void factoryBuildsValidByDefaultEntity() {
        PriorAuthorization pa = new PriorAuthFactory().build();

        assertThat(pa.getStatus()).isEqualTo(PriorAuthStatus.RECEIVED);
        assertThat(pa.getProcedureCode()).matches("\\d{5}");
        assertThat(pa.getPatientId()).isNotBlank();
    }

    @Test
    void fluentSettersAndBaseMethodsChainAsTheSubclassType() {
        // withProcedureCode (subclass) then with (base) then buildMany (base) all return PriorAuthFactory.
        var entities = new PriorAuthFactory()
                .withProcedureCode("29881")
                .withStatus(PriorAuthStatus.PENDING)
                .with(pa -> pa.setId("pa-seed"))
                .buildMany(3);

        assertThat(entities).hasSize(3);
        assertThat(entities).allSatisfy(pa -> {
            assertThat(pa.getProcedureCode()).isEqualTo("29881");
            assertThat(pa.getStatus()).isEqualTo(PriorAuthStatus.PENDING);
            assertThat(pa.getId()).isEqualTo("pa-seed");
        });
    }

    @Test
    void mutatorCanInvalidateOneFieldForNegativeFixtures() {
        PriorAuthorization invalid = new PriorAuthFactory()
                .with(pa -> pa.setProcedureCode("not-a-cpt"))
                .build();

        boolean valid = new PriorAuthValidator(invalid)
                .hasValidProcedureCode()
                .isValid();

        assertThat(valid).isFalse();
    }

    @Test
    void validatorChainMixesBaseAndSubclassMethodsAndVerifies() {
        PriorAuthorization pa = new PriorAuthFactory()
                .withStatus(PriorAuthStatus.APPROVED)
                .with(p -> p.setId("pa-1"))
                .build();

        PriorAuthorization verified = new PriorAuthValidator(pa)
                .hasId()                              // subclass
                .expect(true, "sanity")               // base
                .hasStatus(PriorAuthStatus.APPROVED)  // subclass
                .isTerminal()                         // subclass
                .verify();

        assertThat(verified).isSameAs(pa);
    }

    @Test
    void verifyThrowsWithEveryCollectedFailure() {
        PriorAuthorization pa = new PriorAuthFactory()
                .withStatus(PriorAuthStatus.IN_REVIEW)
                .build(); // no id set

        assertThatThrownBy(() -> new PriorAuthValidator(pa)
                .hasId()
                .hasStatus(PriorAuthStatus.APPROVED)
                .isTerminal()
                .verify())
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("id")
                .hasMessageContaining("status");
    }
}

package com.etp.ticketservice.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyUtilsTest {

    @Test
    void toMinorUnits_convertsDecimalDollarsToCents() {
        assertThat(MoneyUtils.toMinorUnits(19.99)).isEqualTo(1999L);
    }

    @Test
    void toMinorUnits_roundsHalfCentUp() {
        // Math.round rounds half-up -- 19.995 -> 1999.5 -> 2000, not 1999.
        assertThat(MoneyUtils.toMinorUnits(19.995)).isEqualTo(2000L);
    }

    @Test
    void toMinorUnits_null_returnsNull() {
        assertThat(MoneyUtils.toMinorUnits(null)).isNull();
    }

    @Test
    void toMajorUnits_convertsCentsToDecimalDollars() {
        assertThat(MoneyUtils.toMajorUnits(1999L)).isEqualTo(19.99);
    }

    @Test
    void toMajorUnits_null_returnsNull() {
        assertThat(MoneyUtils.toMajorUnits(null)).isNull();
    }
}

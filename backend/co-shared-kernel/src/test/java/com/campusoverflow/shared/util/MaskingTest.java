package com.campusoverflow.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MaskingTest {

    @Test
    void masksAccountNumberKeepingPrefixAndSuffix() {
        assertThat(Masking.account("2023001217")).isEqualTo("2023****17");
        assertThat(Masking.account("123")).isEqualTo("****");
    }

    @Test
    void masksEmailLocalPart() {
        assertThat(Masking.email("alice@campus.edu")).isEqualTo("a***@campus.edu");
    }
}

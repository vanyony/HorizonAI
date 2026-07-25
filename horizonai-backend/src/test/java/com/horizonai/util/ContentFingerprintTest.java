package com.horizonai.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ContentFingerprintTest {

    private final ContentFingerprint fingerprint = new ContentFingerprint();

    @Test
    void equivalentTextProducesSameFingerprint() {
        String first = fingerprint.calculate(
                "Java Virtual Threads",
                "Reduce   blocking-thread cost."
        );
        String second = fingerprint.calculate(
                "ｊａｖａ virtual threads",
                "reduce blocking-thread COST."
        );

        assertEquals(first, second);
    }

    @Test
    void contentChangeProducesDifferentFingerprint() {
        String first = fingerprint.calculate("Redis", "Cache Aside");
        String second = fingerprint.calculate("Redis", "Write Through");

        assertNotEquals(first, second);
    }
}

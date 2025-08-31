package tests;

import com.example.testsupport.annotations.RetryableTest;
import io.qameta.allure.Flaky;
import org.junit.jupiter.api.Assertions;

class SampleRetryableTest {

    @RetryableTest
    @Flaky
    void samplePasses() {
        Assertions.assertTrue(true);
    }
}

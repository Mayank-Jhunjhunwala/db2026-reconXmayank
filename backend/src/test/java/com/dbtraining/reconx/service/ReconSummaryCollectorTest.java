package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.assertThat;

class ReconSummaryCollectorTest {
    @Test
    void serialAndParallel_produceSameResult() {
        List<ReconResult> results = IntStream.range(0, 10000)
                .mapToObj(i -> i % 3 == 0
                        ? ReconResult.matched("REF-" + i)
                        : ReconResult.breakResult("REF-" + i, "MISMATCH", "detail"))
                .toList();
        ReconSummary serial = results.stream().collect(new ReconSummaryCollector());
        ReconSummary parallel = results.parallelStream().collect(new ReconSummaryCollector());
        assertThat(serial).isEqualTo(parallel);
        assertThat(serial.total()).isEqualTo(10000);
        assertThat(serial.matched()).isEqualTo(3334);
        assertThat(serial.broken()).isEqualTo(6666);
    }

    @Test
    void empty_returnsZeros() {
        ReconSummary s = List.<ReconResult>of().stream().collect(new ReconSummaryCollector());
        assertThat(s).isEqualTo(ReconSummary.empty());
    }
}

package com.reactivespring.demo.fluxandmonoplayground;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class FluxAndMonoFilterTest {

    List<String> names = Arrays.asList("adam", "anna", "jack", "jenny");

    @Test
    public void filterTest() {
        Flux<String> namesFlux = Flux.fromIterable(names)
                .filter(el -> el.startsWith("a"));

        StepVerifier.create(namesFlux)
                .expectNext("adam", "anna")
                .verifyComplete();
    }

    @Test
    public void filterLengthTest() {
        Flux<String> namesFlux = Flux.fromIterable(names)
                .filter(el -> el.length() > 4);

        StepVerifier.create(namesFlux)
                .expectNext("jenny")
                .verifyComplete();
    }
}

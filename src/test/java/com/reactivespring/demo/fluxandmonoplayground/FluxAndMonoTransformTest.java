package com.reactivespring.demo.fluxandmonoplayground;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static reactor.core.scheduler.Schedulers.parallel;

public class FluxAndMonoTransformTest {

    List<String> names = Arrays.asList("adam", "anna", "jack", "jenny");

    @Test
    public void transformUsingMap() {
        Flux<String> namesFlux = Flux.fromIterable(names)
                .map(String::toUpperCase);

        StepVerifier.create(namesFlux)
                .expectNext("ADAM", "ANNA", "JACK", "JENNY")
                .verifyComplete();
    }

    @Test
    public void transformUsingMap_Length() {
        Flux<Integer> namesFlux = Flux.fromIterable(names)
                .map(String::length);

        StepVerifier.create(namesFlux)
                .expectNext(4, 4, 4, 5)
                .verifyComplete();
    }

    @Test
    public void transformUsingMap_Length_repeat() {
        Flux<Integer> namesFlux = Flux.fromIterable(names)
                .map(String::length)
                .repeat(1);

        StepVerifier.create(namesFlux)
                .expectNext(4, 4, 4, 5, 4, 4, 4, 5)
                .verifyComplete();
    }

    @Test
    public void transformUsingMap_Filter() {
        Flux<String> namesFlux = Flux.fromIterable(names)
                .filter(el -> el.length() > 4)
                .map(String::toUpperCase);

        StepVerifier.create(namesFlux)
                .expectNext("JENNY")
                .verifyComplete();
    }


    @Test
    public void transformUsingFlatMap() {
        Flux<String> namesFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F"))
                .flatMap(el -> {

                    return Flux.fromIterable(covertToList(el)); // A -> List[A, newValue], B -> List[B, newValue]
                }); //db or external service call that returns a flux -> el -> Flux<String>

        StepVerifier.create(namesFlux)
                .expectNextCount(12)
                .verifyComplete();
    }


    @Test
    public void transformUsingFlatMap_usingParallel() {
        Flux<String> namesFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F"))
                .window(2) // Flux<Flux<String>> -> (A,B), (C, D), (E,F)
                .flatMap((el) -> el.map(this::covertToList).subscribeOn(parallel())) // Flux<Flux<String>>
                .flatMap(Flux::fromIterable);

        StepVerifier.create(namesFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    @Test
    public void transformUsingFlatMap_usingParalle_maintainOrder() {
        Flux<String> namesFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F"))
                .window(2) // Flux<Flux<String>> -> (A,B), (C, D), (E,F)
//                .concatMap((el) -> el.map(this::covertToList).subscribeOn(parallel())) // Flux<Flux<String>> //slower
                .flatMapSequential((el) -> el.map(this::covertToList).subscribeOn(parallel()))  //faster
                .flatMap(Flux::fromIterable);

        StepVerifier.create(namesFlux)
                .expectNextCount(12)
                .verifyComplete();
    }

    private List<String> covertToList(String el) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Arrays.asList(el, "newValue");
    }
}

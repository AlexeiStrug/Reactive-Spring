package com.reactivespring.demo.repository;

import com.reactivespring.demo.document.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@DataMongoTest
@RunWith(SpringRunner.class)
@DirtiesContext
public class ItemReactiveRepositoryTest {

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    List<Item> items = Arrays.asList(
            Item.builder().id(null).description("Some Desc1").price(400.0).build(),
            Item.builder().id(null).description("Some Desc2").price(410.0).build(),
            Item.builder().id(null).description("Some Desc3").price(420.0).build(),
            Item.builder().id(null).description("Some Desc4").price(430.0).build(),
            Item.builder().id("ABC").description("Some Desc5").price(430.0).build());

    @Before
    public void setUp() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(items))
                .flatMap(itemReactiveRepository::save)
                .doOnNext(item -> {
                    System.out.println("Inserted new item -> + " + item.toString());
                })
                .blockLast();
    }

    @Test
    public void getAllItems() {
        StepVerifier.create(itemReactiveRepository.findAll())
                .expectSubscription()
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    public void getItemById() {
        StepVerifier.create(itemReactiveRepository.findById("ABC"))
                .expectSubscription()
                .expectNextMatches(item -> item.getDescription().equals("Some Desc5"))
                .verifyComplete();
    }

    @Test
    public void getItemByDescription() {
        StepVerifier.create(itemReactiveRepository.findByDescription("Some Desc5"))
                .expectSubscription()
                .expectNextMatches(item -> item.getId().equals("ABC"))
                .verifyComplete();
    }

    @Test
    public void saveItem() {
        Item item = Item.builder().id("DEF").description("New Desc").price(40.0).build();
        Mono<Item> itemMono = itemReactiveRepository.save(item);

        StepVerifier.create(itemMono)
                .expectSubscription()
                .expectNextMatches(item1 -> (item1.getId() != null && item1.getDescription().equals("New Desc")))
                .verifyComplete();
    }

    @Test
    public void updateItem() {
        double newPrice = 520.0;
        Flux<Item> updatedItem = itemReactiveRepository.findByDescription("Some Desc1")
                .map(item -> {
                    item.setPrice(newPrice);
                    return item;
                })
                .flatMap(item -> {
                    return itemReactiveRepository.save(item);
                });

        StepVerifier.create(updatedItem)
                .expectSubscription()
                .expectNextMatches(item -> item.getPrice().equals(newPrice))
                .verifyComplete();
    }

    @Test
    public void deleteItem() {

        Mono<Void> deletedItem = itemReactiveRepository.findById("ABC")
                .map(Item::getId)
                .flatMap(id -> itemReactiveRepository.deleteById(id));

        StepVerifier.create(deletedItem)
                .expectSubscription()
                .verifyComplete();

        StepVerifier.create(itemReactiveRepository.findAll())
                .expectSubscription()
                .expectNextCount(4)
                .verifyComplete();
    }

}

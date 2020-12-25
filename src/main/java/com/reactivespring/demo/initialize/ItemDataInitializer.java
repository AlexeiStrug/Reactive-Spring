package com.reactivespring.demo.initialize;

import com.reactivespring.demo.document.Item;
import com.reactivespring.demo.repository.ItemReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
public class ItemDataInitializer implements CommandLineRunner {

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    @Override
    public void run(String... args) throws Exception {
        initialDataSetUp();
    }

    public List<Item> data() {
        return Arrays.asList(Item.builder().id(null).description("Some desc1").price(10.0).build(),
                Item.builder().id(null).description("Some desc2").price(20.0).build(),
                Item.builder().id(null).description("Some desc3").price(30.0).build(),
                Item.builder().id("ABC").description("Some desc4").price(40.0).build());
    }

    private void initialDataSetUp() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(data()))
                .flatMap(itemReactiveRepository::save)
                .thenMany(itemReactiveRepository.findAll())
                .subscribe(item -> System.out.println("Item inserted -> " + item));

    }
}

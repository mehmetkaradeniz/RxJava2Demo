package com.lbs.demo.rx.basic.rest;

import com.lbs.demo.rx.basic.Utils;
import com.lbs.demo.rx.basic.domain.SomeThing;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscription;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Erman.Kaygusuzer on 13/11/2020
 */
@RestController
@RequestMapping("book")
public class BookReactiveController {

	SomeThing alfa = new SomeThing("Alfa");
	SomeThing beta = new SomeThing("Beta");
	SomeThing gamma = new SomeThing("Gamma");
	SomeThing delta = new SomeThing("Delta");
	List<SomeThing> someThings = Arrays.asList(alfa, beta, gamma, delta);

	@RequestMapping(value = "/listStream", method = RequestMethod.GET, produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public Flowable<SomeThing> getBookList() {
		Flowable<SomeThing> someThingFlowable = Flowable.create(new FlowableOnSubscribe<SomeThing>() {
			@Override
			public void subscribe(FlowableEmitter<SomeThing> flowableEmitter) throws Exception {
				try{
					someThings.forEach(thing -> {
						Utils.sleep(2000);
						System.out.println("Produced:" + thing.toString());
						flowableEmitter.onNext(thing);
					});
					flowableEmitter.onComplete();
				} catch(Exception e){
					flowableEmitter.onError(e);
				}
			}
		}, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io());

		return someThingFlowable;
	}

	@RequestMapping(value = "/listStreamFlux", method = RequestMethod.GET, produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public Flux<SomeThing> getBookListFlux() {
		Flowable<SomeThing> someThingFlowable = Flowable.create(new FlowableOnSubscribe<SomeThing>() {
			@Override
			public void subscribe(FlowableEmitter<SomeThing> flowableEmitter) throws Exception {
				try{
					someThings.forEach(thing -> {
						Utils.sleep(2000);
						System.out.println("Produced:" + thing.toString());
						flowableEmitter.onNext(thing);
					});
					flowableEmitter.onComplete();
				} catch(Exception e){
					flowableEmitter.onError(e);
				}
			}
		}, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io());

		return Flux.from(someThingFlowable);
	}

	@RequestMapping(value = "/readInternal", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public List<SomeThing> readInternal() {
		List<SomeThing> someThingList = new ArrayList<>();

		Flowable<SomeThing> bookListFlowable = getBookList();
		bookListFlowable
				.observeOn(Schedulers.computation())
				.subscribe(new FlowableSubscriber<SomeThing>() {
					@Override
					public void onSubscribe(Subscription subscription) {
						subscription.request(Long.MAX_VALUE);
					}

					@Override
					public void onNext(SomeThing someThing) {
						System.out.println("Consumed:" + someThing.toString());
						someThingList.add(someThing);
					}

					@Override
					public void onError(Throwable throwable) {
						System.out.println("Consumed:" + throwable.getMessage());
					}

					@Override
					public void onComplete() {
						System.out.println("Consumed:" + "Completed");
					}
				});

		Utils.sleep(10000);
		return someThingList;
	}

	final WebClient client = WebClient.builder().baseUrl("http://localhost:8080").build();

	@RequestMapping(value = "/readReactive", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public List<SomeThing> readReactive() {
		List<SomeThing> someThingList = new ArrayList<>();

		Flux<SomeThing> someThingFlux = client.get().uri("/book/listStreamFlux").retrieve().bodyToFlux(SomeThing.class);
		someThingFlux.publishOn(reactor.core.scheduler.Schedulers.immediate());
		someThingFlux.subscribe(someThing -> {
			System.out.println("Consumed:" + someThing.toString());
			someThingList.add(someThing);
		}, new Consumer<Throwable>() {
			@Override
			public void accept(Throwable throwable) {
				System.out.println("Exception:" + throwable.getMessage());
			}
		}, new Runnable() {
			@Override
			public void run() {
				System.out.println("Consumed:" + "Completed");
			}
		});

		Utils.sleep(10000);

		return someThingList;
	}
}

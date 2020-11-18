package com.lbs.demo.rx.tests;

import com.lbs.demo.rx.tests.utils.Utils;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DefaultSubscriber;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by Erman.Kaygusuzer on 13/11/2020
 */
public class FlowableTests {

	@Test
	public void flowable() {
		Flowable<Integer> integerFlowable = Flowable.just(1, 2, 3, 4);

		integerFlowable.subscribe(i -> System.out.println(i));
	}

	@Test
	public void flowableFromObservable() {
		Observable<Integer> integerObservable = Observable.just(1, 2, 3, 4);

		Flowable<Integer> integerFlowable = integerObservable.toFlowable(BackpressureStrategy.BUFFER);

		integerFlowable.subscribe(i -> System.out.println(i));
	}

	@Test
	public void flowableOnSubscribe() {
		FlowableOnSubscribe<Integer> flowableOnSubscribe = flowable -> flowable.onNext(1);
		Flowable<Integer> integerFlowable = Flowable.create(flowableOnSubscribe, BackpressureStrategy.BUFFER);

		integerFlowable.subscribe(i -> System.out.println(i));
	}

	@Test
	public void flowableTake() {
		Flowable<Integer> integerFlowable = Flowable.just(1, 2, 3, 4);

		integerFlowable.take(2).subscribe(i -> System.out.println(i));
	}

	@Test
	public void flowableBuffer() {
		List<Integer> testList = IntStream.range(0, 100000).boxed().collect(Collectors.toList());

		Observable<Integer> observable = Observable.fromIterable(testList);

		TestSubscriber<Integer> testSubscriber = observable
				.toFlowable(BackpressureStrategy.BUFFER)
				.observeOn(Schedulers.computation())
				.test();

		testSubscriber.awaitTerminalEvent();

		List<Integer> receivedInts = testSubscriber.getEvents()
				.get(0)
				.stream()
				.mapToInt(object -> (int) object)
				.boxed()
				.collect(Collectors.toList());

		assertEquals(testList, receivedInts);
	}

	@Test
	public void flowableDrop() {
		List<Integer> testList = IntStream.range(0, 100000).boxed().collect(Collectors.toList());

		Observable<Integer> observable = Observable.fromIterable(testList);

		TestSubscriber<Integer> testSubscriber = observable
				.toFlowable(BackpressureStrategy.DROP)
				.observeOn(Schedulers.computation())
				.test();

		testSubscriber.awaitTerminalEvent();

		List<Integer> receivedInts = testSubscriber.getEvents()
				.get(0)
				.stream()
				.mapToInt(object -> (int) object)
				.boxed()
				.collect(Collectors.toList());

		Assert.assertTrue(receivedInts.size() < testList.size());
		Assert.assertTrue(!receivedInts.contains(100000));
	}

	@Test
	public void backPressure() {
		Flowable<Integer> observable = Flowable.range(1, 10);

		observable.subscribe(new DefaultSubscriber<Integer>() {
			@Override
			public void onStart() {
				request(1);
			}

			@Override
			public void onNext(Integer t) {
				System.out.println("item:" + t);
				Utils.sleep(1000);
				request(1);
			}

			@Override
			public void onError(Throwable t) {
				System.out.println("Error:" + t.getMessage());
			}

			@Override
			public void onComplete() {
				System.out.println("completed");
			}
		});

		Utils.sleep(10000);
	}

	@Test
	public void backPressure2() {
		Flowable<Object> flowableAsyncBackp = Flowable.create(emitter -> {

			// Publish 10 numbers
			for(int i = 0; i < 10; i++){
				System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
				emitter.onNext(i);
				Thread.sleep(1000);
			}
			// When all values or emitted, call complete.
			emitter.onComplete();
		}, BackpressureStrategy.BUFFER);

		flowableAsyncBackp.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread())
				.subscribe(new FlowableSubscriber<Object>() {
					Subscription internalSubscription;

					@Override
					public void onSubscribe(Subscription subscription) {
						this.internalSubscription = subscription;
						subscription.request(1);
					}

					@Override
					public void onNext(Object o) {
						// Process received value.
						System.out.println(Thread.currentThread().getName() + " | Received = " + o);
						// 500 mills delay to simulate slow subscriber
						Utils.sleep(1000);
						internalSubscription.request(1);
					}

					@Override
					public void onError(Throwable throwable) {
						// Process error
						System.err.println(Thread.currentThread().getName() + " | Error = " + throwable.getClass().getSimpleName() + " "
								+ throwable.getMessage());
					}

					@Override
					public void onComplete() {
						System.out.println(Thread.currentThread().getName() + " | Completed");
					}
				});
		/*
		 * Notice above -
		 *
		 * subscribeOn & observeOn - Put subscriber & publishers on different threads.
		 */

		// Since publisher & subscriber run on different thread than main thread, keep
		// main thread active for 100 seconds.
		Utils.sleep(15000);
	}

	@Test
	public void flowableOnSubscribeDetailed() {
		System.out.println("start");

		Flowable<Object> flowableAsyncBackp = Flowable.create(new FlowableOnSubscribe<Object>() {
			@Override
			public void subscribe(FlowableEmitter<Object> flowableEmitter) throws Exception {
				// Publish 10 numbers
				for(int i = 0; i < 10; i++){
					System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
					flowableEmitter.onNext(i);
					Thread.sleep(1000);
				}
				// When all values or emitted, call complete.
				flowableEmitter.onComplete();
			}
		}, BackpressureStrategy.BUFFER);
		System.out.println("subscribe");

		flowableAsyncBackp.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread())
				.subscribe(new FlowableSubscriber<Object>() {
					Subscription internalSubscription;

					@Override
					public void onSubscribe(Subscription subscription) {
						this.internalSubscription = subscription;
						subscription.request(1);
					}

					@Override
					public void onNext(Object o) {
						// Process received value.
						System.out.println(Thread.currentThread().getName() + " | Received = " + o);
						Utils.sleep(1000);
						internalSubscription.request(2);
					}

					@Override
					public void onError(Throwable throwable) {
						// Process error
						System.err.println(Thread.currentThread().getName() + " | Error = " + throwable.getClass().getSimpleName() + " "
								+ throwable.getMessage());
					}

					@Override
					public void onComplete() {
						System.out.println(Thread.currentThread().getName() + " | Completed");
					}
				});

		Utils.sleep(15000);
	}
}

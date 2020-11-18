package com.lbs.demo.rx.tests;

import com.lbs.demo.rx.tests.basicdomain.SomeThing;
import com.lbs.demo.rx.tests.utils.Utils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Erman.Kaygusuzer on 13/11/2020
 */
public class ObservableTests {

	@Test
	public void observable() {
		Observable<String> myStrings = Observable.just("Alfa", "Beta", "Gamma", "Delta");

		myStrings.subscribe(s -> System.out.println(s));
	}

	@Test
	public void multipleSubscriber() {
		Observable<String> myStrings = Observable.just("Alfa", "Beta", "Gamma", "Delta");

		myStrings.observeOn(Schedulers.computation()).subscribe(s -> {
			System.out.println("1:" + s);
			Utils.sleep(1000);
		});

		myStrings.observeOn(Schedulers.newThread()).subscribe(s -> System.out.println("2:" + s));

		Utils.sleep(10000);
	}

	@Test
	public void multipleSubscriberWithObject() {
		Observable<SomeThing> myStrings = Observable.fromArray(new SomeThing("Alfa"), new SomeThing("Beta"), new SomeThing("Gamma"), new SomeThing("Delta"));

		myStrings.subscribe(s -> System.out.println("1:" + s.toString()));
		myStrings.subscribe(s -> System.out.println("2:" + s.toString()));
	}

	@Test
	public void observerCreate() {
		SomeThing alfa = new SomeThing("Alfa");
		SomeThing beta = new SomeThing("Beta");
		SomeThing gamma = new SomeThing("Gamma");
		SomeThing delta = new SomeThing("Delta");
		List<SomeThing> someThings = Arrays.asList(alfa, beta, gamma, delta, null);

		Observable<SomeThing> someThingObservable = Observable.create(new ObservableOnSubscribe<SomeThing>() {
			@Override
			public void subscribe(ObservableEmitter<SomeThing> observableEmitter) throws Exception {
				try{
					someThings.forEach(thing -> {
						if(thing == null){
							throw new NullPointerException("null thing");
						}
						observableEmitter.onNext(thing);
					});
					observableEmitter.onComplete();
				} catch(Exception e){
					observableEmitter.onError(e);
				}
			}
		});

		someThingObservable.subscribe(new DisposableObserver<SomeThing>() {
			@Override
			public void onNext(SomeThing someThing) {
				System.out.println(someThing.toString());
			}

			@Override
			public void onError(Throwable throwable) {
				System.out.println("Exception:" + throwable.toString());
			}

			@Override
			public void onComplete() {
				System.out.println("done");
			}
		});
	}

	@Test
	public void producer() {
		SomeThing alfa = new SomeThing("Alfa");
		SomeThing beta = new SomeThing("Beta");
		SomeThing gamma = new SomeThing("Gamma");
		SomeThing delta = new SomeThing("Delta");
		List<SomeThing> someThings = Arrays.asList(alfa, beta, gamma, delta);

		Observable<SomeThing> myStrings = Observable.fromIterable(someThings);

		myStrings.subscribe(s -> System.out.println("1:" + s.toString()));
		myStrings.subscribe(s -> System.out.println("2:" + s.toString()));
	}

	@Test
	public void map() {
		Observable<String> myStrings = Observable.just("Alfa", "Beta", "Gamma", "Delta");

		myStrings.map(s -> s.length()).subscribe(l -> System.out.println(l));
	}

	/**
	 * intervals uses seperate thread default
	 */

	@Test
	public void intervals() {
		Observable<Long> secondIntervals = Observable.interval(2, TimeUnit.SECONDS);

		secondIntervals.subscribe(s -> System.out.println(s));

        /* Hold main thread for 5 seconds
        so Observable above has chance to fire */
		Utils.sleep(10000);
	}

	@Test
	public void range() {
		Observable.range(1, 5).subscribe(System.out::println);
	}


	/* operators */

	List<String> words = Arrays.asList(
			"the",
			"quick",
			"brown",
			"fox",
			"jumped",
			"over",
			"the",
			"lazy",
			"dog"
	);

	/**
	 * zip combines the elements of the source stream with the elements of a supplied stream
	 */
	@Test
	public void zipCase() {
		Observable.fromIterable(words)
				.zipWith(Observable.range(1, Integer.MAX_VALUE), (string, count) -> String.format("%2d. %s", count, string))
				.subscribe(System.out::println);
	}

	/**
	 * flatMap, which takes the streams from an Observable, and maps those elements to individual Observables
	 */
	@Test
	public void flatMapZipCase() {
		Observable.fromIterable(words)
				.flatMap(word -> Observable.fromArray(word.split("")))
				.zipWith(Observable.range(1, Integer.MAX_VALUE), (string, count) -> String.format("%2d. %s", count, string))
				.subscribe(System.out::println);
	}

	/**
	 * distinct letters from observables
	 */
	@Test
	public void flatMapZipDistinctCase() {
		Observable.fromIterable(words)
				.flatMap(word -> Observable.fromArray(word.split("")))
				.distinct()
				.zipWith(Observable.range(1, Integer.MAX_VALUE), (string, count) -> String.format("%2d. %s", count, string))
				.subscribe(System.out::println);
	}

	List<String> fixedWords = Arrays.asList(
			"the",
			"quick",
			"brown",
			"fox",
			"jumped",
			"over",
			"the",
			"lazy",
			"dogs"
	);

	/**
	 * distinct sorted letters from observables
	 */
	@Test
	public void flatMapZipDistinctSortedCase() {
		Observable.fromIterable(fixedWords)
				.flatMap(word -> Observable.fromArray(word.split("")))
				.distinct()
				.sorted()
				.zipWith(Observable.range(1, Integer.MAX_VALUE), (string, count) -> String.format("%2d. %s", count, string))
				.subscribe(System.out::println);
	}

	@Test
	public void tickTock() {
		Observable<Long> fast = Observable.interval(1, TimeUnit.SECONDS);
		Observable<Long> slow = Observable.interval(3, TimeUnit.SECONDS);

		Observable<Long> clock = Observable.merge(
				slow.filter(tick -> Utils.isSlowTime()),
				fast.filter(tick -> Utils.isSlowTime())
		);

		clock.subscribe(tick -> System.out.println(new Date()));

		Utils.sleep(30000);
	}
}

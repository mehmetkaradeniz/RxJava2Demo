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
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by Erman.Kaygusuzer on 13/11/2020
 */
public class SchedularTests {

	@Test
	public void observerWithSleep() {
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
						Utils.sleep(2000);
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
	public void observerThread() throws InterruptedException {
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
						Utils.sleep(2000);
						if(thing == null){
							throw new NullPointerException("null thing");
						}
						System.out.println("Produced:" + thing.toString());
						observableEmitter.onNext(thing);
					});
					observableEmitter.onComplete();
				} catch(Exception e){
					observableEmitter.onError(e);
				}
			}
		}).subscribeOn(Schedulers.computation());

		someThingObservable
				.observeOn(Schedulers.from(Executors.newFixedThreadPool(5)))
				.subscribe(new DisposableObserver<SomeThing>() {
					@Override
					public void onNext(SomeThing someThing) {
						//						Utils.sleep(3000);
						System.out.println("Consumed:" + someThing.toString());
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

		Thread.sleep(20000);
	}
}

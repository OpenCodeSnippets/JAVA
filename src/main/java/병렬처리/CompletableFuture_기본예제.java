package 병렬처리;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFuture_기본예제 {

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		ExecutorService e = Executors.newCachedThreadPool();

		long startTime = System.currentTimeMillis();

		CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(5000);
				} catch (Exception ex) {
					System.out.println(ex);
				}
			System.out.println("cf1 supplyAsync on thread " + Thread.currentThread().getId() + " now=" + (System.currentTimeMillis() - startTime));
			return 100;
		});

		CompletableFuture cf2 = CompletableFuture.supplyAsync(() -> {

			try {
				Thread.sleep(1000);
			} catch (Exception ex) {
				System.out.println(ex);
			}

			System.out.println("cf2 supplyAsync on thread " + Thread.currentThread().getId() + " now=" + (System.currentTimeMillis() - startTime));
			return 200;
		});

		CompletableFuture cf3 = CompletableFuture.supplyAsync(() -> {

			try {
				Thread.sleep(3000);
			} catch (Exception ex) {
				System.out.println(ex);
			}

			System.out.println("cf3 supplyAsync on thread " + Thread.currentThread().getId() + " now=" + (System.currentTimeMillis() - startTime));
			return 300;
		}, e);

		System.out.println("Task execution requested on thread " + Thread.currentThread().getId());

		cf3.thenComposeAsync((data1) -> cf2).thenComposeAsync((data2) -> cf1).join();


		System.out.println("final cf1.get() = " + cf1.get() + " cf2.get()=" + cf2.get() + " cf3.get()=" + cf3.get() + " now=" + (System.currentTimeMillis() - startTime));

	}
}

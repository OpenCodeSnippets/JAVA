package 병렬처리;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

public class CompletableFuture_순차_작업_then {
	// 병행 처리되는지 확인하기 위해 sleep 시간 설정
	static final int SLEEPSECOND = 1;

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		final AtomicInteger atom = new AtomicInteger();
		List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
		long startTime = System.currentTimeMillis();

		//https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html
		// 한번 스레드를 생성하면 캐쉬해두고 재활용한다. max thread 갯수 초과시에만 새로운 스레드 생성
		ExecutorService  executorService = Executors.newCachedThreadPool();
		//lambda만으로 처리하지 않고 join을 사용하여 병행처리 - join은 소스 참조
		테스트CompletableFuture_join을사용하여병행처리(atom, list, executorService);

		list = Arrays.asList(1,2,3,4,5,6,7,8,9,10, 11, 12);
		테스트CompletableFuture_join을사용하여병행처리(atom, list, executorService);

		list = Arrays.asList(1,2,3,4,5);
		//lambda만 사용하여 병행처리 - 람다만 쓸 때 pararrelStream을 사용하지 않으면 시퀀셜하게 동작하는 문제가 있음
		테스트CompletableFuture_join을쓰지않고병행처리_parallelStream을안쓰면_sequential하게동작한다(atom, list, executorService);

		long endTime = System.currentTimeMillis() - startTime;
		System.out.println(String.format("==>  총 걸린 시간: %d", endTime));

	}

	private static void 테스트CompletableFuture_join을사용하여병행처리(AtomicInteger atomicInteger, List<Integer> list, ExecutorService executorService) {
		StringBuffer sb = new StringBuffer();
		CompletableFuture<Integer>[] completableFutures = list.stream()
			.map((x) -> {
				return CompletableFuture.supplyAsync(() -> {
					try {
						atomicInteger.incrementAndGet();
//						sb.append(String.format("[ x: %d, AtomInt: %d] ", x, atomicInteger.intValue()));
//						System.out.print(String.format("[x: %d, AtomInt: %d] ", x, atomicInteger.intValue()));
						TimeUnit.SECONDS.sleep(SLEEPSECOND);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return x + 10;
				}, executorService).thenApply(y -> {
					try {
						TimeUnit.SECONDS.sleep(SLEEPSECOND);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return y + 100;
				});
			}).toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(completableFutures).join();

		Optional<Integer> resultOptional = Arrays.stream(completableFutures)
			.map(x -> {
				try {
					return x.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			})
			.reduce((x, y) -> x + y);
		System.out.println("");
		//Collection의 stream()이 순서대로 되지않는 것 같아 확인용으로 StringBuffer에 담음.
//		System.out.println("[INFO] StringBuffer =>" + sb.toString());
		System.out.println(String.format("[INFO] ExecutorService info: [%s] ", executorService.toString()));
		System.out.println(String.format("==> 비동기로 계산된 결과 값: %d", resultOptional.orElse(0)));
	}

	private static void 테스트CompletableFuture_join을쓰지않고병행처리_parallelStream을안쓰면_sequential하게동작한다(AtomicInteger atomicInteger, List<Integer> list, ExecutorService executorService) {
		StringBuffer sb = new StringBuffer();
		Optional<Integer> resultOptional = list.parallelStream()
			.map((x) -> {
				return CompletableFuture.supplyAsync(() -> {
					try {
						atomicInteger.incrementAndGet();
//						sb.append(String.format("[ x: %d, AtomInt: %d] ", x, atomicInteger.intValue()));
//						System.out.print(String.format("[x: %d, AtomInt: %d] ", x, atomicInteger.intValue()));
						TimeUnit.SECONDS.sleep(SLEEPSECOND);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return x + 10;
				}, executorService).thenApply(y -> {
					try {
						TimeUnit.SECONDS.sleep(SLEEPSECOND);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return y + 100;
				});
			}).map(x -> {
				try {
					return x.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}).reduce((x, y) -> x + y);

		System.out.println("");
//		System.out.println("[INFO] StringBuffer =>" + sb.toString());
		System.out.println(String.format("[INFO] ExecutorService info: [%s] ", executorService.toString()));
		System.out.println(String.format("==> 비동기로 계산된 결과 값: %d", resultOptional.orElse(0)));
	}
}

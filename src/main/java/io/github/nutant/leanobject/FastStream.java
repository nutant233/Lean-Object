package io.github.nutant.leanobject;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public final class FastStream<T> implements Stream<T> {

    @SafeVarargs
    public static <T> FastStream<T> create(T... array) {
        return new FastStream<>(array);
    }

    public final T[] array;
    private boolean consumed = false;

    public FastStream(T[] array) {
        this.array = array;
    }

    private void checkNotConsumed() {
        if (consumed) throw new IllegalStateException("stream has already been operated upon or closed");
        consumed = true;
    }

    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        checkNotConsumed();
        return Arrays.stream(array).filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        checkNotConsumed();
        return Arrays.stream(array).map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        checkNotConsumed();
        return Arrays.stream(array).mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        checkNotConsumed();
        return Arrays.stream(array).mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        checkNotConsumed();
        return Arrays.stream(array).mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        checkNotConsumed();
        return Arrays.stream(array).flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        checkNotConsumed();
        return Arrays.stream(array).flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        checkNotConsumed();
        return Arrays.stream(array).flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        checkNotConsumed();
        return Arrays.stream(array).flatMapToDouble(mapper);
    }

    @Override
    public Stream<T> distinct() {
        checkNotConsumed();
        return Arrays.stream(array).distinct();
    }

    @Override
    public Stream<T> sorted() {
        checkNotConsumed();
        return Arrays.stream(array).sorted();
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
        checkNotConsumed();
        return Arrays.stream(array).sorted(comparator);
    }

    @Override
    public Stream<T> peek(Consumer<? super T> action) {
        checkNotConsumed();
        return Arrays.stream(array).peek(action);
    }

    @Override
    public Stream<T> limit(long maxSize) {
        checkNotConsumed();
        return Arrays.stream(array).limit(maxSize);
    }

    @Override
    public Stream<T> skip(long n) {
        checkNotConsumed();
        return Arrays.stream(array).skip(n);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        checkNotConsumed();
        for (T o : array) {
            action.accept(o);
        }
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        checkNotConsumed();
        for (T o : array) {
            action.accept(o);
        }
    }

    @Override
    public @NotNull T @NotNull [] toArray() {
        checkNotConsumed();
        return array.clone();
    }

    @Override
    public @NotNull <A> A @NotNull [] toArray(IntFunction<A[]> generator) {
        checkNotConsumed();
        var array = this.array;
        var result = generator.apply(array.length);
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        checkNotConsumed();
        return Arrays.stream(array).reduce(identity, accumulator);
    }

    @Override
    public @NotNull Optional<T> reduce(BinaryOperator<T> accumulator) {
        checkNotConsumed();
        return Arrays.stream(array).reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        checkNotConsumed();
        return Arrays.stream(array).reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        checkNotConsumed();
        return Arrays.stream(array).collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        checkNotConsumed();
        return Arrays.stream(array).collect(collector);
    }

    @Override
    public @NotNull Optional<T> min(Comparator<? super T> comparator) {
        checkNotConsumed();
        return Arrays.stream(array).min(comparator);
    }

    @Override
    public @NotNull Optional<T> max(Comparator<? super T> comparator) {
        checkNotConsumed();
        return Arrays.stream(array).max(comparator);
    }

    @Override
    public long count() {
        checkNotConsumed();
        return array.length;
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        checkNotConsumed();
        for (T o : array) {
            if (predicate.test(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        checkNotConsumed();
        for (T o : array) {
            if (!predicate.test(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        checkNotConsumed();
        for (T o : array) {
            if (predicate.test(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull Optional<T> findFirst() {
        checkNotConsumed();
        return array.length == 0 ? Optional.empty() : Optional.of(array[0]);
    }

    @Override
    public @NotNull Optional<T> findAny() {
        checkNotConsumed();
        return array.length == 0 ? Optional.empty() : Optional.of(array[0]);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        checkNotConsumed();
        return new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < array.length;
            }

            @Override
            public T next() {
                if (i >= array.length) {
                    throw new NoSuchElementException();
                }
                return array[i++];
            }
        };
    }

    @Override
    public @NotNull Spliterator<T> spliterator() {
        checkNotConsumed();
        return Arrays.spliterator(array);
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public @NotNull Stream<T> sequential() {
        checkNotConsumed();
        return Arrays.stream(array);
    }

    @Override
    public @NotNull Stream<T> parallel() {
        checkNotConsumed();
        return Arrays.stream(array).parallel();
    }

    @Override
    public @NotNull Stream<T> unordered() {
        checkNotConsumed();
        return Arrays.stream(array).unordered();
    }

    @Override
    public @NotNull Stream<T> onClose(@NotNull Runnable closeHandler) {
        checkNotConsumed();
        return Arrays.stream(array).onClose(closeHandler);
    }

    @Override
    public void close() {
        consumed = true;
    }
}
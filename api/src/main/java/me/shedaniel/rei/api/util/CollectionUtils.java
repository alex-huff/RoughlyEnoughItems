/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.api.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtils {
    public static <A, B> List<B> getOrPutEmptyList(Map<A, List<B>> map, A key) {
        List<B> b = map.get(key);
        if (b != null)
            return b;
        map.put(key, new ArrayList<>());
        return map.get(key);
    }
    
    public static <T> T findFirstOrNullEquals(Iterable<T> list, T obj) {
        for (T t : list) {
            if (t.equals(obj))
                return t;
        }
        return null;
    }
    
    public static <T, R> List<R> castAndMap(Iterable<T> list, Class<R> castClass) {
        List<R> l = new ArrayList<>();
        for (T t : list) {
            if (castClass.isAssignableFrom(t.getClass()))
                l.add((R) t);
        }
        return l;
    }
    
    public static <T> T findFirstOrNull(Iterable<T> list, Predicate<T> predicate) {
        for (T t : list) {
            if (predicate.test(t))
                return t;
        }
        return null;
    }
    
    public static <T> boolean anyMatch(Iterable<T> list, Predicate<T> predicate) {
        for (T t : list) {
            if (predicate.test(t))
                return true;
        }
        return false;
    }
    
    @Environment(EnvType.CLIENT)
    public static EntryStack<?> findFirstOrNullEqualsExact(Iterable<? extends EntryStack<?>> list, EntryStack<?> stack) {
        for (EntryStack<?> t : list) {
            if (EntryStacks.equalsExact(t, stack))
                return t;
        }
        return null;
    }
    
    public static <T> List<T> filterToList(Iterable<T> list, Predicate<T> predicate) {
        List<T> l = Lists.newArrayList();
        for (T t : list) {
            if (predicate.test(t)) {
                l.add(t);
            }
        }
        return l;
    }
    
    public static <T> Set<T> filterToSet(Iterable<T> list, Predicate<T> predicate) {
        Set<T> l = Sets.newLinkedHashSet();
        for (T t : list) {
            if (predicate.test(t)) {
                l.add(t);
            }
        }
        return l;
    }
    
    public static <T, R> List<R> map(Collection<T> list, Function<T, R> function) {
        List<R> l = new ArrayList<>(list.size() + 1);
        for (T t : list) {
            l.add(function.apply(t));
        }
        return l;
    }
    
    public static <T, R> List<R> map(Iterable<T> list, Function<T, R> function) {
        List<R> l = new ArrayList<>();
        for (T t : list) {
            l.add(function.apply(t));
        }
        return l;
    }
    
    public static <T> IntList mapToInt(Collection<T> list, ToIntFunction<T> function) {
        IntList l = new IntArrayList(list.size() + 1);
        for (T t : list) {
            l.add(function.applyAsInt(t));
        }
        return l;
    }
    
    public static <T, R> List<R> mapParallel(Collection<T> list, Function<T, R> function) {
        return list.parallelStream().map(function).collect(Collectors.toList());
    }
    
    public static <T, R, C extends Collection<R>> C mapParallel(Collection<T> list, Function<T, R> function, Supplier<C> supplier) {
        return list.parallelStream().map(function).collect(Collectors.toCollection(supplier));
    }
    
    public static <T, R> List<R> map(T[] list, Function<T, R> function) {
        List<R> l = new ArrayList<>(list.length + 1);
        for (T t : list) {
            l.add(function.apply(t));
        }
        return l;
    }
    
    public static <T, R> Optional<R> mapAndMax(Collection<T> list, Function<T, R> function, Comparator<R> comparator) {
        if (list.isEmpty())
            return Optional.empty();
        return list.stream().max(Comparator.comparing(function, comparator)).map(function);
    }
    
    public static <T, R> Optional<R> mapAndMax(T[] list, Function<T, R> function, Comparator<R> comparator) {
        if (list.length <= 0)
            return Optional.empty();
        return Stream.of(list).max(Comparator.comparing(function, comparator)).map(function);
    }
    
    public static <T> Optional<T> max(Collection<T> list, Comparator<T> comparator) {
        if (list.isEmpty())
            return Optional.empty();
        return list.stream().max(comparator);
    }
    
    public static <T> Optional<T> max(T[] list, Comparator<T> comparator) {
        if (list.length <= 0)
            return Optional.empty();
        return Stream.of(list).max(comparator);
    }
    
    public static String joinToString(Iterable<String> list, String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (String t : list) {
            joiner.add(t);
        }
        return joiner.toString();
    }
    
    public static String joinToString(String[] list, String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (String t : list) {
            joiner.add(t);
        }
        return joiner.toString();
    }
    
    public static <T> String mapAndJoinToString(Iterable<T> list, Function<T, String> function, String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (T t : list) {
            joiner.add(function.apply(t));
        }
        return joiner.toString();
    }
    
    public static <T> String mapAndJoinToString(T[] list, Function<T, String> function, String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (T t : list) {
            joiner.add(function.apply(t));
        }
        return joiner.toString();
    }
    
    public static <T> Component mapAndJoinToComponent(Iterable<T> list, Function<T, Component> function, Component separator) {
        TextComponent joiner = new TextComponent("");
        boolean first = true;
        for (T t : list) {
            if (first) {
                first = false;
            } else {
                joiner.append(separator.copy());
            }
            joiner.append(function.apply(t));
        }
        return joiner;
    }
    
    public static <T> Component mapAndJoinToComponent(T[] list, Function<T, Component> function, Component separator) {
        TextComponent joiner = new TextComponent("");
        boolean first = true;
        for (T t : list) {
            if (first) {
                first = false;
            } else {
                joiner.append(separator.copy());
            }
            joiner.append(function.apply(t));
        }
        return joiner;
    }
    
    public static <T, R> List<R> filterAndMap(Iterable<T> list, Predicate<T> predicate, Function<T, R> function) {
        List<R> l = null;
        for (T t : list) {
            if (predicate.test(t)) {
                if (l == null)
                    l = Lists.newArrayList();
                l.add(function.apply(t));
            }
        }
        return l == null ? Collections.emptyList() : l;
    }
    
    public static <T> int sumInt(Iterable<T> list, Function<T, Integer> function) {
        int sum = 0;
        for (T t : list) {
            sum += function.apply(t);
        }
        return sum;
    }
    
    public static <T> int sumInt(Iterable<Integer> list) {
        int sum = 0;
        for (int t : list) {
            sum += t;
        }
        return sum;
    }
    
    public static <T> double sumDouble(Iterable<T> list, Function<T, Double> function) {
        double sum = 0;
        for (T t : list) {
            sum += function.apply(t);
        }
        return sum;
    }
    
    public static <T> double sumDouble(Iterable<Double> list) {
        double sum = 0;
        for (double t : list) {
            sum += t;
        }
        return sum;
    }
    
    public static <T> Iterable<Iterable<T>> partition(List<T> list, int size) {
        return () -> new UnmodifiableIterator<Iterable<T>>() {
            int i = 0;
            int partitionSize = Mth.ceil(list.size() / (float) size);
            
            @Override
            public boolean hasNext() {
                return i < partitionSize;
            }
            
            @Override
            public Iterable<T> next() {
                UnmodifiableIterator<T> iterator = new UnmodifiableIterator<T>() {
                    int cursor = i++ * size;
                    int curSize = cursor + Math.min(list.size() - cursor, size);
                    
                    @Override
                    public boolean hasNext() {
                        return cursor < curSize;
                    }
                    
                    @Override
                    public T next() {
                        return list.get(cursor++);
                    }
                };
                return () -> iterator;
            }
        };
    }
}

package magicmod.common.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import org.joml.Vector3i;

import cpw.mods.fml.relauncher.ReflectionHelper;

@SuppressWarnings("unused")
public class DataUtils {

    public static int clamp(int val, int lo, int hi) {
        return Math.max(lo, Math.min(val, hi));
    }

    public static long clamp(long val, long lo, long hi) {
        return Math.max(lo, Math.min(val, hi));
    }

    public static float clamp(float val, float lo, float hi) {
        return Math.max(lo, Math.min(val, hi));
    }

    public static double clamp(double val, double lo, double hi) {
        return Math.max(lo, Math.min(val, hi));
    }

    public static int min(int first, int... rest) {
        for (int l : rest) {
            if (l < first) first = l;
        }
        return first;
    }

    public static long min(long first, long... rest) {
        for (long l : rest) {
            if (l < first) first = l;
        }
        return first;
    }

    public static int max(int first, int... rest) {
        for (int l : rest) {
            if (l > first) first = l;
        }
        return first;
    }

    public static long max(long first, long... rest) {
        for (long l : rest) {
            if (l > first) first = l;
        }
        return first;
    }

    public static int ceilDiv(int lhs, int rhs) {
        return (lhs + rhs - 1) / rhs;
    }

    public static int ceilDiv2(int lhs, int rhs) {
        int sign = signum(lhs) * signum(rhs);

        if (lhs == 0) return 0;
        if (rhs == 0) throw new ArithmeticException("/ by zero");

        lhs = Math.abs(lhs);
        rhs = Math.abs(rhs);

        int unsigned = 1 + ((lhs - 1) / rhs);

        return unsigned * sign;
    }

    public static long ceilDiv(long lhs, long rhs) {
        return (lhs + rhs - 1) / rhs;
    }

    public static int signum(int i) {
        return Integer.compare(i, 0);
    }

    public static long signum(long i) {
        return Long.compare(i, 0);
    }

    public static Vector3i signum(Vector3i v) {
        v.x = signum(v.x);
        v.y = signum(v.y);
        v.z = signum(v.z);

        return v;
    }

    public static long d2lCeil(double d) {
        long l = (long) d;
        return d > l ? l + 1 : l;
    }

    public static int d2iCeil(double d) {
        int i = (int) d;
        return d > i ? i + 1 : i;
    }

    public static float lerp(float a, float b, float k) {
        return a * (1 - k) + b * k;
    }

    public static <S, T> List<T> mapToList(Collection<S> in, Function<S, T> mapper) {
        if (in == null) return null;

        List<T> out = new ArrayList<>(in.size());

        for (S s : in) {
            out.add(mapper.apply(s));
        }

        return out;
    }

    public static <S, T> List<T> mapToList(S[] in, Function<S, T> mapper) {
        if (in == null) return null;

        List<T> out = new ArrayList<>(in.length);

        for (S s : in) {
            out.add(mapper.apply(s));
        }

        return out;
    }

    public static <S, T> T[] mapToArray(Collection<S> in, IntFunction<T[]> ctor, Function<S, T> mapper) {
        if (in == null) return null;

        T[] out = ctor.apply(in.size());

        Iterator<S> iter = in.iterator();
        for (int i = 0; i < out.length && iter.hasNext(); i++) {
            out[i] = mapper.apply(iter.next());
        }

        return out;
    }

    public static <S, T> T[] mapToArray(S[] in, IntFunction<T[]> ctor, Function<S, T> mapper) {
        if (in == null) return null;

        T[] out = ctor.apply(in.length);

        for (int i = 0; i < out.length; i++) {
            out[i] = mapper.apply(in[i]);
        }

        return out;
    }

    public static <T> int indexOf(T[] array, T value) {
        int l = array.length;

        for (int i = 0; i < l; i++) {
            if (array[i] == value) { return i; }
        }

        return -1;
    }

    public static <T> int indexOf(T[] array, Predicate<T> filter) {
        int l = array.length;

        for (int i = 0; i < l; i++) {
            if (filter.test(array[i])) { return i; }
        }

        return -1;
    }

    public static <T> int indexOf(List<T> array, Predicate<T> filter) {
        for (int i = 0, arraySize = array.size(); i < arraySize; i++) {
            T value = array.get(i);

            if (filter.test(value)) {
                return i;
            }
        }

        return -1;
    }

    public static <T> T find(T[] array, Predicate<T> filter) {
        for (T value : array) {
            if (filter.test(value)) return value;
        }

        return null;
    }

    public static <T> T find(Collection<T> list, Predicate<T> filter) {
        for (T value : list) {
            if (filter.test(value)) return value;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T, R extends T> R findInstance(Collection<T> list, Class<R> clazz) {
        for (T value : list) {
            if (clazz.isInstance(value)) return (R) value;
        }

        return null;
    }

    public static <T> int count(Collection<T> list, Predicate<T> filter) {
        int count = 0;

        for (T value : list) {
            if (filter.test(value)) count++;
        }

        return count;
    }

    public static <T> T getIndexSafe(T[] array, int index) {
        return array == null || index < 0 || index >= array.length ? null : array[index];
    }

    public static <T> T getIndexSafe(List<T> list, int index) {
        return list == null || index < 0 || index >= list.size() ? null : list.get(index);
    }

    public static <T> T choose(List<T> list, Random rng) {
        if (list.isEmpty()) return null;
        if (list.size() == 1) return list.get(0);

        return list.get(rng.nextInt(list.size()));
    }

    public static <K, V> boolean areMapsEqual(Map<K, V> left, Map<K, V> right) {
        if (left == null || right == null) {
            return left == right;
        }

        HashSet<K> keys = new HashSet<>(left.size() + right.size());

        keys.addAll(left.keySet());
        keys.addAll(right.keySet());

        for (K key : keys) {
            if (!Objects.equals(left.get(key), right.get(key))) return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public static <A, B extends A> List<B> downcastUnchecked(List<A> list) {
        return new AbstractList<>() {

            @Override
            public B get(int index) {
                return (B) list.get(index);
            }

            @Override
            public int size() {
                return list.size();
            }

            @Override
            public B remove(int index) {
                return (B) list.remove(index);
            }
        };
    }

    public static MethodHandle exposeFieldGetter(Class<?> clazz, String... names) {
        try {
            Field field = ReflectionHelper.findField(clazz, names);
            field.setAccessible(true);
            return MethodHandles.lookup()
                .unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not make field getter for " + clazz.getName() + ":" + names[0], e);
        }
    }

    public static MethodHandle exposeFieldSetter(Class<?> clazz, String... names) {
        try {
            Field field = ReflectionHelper.findField(clazz, names);
            field.setAccessible(true);
            return MethodHandles.lookup()
                .unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not make field setter for " + clazz.getName() + ":" + names[0], e);
        }
    }

    public static <T, R> Function<T, R> exposeFieldGetterLambda(Class<? super T> clazz, String... names) {
        final MethodHandle method = exposeFieldGetter(clazz, names);

        return instance -> {
            try {
                //noinspection unchecked
                return (R) method.invokeExact(instance);
            } catch (Throwable e) {
                throw new RuntimeException("Could not get field " + clazz.getName() + ":" + names[0], e);
            }
        };
    }

    public static MethodHandle exposeMethod(Class<?> clazz, MethodType sig, String... names) {
        try {
            Method method = ReflectionHelper.findMethod(clazz, null, names, sig.parameterArray());
            method.setAccessible(true);
            return MethodHandles.lookup()
                .unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not make method handle for " + clazz.getName() + ":" + names[0], e);
        }
    }

    public static <T extends Enum<T>> long enumSetToLong(EnumSet<T> set) {
        long data = 0;

        for (T t : set) {
            data |= 1L << t.ordinal();
        }

        return data;
    }

    public static <T extends Enum<T>> void longToEnumSet(Class<T> clazz, long data, EnumSet<T> set) {
        set.clear();

        for (T t : clazz.getEnumConstants()) {
            if ((data & 1L << t.ordinal()) != 0) {
                set.add(t);
            }
        }
    }

    public static <T> boolean toggleValue(Set<T> set, T value) {
        if (!set.remove(value)) {
            set.add(value);

            return true;
        } else {
            return false;
        }
    }
}

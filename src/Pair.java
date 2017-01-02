

//package org.hh.jga.util;

import java.io.Serializable;

/**
 * Simple STL Pair Implementation
 *
 * @author Hong Hong
 *
 * @param <E1> the type of first element
 * @param <E2> the type of second element
 */
public class Pair<E1, E2> implements Serializable {
    private static final long serialVersionUID = -312111942211427434L;
    public E1 first;
    public E2 second;

    /**
     * This is a comparable pair
     *
     * @author Hong Hong
     *
     * @param <E1> the type of first element
     * @param <E2> the type of second element
     */
    public static class CpPair<E1 extends Comparable<E1>, E2 extends Comparable<E2>> extends Pair<E1, E2> implements Comparable<CpPair<E1, E2>> {
        private static final long serialVersionUID = -6710234564965065727L;

        public int compareTo(CpPair<E1, E2> o) {
            int res = first != null ? first.compareTo(o.first) : (o.first != null ? -1 : 0);
            if(res == 0)
                res = second != null ? second.compareTo(o.second) : (o.second != null ? -1 : 0);
            return res;
        }
        public CpPair(E1 first, E2 second) {
            this.first = first;
            this.second = second;
        }
        public CpPair() {
        }
        public <E extends Comparable<E>> CpPair<E, CpPair<E1, E2>> frontAppend(E e) {
            return make_pair(e, this);
        }
    }
    public static <E1, E2> Pair<E1, E2> make_pair(E1 e1, E2 e2) {
        return new Pair<E1, E2>(e1, e2);
    }
    public static <E1 extends Comparable<E1>, E2 extends Comparable<E2>> CpPair<E1, E2> make_pair(E1 e1, E2 e2) {
        return new CpPair<E1, E2>(e1, e2);
    }

    public static <E1, E2, E3> Pair<E1, Pair<E2, E3>> make_chain(E1 e1, E2 e2, E3 e3) {
        return new Pair<E1, Pair<E2, E3>>(e1, new Pair<E2, E3>(e2, e3));
    }
    public static <E1 extends Comparable<E1>, E2 extends Comparable<E2>, E3 extends Comparable<E3>>
    CpPair<E1, CpPair<E2, E3>> make_chain(E1 e1, E2 e2, E3 e3) {
        return make_pair(e1, make_pair(e2, e3));
    }

    public <E> Pair<E, Pair<E1, E2>> frontAppend(E e) {
        return make_pair(e, this);
    }


    public Pair(E1 first, E2 second) {
        this.first = first;
        this.second = second;
    }
    public Pair() {
    }

    public int hashCode() {
        return (first == null ? 0 : first.hashCode())
                ^ (second == null ? 0 : second.hashCode());
    }

    @SuppressWarnings("unchecked")
    public boolean equals(Object other) {
        if(other.getClass().equals(getClass())) {
            Pair<E1, E2> o = (Pair<E1, E2>) other;
            if(first == o.first || first != null && first.equals(o.first)) {
                if(second == o.second || second != null && second.equals(o.second))
                    return true;
            }
        }
        return false;
    }

    public String toString() {
        return "Pair(" + first.toString() + ", " + second.toString() + ")";
    }
}
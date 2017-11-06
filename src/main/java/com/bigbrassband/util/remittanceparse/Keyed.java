package com.bigbrassband.util.remittanceparse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// Class to handle finding needles in haystacks and dealing with missing needles
public abstract class Keyed implements Comparable<Keyed> {

    @SafeVarargs
    static <T extends Keyed, U extends Keyed>
    void haystackSearch(ArrayList<T> needles, ArrayList<U> haystack, NeedleHandler<T, U>... needleHandlers) {

        //get the haystack sorted
        ArrayList<U> orderedHaystack = new ArrayList<>(haystack);
        orderedHaystack.sort(Comparator.naturalOrder());

        //search for each needle in the sorted haystack
        for (T needle : needles) {
            final int pos = Collections.binarySearch(orderedHaystack, needle);
            for (NeedleHandler<T, U> needleHandler : needleHandlers) {
                if (needleHandler.test(needle))
                    needleHandler.accept(needle, pos >= 0 ? orderedHaystack.get(pos) : null);
            }
        }

        //let the needle handlers know that the search is over
        for (NeedleHandler<T, U> needleHandler : needleHandlers) {
            needleHandler.done();
        }
    }

    protected abstract String getId();

    protected abstract long getPennies();

    @Override
    public int compareTo(Keyed o) {
        int i = getId().compareTo(o.getId());
        return i != 0 ? i : Long.compare(getPennies(), o.getPennies());
    }

    public interface NeedleHandler<T extends Keyed, U extends Keyed> {
        boolean test(T t);

        void accept(T t, U u);

        void done();
    }
}

package com.google.errorprone.bugpatterns.T2R.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Created by ameya on 12/26/18.
 */
public class Util {


    public static class Pair<U,V> {

        private final U fst;
        private final V snd;

        public Pair(U fst,V snd){
            this.fst = fst;
            this.snd = snd;
        }

        public static <U,V> Pair<U,V> P(U u, V v){ return new Pair<>(u,v);}

        public U fst(){
            return this.fst;
        }
        public V snd(){
            return this.snd;
        }


    }


    static <T> List<T> reverse(final List<T> list) {
        final int size = list.size();
        final int last = size - 1;
        final List<T> result = new ArrayList<>(size);
        for (int i = last; i >= 0; --i) {
            final T element = list.get(i);
            result.add(element);
        }
        return result;
    }

    @SafeVarargs
    public static <U> List<U> L(U ... u){ return Arrays.asList(u);}

    public static <U> U nullHandleReduce(U u1, U u2, BinaryOperator<U> binOp, U empty){

        if((null == u1 && null == u2)){
            return empty;
        }
        else if(u1 == null || u1.equals(empty)){
            return u2;
        }
        else if(u2 == null || u2.equals(empty)){
            return u1;
        }
        else if((u1.equals(empty) || u2.equals(empty))){
            return empty;
        }
        else
            return binOp.apply(u1,u2);
    }

}

package com.google.errorprone.bugpatterns.T2R.Mappings;

import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.METHOD;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.emptyTFG;
import static com.google.errorprone.bugpatterns.T2R.common.Util.L;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static com.google.errorprone.bugpatterns.T2R.common.Util.nullHandleReduce;

import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.T2R.Analysis.Analysis;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.State;
import com.google.errorprone.bugpatterns.T2R.common.Tree2State2U;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Created by ameya on 12/16/18.
 */
public class GetMethods {


    @SafeVarargs
    public static TypeFactGraph<Identification> merge(TypeFactGraph<Identification>... s){
        return Arrays.stream(s).reduce(emptyTFG(), Analysis::merge);
    }
    private static final Set<MethodTree> empty = new HashSet<>();


    public static final BinaryOperator<Set<MethodTree>> combine = (s1, s2) -> {
        s1.addAll(s2);
        return s1;
    };

    private static final BinaryOperator<Set<MethodTree>> combiner = (u1,u2) ->
            nullHandleReduce(u1,u2, combine, empty);

    private static final TreeScanner<Set<MethodTree>,Pair<VisitorState, State>> GET_METHOD =
        new TreeScanner<Set<MethodTree>, Pair<VisitorState, State>>() {
            @Override
            public Set<MethodTree> visitMethod(MethodTree m, Pair<VisitorState, State> s) {
                final Identification mid = s.snd().getRootID();
                return mid.getKind().equals(METHOD) ? new HashSet<>(L(m)) : new HashSet<>();
            }
            @Override
            public Set<MethodTree> reduce(Set<MethodTree> u1, Set<MethodTree> u2) {
                return combiner.apply(u1, u2);
        }
    };

    private static final Function<Pair<VisitorState,State>,Set<MethodTree>> translator
            = x -> GET_METHOD.scan(x.snd().getRoot(),P(x.fst(),x.snd()));

    public static Tree2State2U<Set<MethodTree>> GET_METHODS (VisitorState s){
        return new Tree2State2U<>(empty,combiner,translator, s);
    }


}

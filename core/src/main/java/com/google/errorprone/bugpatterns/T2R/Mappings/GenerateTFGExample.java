package com.google.errorprone.bugpatterns.T2R.Mappings;

import static com.google.errorprone.bugpatterns.T2R.Collect.CollectMatchers.matchesTypeT;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.ID;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.ARGUMENT;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.ASSIGNMENT;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.BIN_OP;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.EXTENDS;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.IMPLEMENTS;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.INITIALIZED_AS;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.PARAM;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.RECEIVER;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.RETURNS;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.addNode;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.emptyTFG;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.uV_vU;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static com.google.errorprone.bugpatterns.T2R.common.Util.nullHandleReduce;

import com.google.common.collect.ImmutableMap;
import com.google.common.graph.MutableValueGraph;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.T2R.Analysis.Analysis;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.State;
import com.google.errorprone.bugpatterns.T2R.common.Tree2State2U;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Created by ameya on 12/16/18.
 */
public class GenerateTFGExample {


    public static final String AFFECTED_BY_HIERARCHY = "AFFECTED_BY_HIERARCHY";
    public static final String METHOD_HIERARCHY = "METHOD_HIERARCHY";
    public static final String PARAM_INDEX = "PARAMETER_INDEX:";
    public static final String ARG_INDEX = "ARGUMENT_INDEX:";
    public static final String PASSED_AS_ARG_TO = "PASSED_AS_ARG_TO";
    public static final String ARG_PASSED = "ARG_PASSED";
    public static final String ASSIGNED_TO = "ASSIGNED_TO";
    public static final String ASSIGNED_AS = "ASSIGNED_AS";
    public static final String METHOD_INVOKED = "METHOD_INVOKED";
    public static final String OF_TYPE  = "OF_TYPE";
    public static final String PARENT_METHOD = "PARENT_METHOD";
    public static final String RETURNED_BY  = "RETURNED_BY";
    public static final String OVERRIDES  = "OVERRIDES";
    public static final String OVERRIDEN_BY  = "OVERRIDEN_BY";
    public static final String OVERRIDEN_METHOD = "OVERRIDEN_METHOD";
    public static final String MODIFIER = "MODIFIER";
    public static final String NOT_PRIVATE = "NOT_PRIVATE";
    public static final String DECLARED_IN = "DECLARED_IN";
    public static final String DECLARES = "DECLARES";



    static final ImmutableMap<String, Pair<String, String>> RELATION_TO_EDGE =
            ImmutableMap.<String, Pair<String,String>>builder()
                    .put(INITIALIZED_AS, P(ASSIGNED_AS, ASSIGNED_TO))
                    .put(ASSIGNMENT, P(ASSIGNED_AS,ASSIGNED_TO))
                    .put(RETURNS, P(RETURNS,RETURNED_BY))
                    .put(ARGUMENT, P(ARG_INDEX,PASSED_AS_ARG_TO))
                    .put(RECEIVER, P(RECEIVER, METHOD_INVOKED))
                    .put(PARAM, P(PARAM_INDEX,PARENT_METHOD))
                    .put(EXTENDS,P(OF_TYPE,AFFECTED_BY_HIERARCHY))
                    .put(OVERRIDES, P(OVERRIDEN_METHOD, OVERRIDEN_BY))
                    .put(IMPLEMENTS,P(OF_TYPE,AFFECTED_BY_HIERARCHY))
                    .put(DECLARED_IN, P(DECLARED_IN, DECLARES))
                    .put(MODIFIER, P(MODIFIER, MODIFIER))
                    .put(METHOD_HIERARCHY, P(METHOD_HIERARCHY,METHOD_HIERARCHY))
                    .put(BIN_OP, P(BIN_OP,BIN_OP)).build();

    @SafeVarargs
    public static TypeFactGraph<Identification> merge(TypeFactGraph<Identification>... s){
        return Arrays.stream(s).reduce(emptyTFG(), Analysis::merge);
    }

    public static  String lastCharacter(String s){
        return s.substring(s.length() - 1);
    }

    public static Function<MutableValueGraph<Identification,String>, MutableValueGraph<Identification,String>> addEdge(Identification f, Identification t, String relation){
        Pair<String, String> edges = RELATION_TO_EDGE.get(relation);
        if(relation.contains(PARAM)) {
            edges = RELATION_TO_EDGE.get(PARAM);
            edges = P(edges.fst() + lastCharacter(relation), edges.snd());
        }
        if(relation.contains(ARGUMENT)) {
            edges = RELATION_TO_EDGE.get(ARGUMENT);
            edges = P(edges.fst() + lastCharacter(relation), edges.snd());
        }
        return uV_vU(f,t,edges);
    }

    private static final TypeFactGraph<Identification> empty = emptyTFG();

    private static final BinaryOperator<TypeFactGraph<Identification>> combiner = (u1,u2) ->  nullHandleReduce(u1,u2,Analysis::merge, empty);

    private static final TreeScanner<TypeFactGraph<Identification>,Pair<VisitorState, State>> GET_TFG_EXAMPLE =
        new TreeScanner<TypeFactGraph<Identification>, Pair<VisitorState, State>>() {

            @Override
            public TypeFactGraph<Identification> visitVariable(VariableTree v, Pair<VisitorState, State> s) {
                final Identification vid = s.snd().getRootID();
                return merge(empty.map(addNode(vid)), scanTypeDependent(s));
            }

            @Override
            public TypeFactGraph<Identification> visitMethod(MethodTree m, Pair<VisitorState, State> s) {
                final Identification mid = s.snd().getRootID();
                return merge(empty.map(addNode(mid)), scanTypeDependent(s));
            }

            @Override
            public TypeFactGraph<Identification> visitIdentifier(IdentifierTree i, Pair<VisitorState, State> s) {
                final Identification iid = s.snd().getRootID();
                return merge(empty.map(addNode(iid)), scanTypeDependent(s));
            }

            @Override
            public TypeFactGraph<Identification> visitMemberSelect(MemberSelectTree m, Pair<VisitorState, State> s) {
                return scanTypeDependent(s);
            }

//            @Override
//            public TypeFactGraph<Identification> visitBinary(BinaryTree m, Pair<VisitorState, State> s) {
//                return scanTypeDependent(s);
//            }

            @Override
            public TypeFactGraph<Identification> visitParameterizedType(ParameterizedTypeTree i, Pair<VisitorState, State> s) {
                Identification pid = s.snd().getRootID();
                TypeFactGraph<Identification> acc = emptyTFG();
                acc = acc.map(addEdge(pid, ID(NOT_PRIVATE, MODIFIER, null, pid), MODIFIER));
                return acc;
            }

            @Override
            public TypeFactGraph<Identification> visitLiteral(LiteralTree m, Pair<VisitorState, State> s) {
                return empty.map(addNode(s.snd().getRootID()));
            }

            @Override
            public TypeFactGraph<Identification> visitMethodInvocation(MethodInvocationTree root, Pair<VisitorState, State> s) {
                final Identification mid = s.snd().getRootID();
                return merge(empty.map(addNode(mid)), scanTypeDependent(s));
            }

            @Override
            public TypeFactGraph<Identification> visitNewClass(NewClassTree root, Pair<VisitorState, State> p) {
                final Identification ncid = p.snd().getRootID();
                return merge(empty.map(addNode(ncid)), scanTypeDependent(p));
            }

            @Override
            public TypeFactGraph<Identification> visitLambdaExpression(LambdaExpressionTree l, Pair<VisitorState, State> p) {
                return matchesTypeT().matches(l, p.fst()) ? empty.map(addNode(p.snd().getRootID())) : empty;
            }

//            @Override
//            public TypeFactGraph<Identification> visitClass(ClassTree root, Pair<VisitorState, State> s) {
//                final Identification cid = s.snd().getRootID();
//                TypeFactGraph<Identification> acc = emptyTFG();
//                if (superIs(matchesTypeT(allProgramsT(mapping))).matches(root, s.fst())) {
//                    acc = merge(acc,empty.map(addNode(cid)));
//                    List<State> overridenMethods = new ArrayList<>();
//                    for (Pair<State, String> t : s.snd().getTypeDependents()) {
//                        final TypeFactGraph<Identification> dependent = GET_TFG_EXAMPLE.scan(t.fst().getRoot(), P(s.fst(), t.fst()));
//                        if (!dependent.equals(empty)) {
//                            List<State> ovrMthd = root.getMembers().stream().filter(x -> x.getKind().equals(Tree.Kind.METHOD))
//                                    .map(x -> (MethodTree) x)
//                                    .filter(x -> ASTHelpers.findSuperMethodInType(ASTHelpers.getSymbol(x), ASTHelpers.getType(t.fst().getRoot()), s.fst().getTypes()) != null)
//                                    .map(x -> TREE2STATE.scan(x, cid))
//                                    .collect(toList());
//                            overridenMethods.addAll(ovrMthd);
//                        }
//                    }
//
//                    for (State om : overridenMethods) {
//                        acc = acc.map(addEdge(cid, om.getRootID(), OVERRIDES));
//                        for (Pair<State, String> z : om.getTypeDependents()) {
//                            acc = acc.map(addEdge(om.getRootID(), z.fst().getRootID(), z.snd()));
//                        }
//                    }
//                }
//
//                return merge(acc, scanTypeDependent(s));
//            }

            @Override
            public TypeFactGraph<Identification> reduce(TypeFactGraph<Identification> u1, TypeFactGraph<Identification> u2) {
                return combiner.apply(u1, u2);
        }
    };



    private static TypeFactGraph<Identification> scanTypeDependent(Pair<VisitorState, State> s) {
        TypeFactGraph<Identification> acc = empty;
        for (Pair<State, String> t : s.snd().getTypeDependents()) {
            if(t.fst()!= null) {
                final TypeFactGraph<Identification> dependent = GET_TFG_EXAMPLE.scan(t.fst().getRoot(), P(s.fst(), t.fst()));
                if (dependent.nodes_p().count() > 0) {
                    acc = acc.map(addEdge(s.snd().getRootID(),t.fst().getRootID(),t.snd()));
                    acc = merge(acc, dependent);
                }
            }
        }
        return acc;
    }

    private static final Function<Pair<VisitorState,State>,TypeFactGraph<Identification>> translator
            = x -> GET_TFG_EXAMPLE.scan(x.snd().getRoot(),P(x.fst(),x.snd()));

    public static Tree2State2U<TypeFactGraph<Identification>> TFG_CREATOR_EXAMPLE (VisitorState s){
        return new Tree2State2U<>(empty,combiner,translator, s);
    }


}

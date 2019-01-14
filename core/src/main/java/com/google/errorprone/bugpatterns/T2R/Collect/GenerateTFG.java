package com.google.errorprone.bugpatterns.T2R.Collect;

import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.allProgramsT;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Migrate.mapping;
import static com.google.errorprone.bugpatterns.T2R.Collect.CollectMatchers.isNewClassInitOf;
import static com.google.errorprone.bugpatterns.T2R.Collect.CollectMatchers.isSubType;
import static com.google.errorprone.bugpatterns.T2R.Collect.CollectMatchers.matchesTypeSubT;
import static com.google.errorprone.bugpatterns.T2R.Collect.CollectMatchers.matchesTypeT;
import static com.google.errorprone.bugpatterns.T2R.Collect.CollectMatchers.newClassHasArguments;
import static com.google.errorprone.bugpatterns.T2R.Collect.CollectMatchers.superIs;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.ID;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.getIdFromSymbol;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.ARGUMENT;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.ASSIGNMENT;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.BIN_OP;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.EXTENDS;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.IMPLEMENTS;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.INITIALIZED_AS;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.PARAM;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.RECEIVER;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.RETURNS;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.TREE2STATE;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.addNode;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.emptyTFG;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.uV_vU;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static com.google.errorprone.bugpatterns.T2R.common.Util.nullHandleReduce;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.hasArguments;
import static com.google.errorprone.matchers.Matchers.methodHasParameters;
import static com.google.errorprone.matchers.Matchers.methodReturns;
import static com.google.errorprone.matchers.Matchers.receiverOfInvocation;
import static com.google.errorprone.matchers.Matchers.variableType;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableMap;
import com.google.common.graph.MutableValueGraph;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.T2R.Analysis.Analysis;
import com.google.errorprone.bugpatterns.T2R.Analysis.Migrate;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.State;
import com.google.errorprone.bugpatterns.T2R.common.Tree2Id;
import com.google.errorprone.bugpatterns.T2R.common.Tree2State2U;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol.MethodSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

/**
 * Created by ameya on 12/16/18.
 */
public class GenerateTFG {


    public static final String AFFECTED_BY_HIERARCHY = "AFFECTED_BY_HIERARCHY";
    public static final String METHOD_HIERARCHY = "METHOD_HIERARCHY";
    public static final String PARAM_INDEX = "PARAMETER_INDEX:";
    public static final String ARG_INDEX = "ARGUMENT_INDEX:";
    public static final String PARENT_LAMBDA = "PARENT_LAMBDA";
    public static final String PARAM_LAMBDA = "PARAMETER_LAMBDA:";
    public static final String PASSED_AS_ARG_TO = "PASSED_AS_ARG_TO";
    public static final String ARG_PASSED = "ARG_PASSED";
    public static final String ASSIGNED_TO = "ASSIGNED_TO";
    public static final String ASSIGNED_AS = "ASSIGNED_AS";
    public static final String RECURSIVE = "RECURSIVE";
    public static final String METHOD_INVOKED = "METHOD_INVOKED";
    public static final String REFERENCE = "REFERENCE";
    public static final String TYPE_INFO = "TYPE_INFO";
    public static final String OF_TYPE  = "OF_TYPE";
    public static final String PARENT_METHOD = "PARENT_METHOD";
    public static final String RETURNED_BY  = "RETURNED_BY";
    public static final String OVERRIDES  = "OVERRIDES";
    public static final String OVERRIDEN_BY  = "OVERRIDEN_BY";
    public static final String CALLSITE = "CALLSITE";
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

    private static Function<MutableValueGraph<Identification,String>, MutableValueGraph<Identification,String>> addEdge(Identification f, Identification t, String relation){
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

    private static final TreeScanner<TypeFactGraph<Identification>,Pair<VisitorState, State>> GET_TFG  =
        new TreeScanner<TypeFactGraph<Identification>, Pair<VisitorState, State>>() {

            @Override
            public TypeFactGraph<Identification> visitVariable(VariableTree v, Pair<VisitorState, State> s) {
                final Identification vid = s.snd().getRootID();
                TypeFactGraph<Identification> acc = empty;

                if(variableType(matchesTypeT(allProgramsT(Migrate.mapping))).matches(v,s.fst())) {
                    acc = acc.map(addNode(vid));
                    if(ASTHelpers.getSymbol(v).getKind().equals(ElementKind.FIELD) && !v.getModifiers().getFlags().contains(Modifier.PRIVATE)){
                        acc = acc.map(addEdge(vid, ID(NOT_PRIVATE,MODIFIER, null,vid),MODIFIER));
                    }
                    if (variableType(matchesTypeT(Migrate.mapping)).matches(v,s.fst()) && isSubType(ASTHelpers.getType(v), s.fst())) {
                        Identification id = getIdFromSymbol(ASTHelpers.getType(v).asElement());
                        acc = acc.map(addEdge(vid, id, IMPLEMENTS));
                    }
                }

                return merge(acc, scanTypeDependent(s));
            }

            @Override
            public TypeFactGraph<Identification> visitMethod(MethodTree m, Pair<VisitorState, State> s) {
                final Identification mid = s.snd().getRootID();
                TypeFactGraph<Identification> acc = empty;
                if(methodReturns(matchesTypeT(allProgramsT(mapping))).matches(m, s.fst())
                        || methodHasParameters(AT_LEAST_ONE, matchesTypeT(allProgramsT(mapping))).matches(m, s.fst())) {
                    acc = acc.map(addNode(mid));
                    if(mid.getName().contains("constantSize")) {
                        System.out.println((s.snd().getRootID()));
                    }
                    if (m.getReturnType() != null) {
                        if (methodReturns(matchesTypeT(mapping)).matches(m, s.fst()) && isSubType(ASTHelpers.getType(m.getReturnType()), s.fst())) {
                            Identification id = getIdFromSymbol(ASTHelpers.getType(m.getReturnType()).asElement());
                            acc = acc.map(addEdge(mid, id, IMPLEMENTS));
                        }
                    }
                    if(methodReturns(matchesTypeT(mapping)).matches(m, s.fst())
                            || methodHasParameters(AT_LEAST_ONE, matchesTypeT(mapping)).matches(m, s.fst())) {
                        MethodSymbol ms = ASTHelpers.getSymbol(m);
                        List<Function<MutableValueGraph<Identification, String>, MutableValueGraph<Identification, String>>> superMethods
                                = ASTHelpers.findSuperMethods(ms, s.fst().getTypes())
                                .stream().map(Tree2Id::getIdFromSymbol).map(smid -> addEdge(mid, smid, METHOD_HIERARCHY))
                                .collect(toList());
                        acc = acc.mergeMap(superMethods);
                    }

                    if (!m.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
                        acc = acc.map(addEdge(mid, ID(NOT_PRIVATE, MODIFIER, null, mid), MODIFIER));
                    }
                }
                return merge(acc, scanTypeDependent(s));
            }

            @Override
            public TypeFactGraph<Identification> visitIdentifier(IdentifierTree i, Pair<VisitorState, State> s) {
                final Identification iid = s.snd().getRootID();
                TypeFactGraph<Identification> acc = (matchesTypeT(allProgramsT(mapping)).matches(i, s.fst())) ? empty.map(addNode(iid)) : empty;
                return merge(acc, scanTypeDependent(s));
            }

            @Override
            public TypeFactGraph<Identification> visitMemberSelect(MemberSelectTree m, Pair<VisitorState, State> s) {
                return scanTypeDependent(s);
            }

            @Override
            public TypeFactGraph<Identification> visitBinary(BinaryTree m, Pair<VisitorState, State> s) {
                return scanTypeDependent(s);
            }

            @Override
            public TypeFactGraph<Identification> visitParameterizedType(ParameterizedTypeTree i, Pair<VisitorState, State> s) {
                return empty.map(addNode(s.snd().getRootID()));
            }

            @Override
            public TypeFactGraph<Identification> visitLiteral(LiteralTree m, Pair<VisitorState, State> s) {
                return (matchesTypeT().matches(m, s.fst())) && s.snd().getRootID() != null
                        ? empty.map(addNode(s.snd().getRootID())) : empty;
            }

            @Override
            public TypeFactGraph<Identification> visitMethodInvocation(MethodInvocationTree root, Pair<VisitorState, State> s) {
                final Identification mid = s.snd().getRootID();
                TypeFactGraph<Identification> acc = emptyTFG();
               // System.out.println("**********" + s.snd().getRoot() + "       " + qualifiedName(s.snd().getRootID())     );

                if(ASTHelpers.getReceiver(root)!= null
                        && !receiverOfInvocation(matchesTypeT((mapping))).matches(root,s.fst())
                        && ((hasArguments(AT_LEAST_ONE, matchesTypeT())).matches(root,s.fst()) || matchesTypeT().matches(root,s.fst()))){

                    acc = acc.map(addEdge(mid,getIdFromSymbol(ASTHelpers.getSymbol(root).owner),DECLARED_IN));
                    acc = acc.map(addEdge(mid, ID(NOT_PRIVATE, MODIFIER, null, mid), MODIFIER));
                }

                if ((matchesTypeT(mapping)).matches(root,s.fst())) {
//                    if(mid.getName().contains("constantSize")) {
//                        System.out.println((s.snd().getRootID()));
//                    }
                    acc = acc.map(addNode(mid));
                    if (isSubType(ASTHelpers.getReturnType(root), s.fst())) {
                        Identification id = getIdFromSymbol(ASTHelpers.getType(root).asElement());
                        acc = acc.map(addEdge(mid, id, IMPLEMENTS));
                    }
                }
                return merge(acc, scanTypeDependent(s));
            }

            @Override
            public TypeFactGraph<Identification> visitNewClass(NewClassTree root, Pair<VisitorState, State> p) {
                final Identification ncid = p.snd().getRootID();
                TypeFactGraph<Identification> acc = emptyTFG();
                if(newClassHasArguments(AT_LEAST_ONE,matchesTypeT()).matches(root,p.fst())){
                    acc = acc.map(addEdge(ncid, ID(NOT_PRIVATE, MODIFIER, null, ncid), MODIFIER));
                }
                if(isNewClassInitOf(matchesTypeSubT(mapping)).matches(root, p.fst())){
                    acc = acc.map(addEdge(ncid,getIdFromSymbol(ASTHelpers.getResultType(root).asElement()),IMPLEMENTS));
                    acc = acc.map(addEdge(ncid, ID(NOT_PRIVATE, MODIFIER, null, ncid), MODIFIER));
                }
                return merge(acc, scanTypeDependent(p));
            }

            @Override
            public TypeFactGraph<Identification> visitLambdaExpression(LambdaExpressionTree l, Pair<VisitorState, State> p) {
                return matchesTypeT().matches(l, p.fst()) ? empty.map(addNode(p.snd().getRootID())) : empty;
            }

            @Override
            public TypeFactGraph<Identification> visitClass(ClassTree root, Pair<VisitorState, State> s) {
                final Identification cid = s.snd().getRootID();
                TypeFactGraph<Identification> acc = emptyTFG();
                if (superIs(matchesTypeT(allProgramsT(mapping))).matches(root, s.fst())) {
                    acc = merge(acc,empty.map(addNode(cid)));
                    List<State> overridenMethods = new ArrayList<>();
                    for (Pair<State, String> t : s.snd().getTypeDependents()) {
                        final TypeFactGraph<Identification> dependent = GET_TFG.scan(t.fst().getRoot(), P(s.fst(), t.fst()));
                        if (!dependent.equals(empty)) {
                            List<State> ovrMthd = root.getMembers().stream().filter(x -> x.getKind().equals(Tree.Kind.METHOD))
                                    .map(x -> (MethodTree) x)
                                    .filter(x -> ASTHelpers.findSuperMethodInType(ASTHelpers.getSymbol(x), ASTHelpers.getType(t.fst().getRoot()), s.fst().getTypes()) != null)
                                    .map(x -> TREE2STATE.scan(x, cid))
                                    .collect(toList());
                            overridenMethods.addAll(ovrMthd);
                        }
                    }

                    for (State om : overridenMethods) {
                        acc = acc.map(addEdge(cid, om.getRootID(), OVERRIDES));
                        for (Pair<State, String> z : om.getTypeDependents()) {
                            acc = acc.map(addEdge(om.getRootID(), z.fst().getRootID(), z.snd()));
                        }
                    }
                }

                return merge(acc, scanTypeDependent(s));
            }

            @Override
            public TypeFactGraph<Identification> reduce(TypeFactGraph<Identification> u1, TypeFactGraph<Identification> u2) {
                return combiner.apply(u1, u2);
        }
    };



    private static TypeFactGraph<Identification> scanTypeDependent(Pair<VisitorState, State> s) {
        TypeFactGraph<Identification> acc = empty;
        for (Pair<State, String> t : s.snd().getTypeDependents()) {
            if(t.fst()!= null) {
                final TypeFactGraph<Identification> dependent = GET_TFG.scan(t.fst().getRoot(), P(s.fst(), t.fst()));
                if (dependent.nodes_p().count() > 0) {
                    acc = acc.map(addEdge(s.snd().getRootID(),t.fst().getRootID(),t.snd()));
                    acc = merge(acc, dependent);
                }
            }
        }
        return acc;
    }

    private static final Function<Pair<VisitorState,State>,TypeFactGraph<Identification>> translator
            = x -> GET_TFG.scan(x.snd().getRoot(),P(x.fst(),x.snd()));

    public static Tree2State2U<TypeFactGraph<Identification>> TFG_CREATOR (VisitorState s){
        return new Tree2State2U<>(empty,combiner,translator, s);
    }


}

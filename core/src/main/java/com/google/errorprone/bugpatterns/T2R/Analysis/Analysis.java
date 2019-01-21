package com.google.errorprone.bugpatterns.T2R.Analysis;


import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.ARG_INDEX;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.ARG_PASSED;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.DECLARED_IN;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.METHOD_HIERARCHY;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.NOT_PRIVATE;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.OVERRIDEN_BY;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.PARAM_INDEX;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.PARENT_METHOD;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.PASSED_AS_ARG_TO;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.addEdge;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.lastCharacter;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.INFERRED_;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.SUPER_CLAUSE;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.emptyTFG;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.getSuccessorWithEdge;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.getSuccessorsWithEdge;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.removeEdge;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.removeNodes;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.replace;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.uV_vU;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Streams;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableValueGraph;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeInfo;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeSignature;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by ameya on 12/13/18.
 */
public class Analysis {

    public static final String LOCAL_VARIABLE = "LOCAL_VARIABLE";
    public static final String FIELD = "FIELD";
    public static final String PARAMETER = "PARAMETER";
    public static final String METHOD = "METHOD";


    public static boolean isVarKind(Identification n){
        return n.getKind().equals(PARAMETER) || n.getKind().equals(LOCAL_VARIABLE) || n.getKind().equals(FIELD);
    }

    public static boolean isSuperClause (Identification id){
        return id.getKind().equals(SUPER_CLAUSE);
    }

    public static Predicate<Identification> inferredVarKind = n -> n.getKind().equals(INFERRED_+PARAMETER) || n.getKind().equals(INFERRED_ + LOCAL_VARIABLE)
            || n.getKind().equals(INFERRED_ + FIELD);

    public static TypeFactGraph<Identification> paramArgRelation(TypeFactGraph<Identification> t){

        Set<Pair<Identification, Set<Pair<Identification, Set<Identification>>>>> mthdParamArg =
                t.get().nodes().stream().filter(x -> (x.getKind().equals(METHOD) || x.getKind().equals("CONSTRUCTOR")) && !getSuccessorWithEdge(t,x,OVERRIDEN_BY).isPresent())
                .map(m -> P(m, getSuccessorsWithEdge(t, m, PARAM_INDEX).stream()
                        .map(x -> Integer.parseInt(lastCharacter(t.get().edgeValue(m, x).get())))
                        .map(x -> P(getSuccessorWithEdge(t, m, PARAM_INDEX + x).get(), getSuccessorsWithEdge(t, m, ARG_INDEX + x)))
                        .collect(toSet())))
                .collect(toSet());

        List<Function<MutableValueGraph<Identification, String>, MutableValueGraph<Identification, String>>> newEdges =
                mthdParamArg.stream().flatMap(x -> x.snd().stream())
                .flatMap(x -> x.snd().stream().map(a -> uV_vU(a, x.fst(), P(PASSED_AS_ARG_TO, ARG_PASSED))))
                .collect(toList());

        List<Pair<Identification, Identification>> mthd_param_arg = mthdParamArg.stream().flatMap(x -> x.snd().stream()
                        .flatMap(p -> p.snd().stream()).map(z -> P(x.fst(), z))).collect(toList());

        return removeEdge(t.mergeMap(newEdges),mthd_param_arg);
    }

    public static TypeFactGraph<Identification> propogateAffectedByHierarchy(TypeFactGraph<Identification> tfg){

        List<EndpointPair<Identification>> hierAffMthd = tfg.get().edges().parallelStream()
                .filter(x -> tfg.get().edgeValue(x.nodeU(), x.nodeV()).map(e -> e.equals(METHOD_HIERARCHY)).orElse(false))
                .filter(x -> x.nodeU().getKind().equals(METHOD)).collect(toList());
        List<Function<MutableValueGraph<Identification, String>, MutableValueGraph<Identification, String>>> params = new ArrayList<>();
        for(EndpointPair<Identification> e: hierAffMthd){
            List<String> paramsEdges = getSuccessorsWithEdge(tfg, e.nodeU(), PARAM_INDEX).stream()
                    .map(x -> tfg.get().edgeValue(e.nodeU(), x).get()).collect(toList());
            params.addAll(paramsEdges.stream().map(p -> P(getSuccessorWithEdge(tfg, e.nodeU(), p).get(), getSuccessorWithEdge(tfg, e.nodeV(), p).get()))
                    .map(x -> addEdge(x.fst(), x.snd(), METHOD_HIERARCHY))
                    .collect(toList()));
        }

        return tfg.mergeMap(params);
    }

    private static TypeFactGraph<Identification> resolveLocalVariables(TypeFactGraph<Identification> t) {
        final List<Identification> localVariablesToResolve = t.nodes_p().filter(x->x.getKind().contains(INFERRED_+LOCAL_VARIABLE)).collect(toList());

        final BiPredicate<Identification, Identification> resolveLocalVariables = (d,u) -> u.getName().equals(d.getName()) && u.getType().equals(d.getType());

        final List<Pair<Identification, Identification>> replace_with = t.nodes_p().filter(x -> x.getKind().equals(LOCAL_VARIABLE))
                .map(d -> P(d, localVariablesToResolve.stream().filter(u -> resolveLocalVariables.test(d, u)).findFirst()))
                .filter(p -> p.snd().isPresent()).map(p -> P( p.snd().get(), p.fst())).collect(toList());

        return replace(t,replace_with);
    }


    public static TypeFactGraph<Identification> resolveSuperClause(TypeFactGraph<Identification> tfg){
        List<Identification> inferredSuperclause = tfg.nodes_p().filter(x -> x.getKind().equals(INFERRED_ + "CLASS")
                || x.getKind().equals(INFERRED_ + "INTERFACE")).collect(toList());
        List<Identification> foundClasses = tfg.nodes_p().filter(x -> x.getKind().equals("CLASS") || x.getKind().equals("INTERFACE")).collect(toList());

        List<Pair<Identification, Identification>> replace_with = foundClasses.stream()
                .map(c -> P(inferredSuperclause.stream().filter(x -> x.getName().equals(c.getName()) && typesMatch(c.getType(), x.getType())).findFirst(),c))
                .filter(x -> x.fst().isPresent()).map(x -> P(x.fst().get(), x.snd())).collect(toList());
        return replace(tfg,replace_with);
    }

    public static TypeFactGraph<Identification> removeNonPvt(TypeFactGraph<Identification> tfg){
        List<Identification> nonPvtNodes = tfg.nodes_p().filter(x -> x.getName().equals(NOT_PRIVATE)).collect(toList());
        return removeNodes(tfg,nonPvtNodes);
    }

    public static TypeFactGraph<Identification> removeParentParam(TypeFactGraph<Identification> tfg){
        List<Pair<Identification,Identification>> nonPvtNodes = tfg.get().edges().stream().filter(e -> tfg.get().edgeValue(e.nodeU(),e.nodeV()).get().equals(PARENT_METHOD))
                .filter(e -> !getSuccessorWithEdge(tfg,e.nodeV(),OVERRIDEN_BY).isPresent())
                .map(x -> P(x.nodeU(),x.nodeV())).collect(toList());
        return removeEdge(tfg,nonPvtNodes);
    }

    public static TypeFactGraph<Identification> simplifyMethods(TypeFactGraph<Identification> tfg){
        if(tfg == null) {
            return emptyTFG();
        }
        final List<Pair<Identification, Identification>> resolvedInferredMthds = tfg.get().edges().stream()
                .filter(e -> tfg.get().edgeValue(e.nodeU(), e.nodeV()).get().equals(DECLARED_IN))
                .map(e -> P(e.nodeU(),e.nodeU().toBuilder().setOwner(e.nodeV()).build()))
                .collect(toList());

        TypeFactGraph<Identification> updTFG = removeNodes(tfg,tfg.get().edges().stream()
                .filter(e -> tfg.get().edgeValue(e.nodeU(), e.nodeV()).get().equals(DECLARED_IN))
                .map(EndpointPair::nodeV).collect(toList()));

        return replace(updTFG, resolvedInferredMthds);
    }

    public static TypeFactGraph<Identification> resolveInferred(TypeFactGraph<Identification> tfg) {
        if(tfg == null) {
            return emptyTFG();
        }
        final List<Pair<Identification,Identification>> replace_with = tfg.get().nodes().stream()
                .filter(Analysis::isInferred).filter(x -> tfg.get().nodes().contains(makeNonInferred(x)))
                .map(x -> P(x,makeNonInferred(x))).collect(toList());
        return resolveLocalVariables(replace(tfg,replace_with));
    }

    private static boolean isInferred(Identification id) {
        return id.hasOwner() && (id.getKind().contains(INFERRED_) || isInferred(id.getOwner()));
    }

    public static boolean typesMatch (TypeSignature t1, TypeSignature t2) {
        if (t1.hasMthdSign() && t2.hasMthdSign()) {
            return typeInfoMatch(t1.getMthdSign().getReturnType(), t2.getMthdSign().getReturnType())
                    && Streams.zip(t1.getMthdSign().getParamList().stream(), t2.getMthdSign().getParamList().stream(), Analysis::typeInfoMatch)
                    .allMatch(x -> x);
        }
        return t1.hasTypeSign() && t2.hasTypeSign() && typeInfoMatch(t1.getTypeSign(), t2.getTypeSign());
    }

    public static boolean typesMatch(TypeSignature t1, TypeInfo r) {
        if (t1.hasMthdSign()) {
            return typeInfoMatch(r, t1.getMthdSign().getReturnType());
        }
        return typeInfoMatch(r, t1.getTypeSign());
    }

    public static TypeInfo getIdType(TypeSignature t){
        return t.hasTypeSign() ? t.getTypeSign() : t.getMthdSign().getReturnType();
    }

    public static boolean typeInfoMatch(TypeInfo r, TypeInfo t) {
        if(r.hasAnyType())
            return true;
        return r.hasAnyType() || (r.getOf().getInterfaceName().equals(t.getOf().getInterfaceName()) && r.getOf().getTypeParameterCount() == t.getOf().getTypeParameterCount()
                && Streams.zip(r.getOf().getTypeParameterList().stream(), t.getOf().getTypeParameterList().stream(), Analysis::typeInfoMatch).allMatch(x -> x));
    }

    public static Identification makeNonInferred(Identification id){
        Identification.Builder i = id.toBuilder();
        if(id.getKind().contains(INFERRED_))
            i.setKind(id.getKind().replace(INFERRED_,""));
        if(id.hasOwner())
            i.setOwner(makeNonInferred(id.getOwner()));
        return i.build();
    }

    public static TypeFactGraph<Identification> merge(TypeFactGraph<Identification> gl, TypeFactGraph<Identification> gr) {
        return resolveInferred(TypeFactGraph.mergeGraphs(gl,gr));
    }

    public static List<Set<Identification>> induceConnectedSubgraphs(TypeFactGraph<Identification> tfgs) {
        List<Set<Identification>> inducedSubGraphNodes = new ArrayList<>();
        List<Identification> st = new ArrayList<>(tfgs.get().nodes());
        while (!st.isEmpty()) {
            Identification id = st.get(0);
            Set<Identification> reachables = Graphs.reachableNodes(tfgs.get().asGraph(), id).parallelStream().collect(toSet());
            reachables.add(id);
            inducedSubGraphNodes.add(reachables);
            st.removeAll(reachables);
        }
        return inducedSubGraphNodes.stream().filter(x -> x.size() > 1).collect(toList());
    }
}
package com.google.errorprone.bugpatterns.refactoringexperiment.analysis;


import static com.google.errorprone.bugpatterns.refactoringexperiment.Constants.FIELD;
import static com.google.errorprone.bugpatterns.refactoringexperiment.Constants.LOCAL_VARIABLE;
import static com.google.errorprone.bugpatterns.refactoringexperiment.Constants.PARAMETER;
import static com.google.errorprone.bugpatterns.refactoringexperiment.Constants.REFACTOR_INFO;
import static com.google.errorprone.bugpatterns.refactoringexperiment.Constants.WRAPPER_CLASSES;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableValueGraph;
import com.google.errorprone.bugpatterns.refactoringexperiment.Constants;
import com.google.errorprone.bugpatterns.refactoringexperiment.ProtoBuffPersist;
import com.google.errorprone.bugpatterns.refactoringexperiment.models.RefactorInfoOuterClass.RefactorInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ameya on 1/26/18.
 */
public class Analyzer {




    public static Predicate<MutableValueGraph<Node,String>> PRE_CONDITION_1 = graph ->
            !graph.edges().stream().filter(endpt -> graph.edgeValue(endpt.nodeU(),endpt.nodeV()).equals(Relationships.arg_passed))
                    .map(endpt->endpt.nodeV()).anyMatch(v->!v.kind.equals(Constants.lambdaExpr));

    public static Predicate<MutableValueGraph<Node,String>> PRE_CONDITION_2 = graph ->
            !graph.edges().stream().filter(endpt -> graph.edgeValue(endpt.nodeU(),endpt.nodeV()).equals(Relationships.assigned_as))
                    .anyMatch(endpt->(endpt.nodeV().kind.equals(PARAMETER) || endpt.nodeU().kind.equals(PARAMETER)));

    public static Predicate<MutableValueGraph<Node,String>> PRE_CONDITION_3 = graph ->
            !graph.edges().stream().filter(endpt -> graph.edgeValue(endpt.nodeU(),endpt.nodeV()).equals(Relationships.passed_to_non_editable))
                    .anyMatch(endpt->(endpt.nodeV().kind.equals(PARAMETER) || endpt.nodeU().kind.equals(PARAMETER)));

    public static void main(String args[]) throws Exception {

        MutableValueGraph<Node, String> graphOfGraphs = CreateGraph.create();
        List<Node> params = graphOfGraphs.nodes().stream().filter(x -> x.kind.equals(PARAMETER)).collect(Collectors.toList());
        List<MutableValueGraph<Node, String>> subGraphs = new ArrayList<>();
        for (Node n : params) {
            Set<Node> reachables = Graphs.reachableNodes(graphOfGraphs.asGraph(), n);
            n.visited = true;
            reachables.stream().forEach(x -> x.visited = true);
            subGraphs.add(Graphs.inducedSubgraph(graphOfGraphs, reachables));
        }
        //
        subGraphs.stream().filter(PRE_CONDITION_1).filter(PRE_CONDITION_2).filter(PRE_CONDITION_3).forEach(g -> {
            Node param = g.nodes().stream().filter(x -> {

                return x.kind.equals(PARAMETER) || x.kind.equals(LOCAL_VARIABLE) || x.kind.equals(FIELD);
            }).findFirst().get();
            Entry<String, List<String>> refactInfo = getRefactorFromInfo(param.type);
            String refactorTo = Mapping.getMappedTypeFor(refactInfo.getKey(),refactInfo.getValue().get(0),refactInfo.getValue().get(1));
            g.nodes().stream().map(n -> mapToRefactorObj(n,refactorTo)).forEach(r -> ProtoBuffPersist.write(r,REFACTOR_INFO));
        });


    }

    private static RefactorInfo.Builder mapToRefactorObj(Node n, String refactorTo) {

        RefactorInfo.Builder ri = RefactorInfo.newBuilder();
        return ri.setName(n.name)
                .setKind(n.kind)
                .setOwner(n.owner)
                .setType(n.type)
                .setRefactorTo(refactorTo);
    }

    public static Entry<String,List<String>> getRefactorFromInfo(String type){
        String className = type.substring(0, type.indexOf("<"));
        List<String> typeParam = new ArrayList<>();

        if(WRAPPER_CLASSES.stream().anyMatch(x -> x.equals(type.substring(type.indexOf("<")+1,type.indexOf(","))))){
            typeParam.add(type.substring(type.indexOf("<")+1,type.indexOf(",")));
            typeParam.add(type.substring(type.indexOf(",")+1));
        }else{
            typeParam.add(type.substring(0,type.lastIndexOf(",")));
            typeParam.add(type.substring(type.lastIndexOf(",")+1,type.lastIndexOf(">")));

        }
        return Maps.immutableEntry(className,typeParam);
    }

}

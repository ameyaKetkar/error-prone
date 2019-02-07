package com.google.errorprone.bugpatterns.T2R.Mappings;

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.induceDisconnectedSubgraphs;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Migrate.editType;
import static com.google.errorprone.bugpatterns.T2R.Mappings.GenerateTFGExample.ASSIGNED_AS;
import static com.google.errorprone.bugpatterns.T2R.Mappings.GenerateTFGExample.PASSED_AS_ARG_TO;
import static com.google.errorprone.bugpatterns.T2R.Mappings.GenerateTFGExample.TFG_CREATOR_EXAMPLE;
import static com.google.errorprone.bugpatterns.T2R.Mappings.GetMethods.GET_METHODS;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.TREE2ID;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.getPackageId;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.induceGraph;
import static com.google.errorprone.bugpatterns.T2R.common.Util.L;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.auto.service.AutoService;
import com.google.common.collect.Streams;
import com.google.common.graph.EndpointPair;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program;
import com.google.errorprone.bugpatterns.T2R.common.RWProtos;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;
import com.google.errorprone.matchers.Description;

import com.sun.source.tree.*;
import org.checkerframework.checker.nullness.Opt;

import java.lang.invoke.MethodType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@AutoService(BugChecker.class)
@BugPattern(
        name = "ScanMappings",
        category = JDK,
        summary = "Collection phase for type migration!!!",
        severity = ERROR,
        linkType = CUSTOM,
        link = "example.com/bugpattern/Collect"
)


public class ScanMappings extends BugChecker implements BugChecker.CompilationUnitTreeMatcher {


    public static Optional<Program> detectChangeType(VariableTree v1, VariableTree v2){
        if(!v1.getName().equals(v2.getName())) throw new RuntimeException("Declaration names should not change");
        Identification id1 = TREE2ID.scan(v1,null);
        Identification id2 = TREE2ID.scan(v2,null);
        return Optional.ofNullable(
                id1.getType().equals(id2.getType())
                ? null : Program.newBuilder().setFrom(id1.getType().getTypeSign())
                        .setTo(id2.getType().getTypeSign())
                        .setDeclarations(editType(id2.getType().getTypeSign())).build());

    }

    public static List<Pair<VariableTree,VariableTree>> matchBeforeAfter(List<VariableTree> d1, List<VariableTree> d2){
        if(d1.size()<d2.size()) throw new RuntimeException("V1 cannot have lesser methods");
        List<Pair<VariableTree,VariableTree>> ds = new ArrayList<>();
        for(VariableTree i:d1){
            for(VariableTree j:d2){
                if(i.getName().equals(j.getName()))
                    ds.add(P(i,j));
            }
        }
        return ds;
    }

    public static List<VariableTree> getDeclarationsInBody (MethodTree m1){
        return m1.getBody().getStatements().isEmpty() ?
                L() : m1.getBody().getStatements().stream()
                .filter(x -> x.getKind().equals(Tree.Kind.VARIABLE))
                .map(x -> (VariableTree)x)
                .collect(toList());
    }

    public static List<Program> getPrograms(MethodTree m1, MethodTree m2){

        if(m1.getParameters().size()!=m2.getParameters().size()) throw new RuntimeException("Number of paramters should be equal");

        return Stream.concat(Streams.zip(m1.getParameters().stream(),m2.getParameters().stream(),ScanMappings::detectChangeType)
                .filter(Optional::isPresent).map(x->x.get()),
                matchBeforeAfter(getDeclarationsInBody(m1),getDeclarationsInBody(m2))
                .stream().map(p->detectChangeType(p.fst(),p.snd())).filter(Optional::isPresent).map(x->x.get()))
                .collect(toList());
    }



    @Override
    public Description matchCompilationUnit(CompilationUnitTree cu, VisitorState s) {

       Set<Pair<MethodTree, MethodTree>> mthdPair = getMethods(cu,s);



        List<Program> programsRequired = getMethods(cu,s);




        //TypeFactGraph<Identification> tfg = TFG_CREATOR_EXAMPLE(s).scan(cu, getPackageId(cu));
        //RWProtos.pckgName = "/Users/ameya/FinalResults/error-prone/ProtoBufOutput/";
        RWProtos.write(tfg.asTFG(),"TFG");
        final Set<TypeFactGraph<Identification>> relevantSubTFGs = induceDisconnectedSubgraphs(tfg).stream()
                .map(x -> induceGraph(tfg, x)).collect(toSet());
        Set<Set<Pair<EndpointPair<Identification>, String>>> matchedConstruct = matchLC(relevantSubTFGs);
        return null;
    }

    private Set<Pair<MethodTree, MethodTree>> getMethods(CompilationUnitTree cu, VisitorState s) {
        Set<MethodTree> methods = GET_METHODS(s).scan(cu, getPackageId(cu));
        Set<Pair<MethodTree, MethodTree>> mthds = new HashSet<>();
        for(MethodTree n1 : methods){
            for(MethodTree n2 : methods){
                if(!n1.equals(n2) && n1.getName().toString().replace("before","")
                .equals(n2.getName().toString().replace("after_",""))){
                    mthds.add(P(n1,n2));
                }
            }
        }
        return mthds;
    }

    private Program generateProgram(Pair<TypeFactGraph<Identification>, TypeFactGraph<Identification>> tfgPair) {
        return relevantSubTFGs.stream()
                .map(x -> x.get().edges().stream()
                        .map(e -> P(e,x.get().edgeValue(e.nodeU(),e.nodeV()).get()))
                        .filter(p -> p.snd().contains(ASSIGNED_AS) || p.snd().contains(PASSED_AS_ARG_TO))
                        .collect(toSet()))
                .collect(toSet());
    }

    private static <X> Optional<X> maybeGet(List<X> xs,int i){
        return i >= xs.size() ? Optional.empty() : Optional.of(xs.get(i));
    }

    private static <U,V> List<Pair<Optional<U>, Optional<V>>> zipUnequal(final List<U> us, final List<V> vs){
        return IntStream.range(0,Math.max(us.size(),vs.size()))
                .mapToObj(i -> P(maybeGet(us,i), maybeGet(vs,i))).collect(toList());
    }

    public static class TFGPair extends Pair<TypeFactGraph<Identification>, TypeFactGraph<Identification>> {

        public TFGPair(TypeFactGraph<Identification> fst, TypeFactGraph<Identification> snd) {
            super(fst, snd);
        }
    }

}

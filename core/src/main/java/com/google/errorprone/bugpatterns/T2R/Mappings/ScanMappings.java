package com.google.errorprone.bugpatterns.T2R.Mappings;

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.induceDisconnectedSubgraphs;
import static com.google.errorprone.bugpatterns.T2R.Mappings.GenerateTFGExample.ASSIGNED_AS;
import static com.google.errorprone.bugpatterns.T2R.Mappings.GenerateTFGExample.PASSED_AS_ARG_TO;
import static com.google.errorprone.bugpatterns.T2R.Mappings.GenerateTFGExample.TFG_CREATOR_EXAMPLE;
import static com.google.errorprone.bugpatterns.T2R.Mappings.GetMethods.GET_METHODS;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.getPackageId;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.induceGraph;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static java.util.stream.Collectors.toSet;

import com.google.auto.service.AutoService;
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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;

import java.util.HashSet;
import java.util.Set;

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


    // TODO : anonymous class
    // TODO : New Class
    // TODO : why do some callsites not migrate?
    // TODO : two parameters of same method match on different types

    @Override
    public Description matchCompilationUnit(CompilationUnitTree cu, VisitorState s) {

        Set<Pair<TypeFactGraph<Identification>, TypeFactGraph<Identification>>> tfgPairs =
                getMethods(cu, s).stream()
                .map(p -> P(TFG_CREATOR_EXAMPLE(s).scan(p.fst(), getPackageId(cu)),
                        TFG_CREATOR_EXAMPLE(s).scan(p.snd(), getPackageId(cu))))
                .collect(toSet());

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

    private Program generateProgram(Pair<TypeFactGraph<Identification>, TypeFactGraph<Identification>> relevantSubTFGs) {
        return relevantSubTFGs.stream()
                .map(x -> x.get().edges().stream()
                        .map(e -> P(e,x.get().edgeValue(e.nodeU(),e.nodeV()).get()))
                        .filter(p -> p.snd().contains(ASSIGNED_AS) || p.snd().contains(PASSED_AS_ARG_TO))
                        .collect(toSet()))
                .collect(toSet());
    }

}

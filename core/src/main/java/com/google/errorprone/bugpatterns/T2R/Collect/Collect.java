package com.google.errorprone.bugpatterns.T2R.Collect;


import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.induceConnectedSubgraphs;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.paramArgRelation;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.resolveInferred;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.resolveSuperClause;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.simplifyMethods;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.Refactorables;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.allRefactorables;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.cuPresent;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.cus;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.getRefactorableTFG;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.matchProgram;
import static com.google.errorprone.bugpatterns.T2R.Analysis.PreConditions.DO_NOT_MIGRATE;
import static com.google.errorprone.bugpatterns.T2R.Analysis.PreConditions.EVERYTHING_PRIVATE;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.TFG_CREATOR;
import static com.google.errorprone.bugpatterns.T2R.Refactor.GenerateFix.FIX_CREATOR;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.getPackageId;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;


import com.google.auto.service.AutoService;
import com.google.common.graph.Graphs;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.T2R.Analysis.Migrate;
import com.google.errorprone.bugpatterns.T2R.common.Models.CUsOuterClass.CUs;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.RWProtos;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;

import com.sun.source.tree.CompilationUnitTree;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@AutoService(BugChecker.class)
@BugPattern(
        name = "Collect",
        category = JDK,
        summary = "Collection phase for type migration!!!",
        severity = ERROR,
        linkType = CUSTOM,
        link = "example.com/bugpattern/Collect"
)


public class Collect extends BugChecker implements BugChecker.CompilationUnitTreeMatcher {


    // TODO : anonymous class
    // TODO : New Class
    // TODO : why do some callsites not migrate?
    // TODO : two parameters of same method match on different types

    @Override
    public Description matchCompilationUnit(CompilationUnitTree cu, VisitorState s) {

        String fileName = getFileName(cu);
        System.out.println("******" + fileName + "******");
     //   System.out.println(allRefactorables.size());
        Refactorables = cus.size() > 0
                ? cuPresent(getFileName(cu)) ? allRefactorables : new HashSet<>()
                : getRefactorablesForCu(cu,s);

        System.out.println(Refactorables.size());

            if (Refactorables.size() > 0) {
                SuggestedFix.Builder fixes = FIX_CREATOR(s).scan(cu.getTypeDecls(), getPackageId(cu));
                if (fixes != null && !fixes.isEmpty()) {
                    return describeMatch(cu, fixes.build());
                }
            }
        return describeMatch(cu);
    }


    private static Set<Refactorable> getRefactorablesForCu(CompilationUnitTree cu, VisitorState s){

        System.out.println("Collecting TFGs");
        final TypeFactGraph<Identification> tfggg = TFG_CREATOR(s).scan(cu.getTypeDecls(), getPackageId(cu));

        final TypeFactGraph<Identification> tfg = resolveSuperClause(paramArgRelation(
                resolveInferred(simplifyMethods(tfggg))));

        System.out.println("TFG Created");

        Set<TypeFactGraph<Identification>> relevantSubTFGs = induceConnectedSubgraphs(tfg).stream()
                .map(x -> TypeFactGraph.of(Graphs.inducedSubgraph(tfg.get(), x)))
                .filter(e -> matchProgram(e, Migrate.mapping).isPresent()).collect(toSet());

//        for(TypeFactGraph<Identification> tfg1: relevantSubTFGs) {
//            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&");
//            for (Identification ii : tfg1.nodes_p().collect(toList())) {
//                System.out.println(qualifiedName(ii));
//                System.out.println(prettyType(ii.getType()));
//
//            }
//            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&");
//        }

        Map<Boolean, List<TypeFactGraph<Identification>>> pvtSubTFGs = relevantSubTFGs.stream()
                .collect(partitioningBy(EVERYTHING_PRIVATE));

        RWProtos.write(pvtSubTFGs.get(Boolean.FALSE).stream().map(TypeFactGraph::asTFG).collect(toList()), "TFG");
        if(pvtSubTFGs.get(Boolean.FALSE).size() > 0){
            RWProtos.write(CUs.newBuilder().setCu(getFileName(cu)).build(),"CU");
        }

        List<TypeFactGraph<Identification>> pvtTfgs = pvtSubTFGs.get(TRUE);
        System.out.println("THE FOLLOWING PASSED");
        //pvtTfgs.stream().flatMap(TypeFactGraph::nodes_p).map(Visualizer::qualifiedName).forEach(System.out::println);

       // visualizeGraph(getFileName(cu), pvtTfgs.stream().map(TypeFactGraph::asTFG).collect(toList()));

        List<Pair<TypeFactGraph<Identification>, Set<Refactorable>>> migratedTFGs = pvtTfgs.stream()
                .map(t -> P(t, matchProgram(t, Migrate.mapping))).filter(p -> p.snd().isPresent()).map(p -> P(p.fst(), p.snd().orElseThrow(() -> new RuntimeException(p.fst().asTFG().toString()))))
                .map(e -> P(e.fst(), getRefactorableTFG(e.fst(),e.snd().snd(), new HashSet<>()))).collect(toList());

        Map<Boolean, List<Pair<TypeFactGraph<Identification>, Set<Refactorable>>>>
                passedPreConditions = migratedTFGs.stream().collect(partitioningBy(e -> DO_NOT_MIGRATE.test(e.snd())));

        if (cus.size() == 0 ) {
            passedPreConditions.get(Boolean.FALSE).stream().map(t -> t.fst().asTFG()).forEach(x -> RWProtos.write(x, "TFG"));
            if (passedPreConditions.get(Boolean.FALSE).size() > 0) {
                RWProtos.write(CUs.newBuilder().setCu(getFileName(cu)).build(), "CU");
            }
        }


        //visualizeGraphR(getFileName(cu), passedPreConditions.get(Boolean.TRUE).stream().map(Pair::snd).map(TypeFactGraph::asTFGRef).collect(toList()));
        return passedPreConditions.get(TRUE).parallelStream().flatMap(x -> x.snd().stream()).collect(toSet());
    }





    private static String getFileName(CompilationUnitTree cu) {
        return cu.getSourceFile().getName().substring(cu.getSourceFile().getName().lastIndexOf("/") + 1, cu.getSourceFile().getName().lastIndexOf("."));
    }
}




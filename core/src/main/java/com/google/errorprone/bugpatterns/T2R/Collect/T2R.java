package com.google.errorprone.bugpatterns.T2R.Collect;


import static com.google.common.graph.Graphs.inducedSubgraph;
import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.induceDisconnectedSubgraphs;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.Refactorables;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.allRefactorables;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.cuPresent;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.cus;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.getRefactorableTFG;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.matchProgram;
import static com.google.errorprone.bugpatterns.T2R.Analysis.PreConditions.DO_NOT_MIGRATE;
import static com.google.errorprone.bugpatterns.T2R.Analysis.PreConditions.EVERYTHING_PRIVATE;
import static com.google.errorprone.bugpatterns.T2R.Analysis.PreConditions.METHOD_FOUND;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.TFG_CREATOR;
import static com.google.errorprone.bugpatterns.T2R.Refactor.GenerateFix.FIX_CREATOR;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.getPackageId;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.T2R.Analysis.Analysis;
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
import java.util.function.UnaryOperator;


@AutoService(BugChecker.class)
@BugPattern(
        name = "T2R",
        category = JDK,
        summary = "Collection phase for type migration!!!",
        severity = SUGGESTION,
        linkType = CUSTOM,
        link = "example.com/bugpattern/T2R"
)


public class T2R extends BugChecker implements BugChecker.CompilationUnitTreeMatcher {

    private final String mappingsLoc;

    public T2R(){
        this.mappingsLoc = "Ow";
      //  throw new RuntimeException("Location for the mapping information is not defined");
    }
    public T2R(ErrorProneFlags flags){
        this.mappingsLoc = flags.get("T2R:mappingLocation").orElse("");
        System.out.println(mappingsLoc);
    }

    @Override
    public Description matchCompilationUnit(CompilationUnitTree cu, VisitorState s) {

        Refactorables = cus.size() > 0 && allRefactorables.size()>0
                ? cuPresent(getFileName(cu)) ? allRefactorables : new HashSet<>()
                : getRefactorablesForCu(cu,s);

        if (Refactorables.size() > 0) {
            SuggestedFix.Builder fixes = FIX_CREATOR(s).scan(cu.getTypeDecls(), getPackageId(cu));
            if (fixes != null && !fixes.isEmpty()) {
                return describeMatch(cu, fixes.build());
            }
        }

        return describeMatch(cu);
    }


    private static Set<Refactorable> getRefactorablesForCu(CompilationUnitTree cu, VisitorState s){

        final TypeFactGraph<Identification> tfg =
                UnaryOperator.<TypeFactGraph<Identification>>identity()
                .andThen(Analysis::simplifyMethods)
                .andThen(Analysis::resolveInferred)
                .andThen(Analysis::paramArgRelation)
                .andThen(Analysis::resolveSuperClause)
                        .apply(TFG_CREATOR(s).scan(cu.getTypeDecls(), getPackageId(cu)));

        if(!tfg.isEmpty()) {
            System.out.println("TFG Created for file unit: " + getFileName(cu));
        }

        final Set<TypeFactGraph<Identification>> relevantSubTFGs = induceDisconnectedSubgraphs(tfg).stream()
                .map(x -> TypeFactGraph.of(inducedSubgraph(tfg.get(), x)))
                .filter(e -> matchProgram(e, Migrate.mapping).isPresent())
                .collect(toSet());

        System.out.println(relevantSubTFGs.size());

        final Map<Boolean, List<TypeFactGraph<Identification>>> arePvtSubTFGs = relevantSubTFGs.stream()
                .collect(partitioningBy(EVERYTHING_PRIVATE));

        System.out.println(arePvtSubTFGs.get(Boolean.FALSE).size());
        System.out.println(arePvtSubTFGs.get(Boolean.TRUE).size());

        final List<TypeFactGraph<Identification>> notPvtTfgs = arePvtSubTFGs.get(Boolean.FALSE);

        RWProtos.write(notPvtTfgs.stream().map(TypeFactGraph::asTFG).collect(toList()), "TFG");
        if(arePvtSubTFGs.get(Boolean.FALSE).size() > 0){
            RWProtos.write(CUs.newBuilder().setCu(getFileName(cu)).build(),"CU");
        }

        final List<Pair<TypeFactGraph<Identification>, Set<Refactorable>>> migratedTFGs = arePvtSubTFGs.get(TRUE).stream()
                .map(t -> P(t, matchProgram(t, Migrate.mapping)))
                .filter(p -> p.snd().isPresent()).map(p -> P(p.fst(), p.snd().orElseThrow(() -> new RuntimeException(p.fst().asTFG().toString()))))
                .map(e -> P(e.fst(), getRefactorableTFG(e.fst(),e.snd().snd(), new HashSet<>())))
                .collect(toList());

        final Map<Boolean, List<Pair<TypeFactGraph<Identification>, Set<Refactorable>>>>
                passedPreConditions = migratedTFGs.stream()
                .collect(partitioningBy(e -> DO_NOT_MIGRATE.test(e.snd()) && METHOD_FOUND.test(e.fst())));

        if (cus.size() == 0 ) {
            RWProtos.write(passedPreConditions.get(Boolean.FALSE).stream().map(t->t.fst().asTFG()).collect(toList()),"TFG");
            if (passedPreConditions.get(Boolean.FALSE).size() > 0) {
                RWProtos.write(CUs.newBuilder().setCu(getFileName(cu)).build(), "CU");
            }
        }

        return passedPreConditions.get(TRUE).stream()
                .flatMap(x -> x.snd().stream())
                .collect(toSet());
    }





    private static String getFileName(CompilationUnitTree cu) {
        return cu.getSourceFile().getName().substring(cu.getSourceFile().getName().lastIndexOf("/") + 1, cu.getSourceFile().getName().lastIndexOf("."));
    }
}




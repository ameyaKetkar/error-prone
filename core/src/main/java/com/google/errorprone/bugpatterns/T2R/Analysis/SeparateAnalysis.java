package com.google.errorprone.bugpatterns.T2R.Analysis;

import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.induceDisconnectedSubgraphs1;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.getRefactorableTFG1;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.matchProgram;
import static com.google.errorprone.bugpatterns.T2R.Analysis.PreConditions.NO_INFERRED_ASSIGNMENT;
import static com.google.errorprone.bugpatterns.T2R.Analysis.PreConditions.NO_INFERRED_METHOD_IN_HIERARCHY;
import static com.google.errorprone.bugpatterns.T2R.Analysis.PreConditions.NO_INFERRED_PASSED_AS_ARG;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.emptyTFG;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.induceGraph;
import static java.util.stream.Collectors.toList;

import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.Models.TFGOuterClass.TFG;
import com.google.errorprone.bugpatterns.T2R.common.RWProtos;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Created by ameya on 1/4/19.
 */
public class SeparateAnalysis {


    public static void main(String a[]){
        final List<TFG> tfgss = RWProtos.readTFG(a[0]);

        final TypeFactGraph<Identification> gTFG = tfgss.stream().map(TypeFactGraph::of).reduce(Analysis::merge).orElse(emptyTFG());

        final TypeFactGraph<Identification> globalTFG =
                UnaryOperator.<TypeFactGraph<Identification>>identity()
                        .andThen(Analysis::simplifyMethods)
                        .andThen(Analysis::resolveInferred)
                        .andThen(Analysis::paramArgRelation)
                        .andThen(Analysis::resolveSuperClause)
                        .andThen(Analysis::removeNonPvt)
                        .andThen(Analysis::propogateAffectedByHierarchy)
                        .andThen(Analysis::removeParentParam)
                        .apply(gTFG);

        final List<TypeFactGraph<Identification>> globalTFGs = induceDisconnectedSubgraphs1(globalTFG).stream()
                .filter(x->matchProgram(x, Migrate.mapping).isPresent())
                .map(x -> induceGraph(globalTFG, x)).filter(x -> !x.isEmpty())
                .filter(NO_INFERRED_PASSED_AS_ARG)
                .filter(NO_INFERRED_ASSIGNMENT)
                .filter(NO_INFERRED_METHOD_IN_HIERARCHY)
                .collect(toList());

        System.out.println("TOTAL SubTFGs = " + globalTFGs.size());
        List<Set<Refactorable>> migratedTFGss = new ArrayList<>();
        for(int i = 0 ; i < globalTFGs.size(); i ++ ){
            TypeFactGraph<Identification> t = globalTFGs.get(i);
            Optional<Pair<Identification, Program>>  pp = matchProgram(t,Migrate.mapping);
            if(pp.isPresent()){
                Set<Refactorable> r = getRefactorableTFG1(t, new HashSet<>());
                migratedTFGss.add(r);
            }
        }
        final List<Refactorable> migrate = migratedTFGss.stream()
                .filter(x -> x.stream().noneMatch(z -> z.getEditInstructionsList().stream().anyMatch(c -> c.hasCmd() && c.getCmd().getNumber()==1)))
                .flatMap(rt -> rt.stream()).collect(toList());

        System.out.println("Number of refactorables: " + migrate.size());
        migrate.forEach(m -> RWProtos.writeRef(m,a[0]));
    }
}

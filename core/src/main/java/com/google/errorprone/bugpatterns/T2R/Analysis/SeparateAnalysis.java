package com.google.errorprone.bugpatterns.T2R.Analysis;

import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.induceConnectedSubgraphs;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.getRefactorableTFG;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.matchProgram;
import static com.google.errorprone.bugpatterns.T2R.Analysis.PreConditions.DO_NOT_MIGRATE;
import static com.google.errorprone.bugpatterns.T2R.Analysis.PreConditions.METHOD_FOUND;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.emptyTFG;
import static java.util.stream.Collectors.toList;

import com.google.common.graph.Graphs;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.Models.TFGOuterClass.TFG;
import com.google.errorprone.bugpatterns.T2R.common.RWProtos;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;

import java.util.ArrayList;
import java.util.Collection;
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
        final List<TFG> tfgs = RWProtos.read("TFG");
        if(tfgs.size()>0) {
            System.out.println("Starting global analysis");
            final TypeFactGraph<Identification> globalTFG =
                    UnaryOperator.<TypeFactGraph<Identification>>identity()
                            .andThen(Analysis::simplifyMethods)
                            .andThen(Analysis::resolveInferred)
                            .andThen(Analysis::paramArgRelation)
                            .andThen(Analysis::resolveSuperClause)
                            .andThen(Analysis::removeNonPvt)
                            .andThen(Analysis::propogateAffectedByHierarchy)
                            .andThen(Analysis::removeParentParam)
                            .apply(tfgs.stream().map(TypeFactGraph::of).reduce(emptyTFG(),Analysis::merge));

            System.out.println("Constructed a global TFG");

            final List<TypeFactGraph<Identification>> globalTFGs = induceConnectedSubgraphs(globalTFG)
                    .parallelStream()
                    .map(x -> TypeFactGraph.of(Graphs.inducedSubgraph(globalTFG.get(), x))).filter(x -> !x.isEmpty())
                    .filter(METHOD_FOUND)
                    .collect(toList());

            System.out.println("Induced sub-graphs :" + globalTFGs.size());

            List<Set<Refactorable>> migratedTFGss = new ArrayList<>();
            for (int i = 0; i < globalTFGs.size(); i++) {
                TypeFactGraph<Identification> t = globalTFGs.get(i);
                Optional<Pair<Identification, Program>> ip = matchProgram(t, Migrate.mapping);
                if(ip.isPresent()){
                    Set<Refactorable> rs = getRefactorableTFG(t, ip.get().snd(), new HashSet<>());
                    if(DO_NOT_MIGRATE.test(rs)){
                        migratedTFGss.add(rs);
                    }
                }
            }
            final List<Refactorable> migrate = migratedTFGss.stream()
                    .flatMap(Collection::stream).collect(toList());

            migrate.forEach(m -> RWProtos.write(m, "Ref"));
            System.out.println(migrate.size() + " : # of Refactorables found");
        }
    }

}

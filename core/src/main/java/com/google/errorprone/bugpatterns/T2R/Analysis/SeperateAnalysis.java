package com.google.errorprone.bugpatterns.T2R.Analysis;

import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.induceConnectedSubgraphs;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.paramArgRelation;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.removeNonPvt;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.removeParentParam;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.resolveInferred;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.resolveSuperClause;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.simplifyMethods;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.getRefactorableTFG;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.matchProgram;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.emptyTFG;
import static com.google.errorprone.bugpatterns.T2R.common.Visualizer.prettyType;
import static com.google.errorprone.bugpatterns.T2R.common.Visualizer.qualifiedName;
import static java.util.stream.Collectors.toList;

import com.google.common.graph.Graphs;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.Models.TFGOuterClass.TFG;
import com.google.errorprone.bugpatterns.T2R.common.RWProtos;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;
import com.google.errorprone.bugpatterns.T2R.common.Visualizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by ameya on 1/4/19.
 */
public class SeperateAnalysis {

    public static void main(String a[]){
        List<TFG> tfgss = RWProtos.readTFGs();

        System.out.println(tfgss.size());



        TypeFactGraph<Identification> gTFG = tfgss.stream().map(TypeFactGraph::of).reduce(Analysis::merge).orElse(emptyTFG());

       // gTFG.nodes_p().map(Visualizer::qualifiedName).forEach(System.out::println);

        TypeFactGraph<Identification> globalTFG =
                removeParentParam(removeNonPvt(resolveSuperClause(paramArgRelation(resolveInferred(simplifyMethods(gTFG))))));

        System.out.println(globalTFG.nodes_p().count());
        List<TypeFactGraph<Identification>> globalTFGs = induceConnectedSubgraphs(globalTFG).parallelStream()
                .map(x -> TypeFactGraph.of(Graphs.inducedSubgraph(globalTFG.get(), x))).filter(x -> !x.isEmpty())
            //    .peek(t -> t.nodes_p().map(Visualizer::qualifiedName).forEach(System.out::println))
                .collect(toList());
      //  System.out.println("^^^^^^^^^^^^^^^^^^^^^^^");
        List<Set<Refactorable>> migratedTFGss = new ArrayList<>();
        for(int i = 0 ; i < globalTFGs.size(); i ++ ){
            TypeFactGraph<Identification> t = globalTFGs.get(i);
            Optional<Pair<Identification, Program>>  pp = matchProgram(t,Migrate.mapping);
            if(pp.isPresent()){
                System.out.println("MATCHED AT : ");
                System.out.println(qualifiedName(pp.get().fst()));
                System.out.println(prettyType(pp.get().fst().getType()));
                System.out.println("##");
                Set<Refactorable> r = getRefactorableTFG(t, pp.get().snd(), new HashSet<>());
                migratedTFGss.add(r);
                System.out.println("added " + r.size() + " out of  " + t.nodes_p().count());

           //     t.get().edges().stream().forEach(x -> System.out.println(qualifiedName(x.nodeU()) + "     " + qualifiedName(x.nodeV())));

                //t.nodes_p().map(x -> qualifiedName(x) + "..." + prettyType(x.getType()) + "..." + (t.get().inDegree(x) + t.get().outDegree(x))).forEach(System.out::println);

                System.out.println("********************************************");
                for(Refactorable x : r) {
                    System.out.println(qualifiedName(x.getId()));
                    System.out.println(Visualizer.prettyEdits(x.getEditInstructionsList()));
                    System.out.println("--");
                }
                System.out.println("********************************************");
            }
        }

        List<Refactorable> migrate = migratedTFGss.stream()//.filter(x -> x.stream().noneMatch(z -> z.getEditInstructionsList().contains(DoNotMigrate)))
                .flatMap(rt -> rt.stream()).collect(toList());

        System.out.println("PASSED " + migrate.size());

//        for (Refactorable x : migrate) {
//            System.out.println(Visualizer.qualifiedName(x.getId()));
//            System.out.println(Visualizer.prettyEdits(x.getEditInstructionsList()));
//            System.out.println("**");
//        }

        migrate.forEach(m -> RWProtos.write(m,"Ref"));

    }
}

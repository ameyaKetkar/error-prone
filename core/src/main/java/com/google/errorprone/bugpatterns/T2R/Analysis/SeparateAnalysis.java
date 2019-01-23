package com.google.errorprone.bugpatterns.T2R.Analysis;

import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.induceDisconnectedSubgraphs;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.paramArgRelation;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.propogateAffectedByHierarchy;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.removeNonPvt;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.removeParentParam;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.resolveInferred;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.resolveSuperClause;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.simplifyMethods;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.getRefactorableTFG1;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.matchProgram;
import static com.google.errorprone.bugpatterns.T2R.Analysis.PreConditions.METHOD_FOUND;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.emptyTFG;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.induceGraph;
import static com.google.errorprone.bugpatterns.T2R.common.Visualizer.prettyType;
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

/**
 * Created by ameya on 1/4/19.
 */
public class SeparateAnalysis {


    public static void main(String a[]){
        final List<TFG> tfgss = RWProtos.read("TFG");
        TypeFactGraph<Identification> gTFG = tfgss.stream().map(TypeFactGraph::of).reduce(Analysis::merge).orElse(emptyTFG());
        TypeFactGraph<Identification> globalTFG =
                removeParentParam(propogateAffectedByHierarchy(removeNonPvt(resolveSuperClause(paramArgRelation(resolveInferred(simplifyMethods(gTFG)))))));

        List<TypeFactGraph<Identification>> globalTFGs = induceDisconnectedSubgraphs(globalTFG).stream()
                .filter(x->matchProgram(x, Migrate.mapping).isPresent())
                .map(x -> induceGraph(globalTFG, x)).filter(x -> !x.isEmpty())
                .filter(METHOD_FOUND)
                .collect(toList());
        System.out.println("TOTAL SubTFGs = " + globalTFGs.size());
        List<Set<Refactorable>> migratedTFGss = new ArrayList<>();
        for(int i = 0 ; i < globalTFGs.size(); i ++ ){
            TypeFactGraph<Identification> t = globalTFGs.get(i);
            Optional<Pair<Identification, Program>>  pp = matchProgram(t,Migrate.mapping);
            if(pp.isPresent()){
                System.out.println(prettyType(pp.get().snd().getFrom()));
                Set<Refactorable> r = getRefactorableTFG1(t, new HashSet<>());
                migratedTFGss.add(r);
            }
        }
        List<Refactorable> migrate = migratedTFGss.stream()
                .filter(x -> x.stream().noneMatch(z -> z.getEditInstructionsList().stream().anyMatch(c -> c.hasCmd() && c.getCmd().getNumber()==1)))
                .flatMap(rt -> rt.stream()).collect(toList());
        System.out.println("Number of refactorables: " + migrate.size());
        migrate.forEach(m -> RWProtos.write(m,"Ref"));
    }
}

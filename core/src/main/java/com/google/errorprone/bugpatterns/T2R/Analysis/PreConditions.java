package com.google.errorprone.bugpatterns.T2R.Analysis;

import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.DECLARED_IN;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.NOT_PRIVATE;

import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class PreConditions {

   public static Predicate<Set<Refactorable>> DO_NOT_MIGRATE = tfg -> tfg.stream().noneMatch(y -> y.getEditInstructionsList().stream().anyMatch(e -> e.hasCmd() && e.getCmd().getNumber() == 1));
   public static Predicate<TypeFactGraph<Identification>> EVERYTHING_PRIVATE = x -> x.nodes_p().noneMatch(y -> y.getName().equals(NOT_PRIVATE));
   public static Predicate<TypeFactGraph<Identification>> ALL_MTHD_SIMPLIFIED = x ->
           x.get().edges().stream().map(e -> x.get().edgeValue(e.nodeU(),e.nodeV()).get())
           .noneMatch(e -> e.contains(DECLARED_IN));

   public static Predicate<Map.Entry<TypeFactGraph<Identification>, Optional<Pair<Identification, Program>>>> MAPPING_FOUND = x -> x.getValue().isPresent();

}

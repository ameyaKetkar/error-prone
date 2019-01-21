package com.google.errorprone.bugpatterns.T2R.Analysis;

import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.METHOD;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.typeInfoMatch;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Migrate.PDbl;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Migrate.PInt;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Migrate.PLng;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Migrate.WDbl;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Migrate.WInt;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Migrate.WLng;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.NOT_PRIVATE;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.INFERRED_;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.RECEIVER;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.getSuccessorWithEdge;

import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PreConditions {

   public static Predicate<Set<Refactorable>> DO_NOT_MIGRATE = tfg -> tfg.stream().noneMatch(y -> y.getEditInstructionsList().stream().anyMatch(e -> e.hasCmd() && e.getCmd().getNumber() == 1));
   //x -> x.stream().noneMatch(z -> z.getEditInstructionsList().stream().anyMatch(c -> c.hasCmd() && c.getCmd().getNumber()==1))
   public static Predicate<TypeFactGraph<Identification>> EVERYTHING_PRIVATE = x -> x.nodes_p().noneMatch(y -> y.getName().equals(NOT_PRIVATE));

   public static Predicate<TypeFactGraph<Identification>> METHOD_FOUND = x ->
           x.nodes_p().filter(m -> m.getKind().equals(INFERRED_+METHOD))
                   .allMatch(m ->  getSuccessorWithEdge(x,m,RECEIVER).isPresent()
                      || m.getType().getMthdSign().getParamList().stream()
                           .allMatch(mm -> Stream.of(WInt,PInt,WDbl,PDbl,WLng,PLng)
                                   .anyMatch(p ->typeInfoMatch(p,mm))));




}

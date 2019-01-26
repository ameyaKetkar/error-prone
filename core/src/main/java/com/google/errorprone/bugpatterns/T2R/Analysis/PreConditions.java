package com.google.errorprone.bugpatterns.T2R.Analysis;

import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.METHOD;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.matchProgram;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.ASSIGNED_AS;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.METHOD_HIERARCHY;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.NOT_PRIVATE;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.PASSED_AS_ARG_TO;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.INFERRED_;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.getSuccessorsWithEdges;
import static com.google.errorprone.bugpatterns.T2R.common.Util.L;

import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;

import java.util.Set;
import java.util.function.Predicate;

public class PreConditions {

    public static Predicate<Set<Refactorable>> DO_NOT_MIGRATE = tfg -> tfg.stream()
           .noneMatch(y -> y.getEditInstructionsList().stream()
                   .anyMatch(e -> e.hasCmd() && e.getCmd().getNumber() == 1));


    public static Predicate<TypeFactGraph<Identification>> EVERYTHING_PRIVATE = x -> x.nodes_p().noneMatch(y -> y.getName().equals(NOT_PRIVATE));

    public static Predicate<TypeFactGraph<Identification>> NO_INFERRED_PASSED_AS_ARG = tfg ->
            tfg.nodes_p().filter(x -> matchProgram(x,Migrate.mapping).isPresent())
                    .noneMatch(x -> {
                        return getSuccessorsWithEdges(tfg, x, L(PASSED_AS_ARG_TO))
                                .stream().anyMatch(s -> s.getKind().contains(INFERRED_));
                    });

    public static Predicate<TypeFactGraph<Identification>> NO_INFERRED_ASSIGNMENT = tfg ->
            tfg.nodes_p().filter(x -> matchProgram(x,Migrate.mapping).isPresent())
                    .noneMatch(x -> {
                        return getSuccessorsWithEdges(tfg, x, L(ASSIGNED_AS))
                                .stream().anyMatch(s -> s.getKind().contains(INFERRED_) && !s.getKind().contains(INFERRED_ + "CONSTRUCTOR"));
                    });

    public static Predicate<TypeFactGraph<Identification>> NO_INFERRED_METHOD_IN_HIERARCHY = tfg ->
            tfg.nodes_p().filter(x -> matchProgram(x,Migrate.mapping).isPresent())
                    .filter(x -> x.getKind().equals(METHOD))
                    .noneMatch(x -> {
                        return getSuccessorsWithEdges(tfg, x, L(METHOD_HIERARCHY))
                                .stream().anyMatch(s -> s.getKind().contains(INFERRED_) && !s.getKind().contains(INFERRED_ + "CONSTRUCTOR"));
                    });


}

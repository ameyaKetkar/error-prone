package com.google.errorprone.bugpatterns.T2R.Collect;


import com.sun.source.tree.CompilationUnitTree;

//
//@AutoService(BugChecker.class)
//@BugPattern(
//        name = "Collect",
//        category = JDK,
//        summary = "Collection phase for type migration!!!",
//        severity = SUGGESTION,
//        linkType = CUSTOM,
//        link = "example.com/bugpattern/Collect"
//)


public class Collect {//extends BugChecker implements BugChecker.CompilationUnitTreeMatcher {


    // TODO : anonymous class
    // TODO : New Class
    // TODO : why do some callsites not migrate?
    // TODO : two parameters of same method match on different types

//    @Override
//    public Description matchCompilationUnit(CompilationUnitTree cu, VisitorState s) {
//        String fileName = getFileName(cu);
//        System.out.println("******" + fileName + "******");
//        Refactorables = cus.size() > 0 && allRefactorables.size()>0
//                ? cuPresent(getFileName(cu)) ? allRefactorables : new HashSet<>()
//                : getRefactorablesForCu(cu,s);
//
//        System.out.println(Refactorables.size());
//
//            if (Refactorables.size() > 0) {
//                SuggestedFix.Builder fixes = FIX_CREATOR(s).scan(cu.getTypeDecls(), getPackageId(cu));
//                if (fixes != null && !fixes.isEmpty()) {
//                    return describeMatch(cu, fixes.build());
//                }
//            }
//        return describeMatch(cu);
//    }
//
//
//    private static Set<Refactorable> getRefactorablesForCu(CompilationUnitTree cu, VisitorState s){
//
//        System.out.println("Collecting TFGs");
//        final TypeFactGraph<Identification> tfggg = TFG_CREATOR(s).scan(cu.getTypeDecls(), getPackageId(cu));
//
//        final TypeFactGraph<Identification> tfg = resolveSuperClause(paramArgRelation(
//                resolveInferred(simplifyMethods(tfggg))));
//
//        System.out.println("TFG Created");
//
//        Set<TypeFactGraph<Identification>> relevantSubTFGs = induceConnectedSubgraphs(tfg).stream()
//                .map(x -> TypeFactGraph.of(Graphs.inducedSubgraph(tfg.get(), x)))
//                .filter(e -> matchProgram(e, Migrate.mapping).isPresent()).collect(toSet());
//
//
//        Map<Boolean, List<TypeFactGraph<Identification>>> pvtSubTFGs = relevantSubTFGs.stream()
//                .collect(partitioningBy(EVERYTHING_PRIVATE));
//
//        RWProtos.write(pvtSubTFGs.get(Boolean.FALSE).stream().map(TypeFactGraph::asTFG).collect(toList()), "TFG");
//        if(pvtSubTFGs.get(Boolean.FALSE).size() > 0){
//            RWProtos.write(CUs.newBuilder().setCu(getFileName(cu)).build(),"CU");
//        }
//
//        List<TypeFactGraph<Identification>> pvtTfgs = pvtSubTFGs.get(TRUE);
//        System.out.println("THE FOLLOWING PASSED");
//
//        List<Pair<TypeFactGraph<Identification>, Set<Refactorable>>> migratedTFGs = pvtTfgs.stream()
//                .map(t -> P(t, matchProgram(t, Migrate.mapping)))
//                .filter(p -> p.snd().isPresent()).map(p -> P(p.fst(), p.snd().orElseThrow(() -> new RuntimeException(p.fst().asTFG().toString()))))
//                .map(e -> P(e.fst(), getRefactorableTFG(e.fst(),e.snd().snd(), new HashSet<>()))).collect(toList());
//
//        Map<Boolean, List<Pair<TypeFactGraph<Identification>, Set<Refactorable>>>>
//                passedPreConditions = migratedTFGs.stream().collect(partitioningBy(e -> DO_NOT_MIGRATE.test(e.snd())));
//
//        if (cus.size() == 0 ) {
//            passedPreConditions.get(Boolean.FALSE).stream().map(t -> t.fst().asTFG()).forEach(x -> RWProtos.write(x, "TFG"));
//            if (passedPreConditions.get(Boolean.FALSE).size() > 0) {
//                RWProtos.write(CUs.newBuilder().setCu(getFileName(cu)).build(), "CU");
//            }
//        }
//
//        return passedPreConditions.get(TRUE).parallelStream()
//                .filter(x -> METHOD_FOUND.test(x.fst()))
//                .flatMap(x -> x.snd().stream())
//                .collect(toSet());
//    }





    private static String getFileName(CompilationUnitTree cu) {
        return cu.getSourceFile().getName().substring(cu.getSourceFile().getName().lastIndexOf("/") + 1, cu.getSourceFile().getName().lastIndexOf("."));
    }
}




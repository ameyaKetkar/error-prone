package com.google.errorprone.bugpatterns.T2R.Analysis;

import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.FIELD;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.LOCAL_VARIABLE;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.METHOD;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.PARAMETER;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.getIdType;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.isVarKind;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.typeInfoMatch;
import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.typesMatch;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.OF_TYPE;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.OVERRIDEN_BY;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.INFERRED_;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.SUPER_CLAUSE;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.RECEIVER;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.getSuccessorWithEdge;
import static com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph.getSuccessorsWithEdge;
import static com.google.errorprone.bugpatterns.T2R.common.Util.L;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static com.sun.source.tree.Tree.Kind.LAMBDA_EXPRESSION;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.errorprone.bugpatterns.T2R.common.Models.CUsOuterClass.CUs;
import com.google.errorprone.bugpatterns.T2R.common.Models.EditInstructionOuterClass.EditCmd;
import com.google.errorprone.bugpatterns.T2R.common.Models.EditInstructionOuterClass.EditInstruction;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program.ChangeInstruction;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program.MethodChange;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.FilteredType;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeInfo;
import com.google.errorprone.bugpatterns.T2R.common.RWProtos;
import com.google.errorprone.bugpatterns.T2R.common.TypeFactGraph;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by ameya on 12/18/18.
 */
public class GenerateRefactorables {

    public static List<CUs> cus =  RWProtos.readCUs();


    public static Set<Refactorable> allRefactorables= RWProtos.readRef().stream().collect(toSet());
    public static Set<Refactorable> Refactorables;

    public static boolean cuPresent(String fileName){
        System.out.println(cus.size());
        if(cus.size() > 0)
            return cus.stream().anyMatch(cu -> cu.getCu().equals(fileName));
        return true;
    }

    public static Refactorable R(Identification id, List<EditInstruction> ei){
        return Refactorable.newBuilder().setId(id).addAllEditInstructions(ei).build();
    }

    //final Identification id,
    public static Set<Refactorable> getRefactorableTFG(final TypeFactGraph<Identification> tfg, Program p, Set<Identification> visitedNodes){

        Set<Refactorable> acc = new HashSet<>();
        List<Identification> elemsOfT = tfg.nodes_p().filter(x -> typesMatch(x.getType(), p.getFrom())).collect(toList());
        visitedNodes.addAll(elemsOfT);

        List<Refactorable> trsnfrmdElemsOfT = elemsOfT.stream().map(x -> applyProgram(x,p,tfg)).collect(toList());
        acc.addAll(trsnfrmdElemsOfT);

        List<Identification> cs = tfg.nodes_p().filter(x -> isCallSite(x,p,tfg) || getSuccessorWithEdge(tfg, x, OVERRIDEN_BY).isPresent())
                .collect(toList());

        Set<Refactorable> trnsfrmdRef = cs.stream().map(x -> applyProgram(x,p,tfg)).collect(toSet());
        acc.addAll(trnsfrmdRef);
        visitedNodes.addAll(cs);

        for(Identification c : cs){
            List<Program.ChangeType> typeChanges = getChgInstrMthd(c, p).orElse(new ArrayList<>()).stream().filter(ChangeInstruction::hasChngType)
                    .map(ChangeInstruction::getChngType).collect(toList());
            for (Program.ChangeType typeChng : typeChanges) {
                Set<Identification> succ = typeChng.hasNavigateTo()
                        ? getSuccessorsWithEdge(tfg, c, typeChng.getNavigateTo()).stream().collect(Collectors.toSet())
                        : tfg.get().successors(c).stream().filter(s -> !visitedNodes.contains(s)).collect(Collectors.toSet());
                Set<Refactorable> rs = succ.stream().map(x -> applyProgram(x, typeChng.getValue(), tfg)).collect(toSet());
                acc.addAll(rs);
                visitedNodes.addAll(succ);
            }
        }
        return acc;
    }


    private static Optional<List<ChangeInstruction>> getChgInstrMthd(Identification id, Program p) {
        return p.getMethodChangeList().stream().filter(m -> match(P(m.getId(), id)))
                .findFirst().map(MethodChange::getChangeList);
    }

    static EditInstruction IDENTITIY = EditInstruction.newBuilder().setCmd(EditCmd.Identity).build();

    private static Refactorable applyProgram(Identification id, Program p, TypeFactGraph<Identification> tfg){
        if(isCallSite(id,p, tfg) || getSuccessorWithEdge(tfg,id,OVERRIDEN_BY).isPresent()){
            List<ChangeInstruction> changes = getChgInstrMthd(id, p)
                    .orElse(L(ChangeInstruction.newBuilder().setEditInstr(IDENTITIY).build()));
            List<EditInstruction> edits = changes.stream().map(GenerateRefactorables::getEdit).filter(Optional::isPresent).map(Optional::get).collect(toList());
            if(getSuccessorWithEdge(tfg,id,OVERRIDEN_BY).isPresent()){
                    changes.stream().filter(c->c.hasChngType() && !c.getChngType().hasNavigateTo()).findAny().ifPresent(c->edits.add(c.getChngType().getValue().getDeclarations()));
            }
            return R(id,edits);
        }
        else if(isDeclarationKind(id)) {
//            if(tfg.get().successors(id).stream().anyMatch(s -> tfg.get().edgeValue(s,id).get().equals(AFFECTED_BY_HIERARCHY))) {
//                return R(id, L(IDENTITIY));
//            }
            return (typesMatch(id.getType(),p.getFrom())) ? getRefactorableForDecl(id,p) : R(id, L(IDENTITIY));
        }
        else{
            if(id.getKind().contains(INFERRED_)){
                return R(id, p.getNonEditableExprList());
            }else if(id.getKind().equals(LAMBDA_EXPRESSION.toString())){
                return R(id,L(p.getLambda()));
            }
            return R(id, L(IDENTITIY));
        }
    }

    private static Refactorable getRefactorableForDecl(Identification id, Program p) {
        FilteredType t = p.getFrom().getOf();
        int i = 0;
        List<Integer> preserveParams = new ArrayList<>();
        for(TypeInfo tp : t.getTypeParameterList()){
            if(tp.hasAnyType()){
                preserveParams.add(i);
            }
            i += 1;
        }
        TypeInfo id_type = getIdType(id.getType());
        if(preserveParams.size() > 0){
            List<TypeInfo> x = preserveParams.stream().map(pr -> id_type.getOf().getTypeParameter(pr)).collect(toList());
            if(p.getDeclarations().hasEditType()){
                TypeInfo et = TypeInfo.newBuilder().setOf(p.getDeclarations().getEditType().getOf().toBuilder().addAllTypeParameter(x)).build();
                return R(id, L(p.getDeclarations().toBuilder().setEditType(et).build()));
            }
        }
        return R(id, L(p.getDeclarations()));
    }

    private static Optional<EditInstruction> getEdit(ChangeInstruction c){
        if(c.hasEditInstr())
            return Optional.of(c.getEditInstr());
        else if(c.hasCmd())
            return Optional.of(EditInstruction.newBuilder().setCmd(c.getCmd()).build());
        else return Optional.empty();
    }


    private static boolean isCallSite(Identification id, Program p, TypeFactGraph<Identification> tfg) {
        return id.getKind().equals(INFERRED_ + METHOD)
                && (typesMatch(id.getOwner().getType(), p.getFrom())
                || getSuccessorsWithEdge(tfg,id,RECEIVER).stream().anyMatch(rec -> getSuccessorsWithEdge(tfg,rec,OF_TYPE).size() > 0));
    }

    private static boolean isDeclarationKind(Identification id) {
        return Stream.of(METHOD,LOCAL_VARIABLE,PARAMETER,FIELD,"CLASS", SUPER_CLAUSE).anyMatch(k -> k.equals(id.getKind()));
    }

    public static List<Program> allProgramsT(List<Program> programs){
        List<Program> ps = new ArrayList<>(programs);
        for(Program p : programs){
            List<Program> programsNeeded = p.getMethodChangeList().stream().flatMap(x -> x.getChangeList().stream())
                    .filter(c -> c.hasChngType()).map(ct -> ct.getChngType().getValue()).collect(toList());
            ps.addAll(programsNeeded);
            ps.addAll(allProgramsT(programsNeeded));
        }
        return ps;
    }

    public static Optional<Pair<Identification,Program>> matchProgram(TypeFactGraph<Identification> t, List<Program> ps){
        //Set<Identification> vars = t.nodes_p().filter(x -> isVarKind(x) || isSuperClause(x)).collect(toSet());
        for(Identification v: t.get().nodes()) {
            for (Program p : ps) {
                if (typeInfoMatch(p.getFrom(), getIdType(v.getType()))){
                    return Optional.of(P(v,p));
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<Pair<Identification,Program>> matchProgram1(TypeFactGraph<Identification> t, List<Program> ps){
        Set<Identification> vars = t.nodes_p().filter(x -> isVarKind(x)).collect(toSet());
        for(Identification v: vars) {
            for (Program p : ps) {
                if (typeInfoMatch(p.getFrom(), v.getType().getTypeSign())){ //|| getSuccessorsWithEdge(t,v ,OF_TYPE).size() > 0) {
                    return Optional.of(P(v,p));
                }
            }
        }
        return Optional.empty();
    }

    private static boolean match(Pair<Identification,Identification> p){
        final Identification id = p.snd();
        final Identification ref = p.fst();
        boolean result = true;
        if(ref.hasName())
            result = ref.getName().equals(id.getName());
        if(ref.hasType())
            result = result && typesMatch(ref.getType(),id.getType());
        return result;
    }



}

package com.google.errorprone.bugpatterns.T2R.Refactor;


import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.typesMatch;
import static com.google.errorprone.bugpatterns.T2R.Analysis.GenerateRefactorables.Refactorables;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2State2U.TREE2STATE;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static java.util.stream.Collectors.toList;

import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.T2R.common.Models.EditInstructionOuterClass.EditInstruction;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeInfo;
import com.google.errorprone.bugpatterns.T2R.common.State;
import com.google.errorprone.bugpatterns.T2R.common.Tree2State2U;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFix.Builder;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by ameya on 12/16/18.
 */
public class GenerateFix {


    private static final SuggestedFix.Builder empty = SuggestedFix.builder();


    public static SuggestedFix.Builder nullHandleReduce(SuggestedFix.Builder u1, SuggestedFix.Builder u2){

        if(null == u1 && null == u2){
            return SuggestedFix.builder();
        }

        else if((null != u1 && null != u2) && (u1.isEmpty() && u2.isEmpty())){
            return SuggestedFix.builder();
        }

        else if(u1 == null || u1.isEmpty()){
            return u2;
        }
        else if(u2 == null || u2.isEmpty()){
            return u1;
        }
        else
            return u1.merge(u2);
    }

    private static final BinaryOperator<Builder> combiner = GenerateFix::nullHandleReduce;
    private static final TreeScanner<Builder, Pair<VisitorState, State>> FIX_GEN  =
            new TreeScanner<Builder,Pair<VisitorState,State>>(){

        private SuggestedFix.Builder getFix(Pair<VisitorState, State> s){
            return getRef(s.snd().getRootID()).map(r -> getSuggestionFix(s.snd().getRoot(),s.fst(),r.getEditInstructionsList())).orElse(SuggestedFix.builder());
        }

        private SuggestedFix.Builder getFix1(Pair<VisitorState, State> s){
            return getRefCornerCase(s.snd().getRootID()).map(r -> getSuggestionFix(s.snd().getRoot(),s.fst(),r.getEditInstructionsList())).orElse(SuggestedFix.builder());
        }

        private Optional<Refactorable> getRef(Identification i){
            return Refactorables.parallelStream().filter(x -> x.getId().equals(i)).findAny();
        }

        private Optional<Refactorable> getRefCornerCase(Identification i){
            BiPredicate<Identification,Identification> match = (a,b) -> a.getName().equals(b.getName())
                    && a.getKind().equals(b.getKind()) && a.getOwner().equals(b.getOwner())
                    && typesMatch(a.getType(),b.getType());
            return Refactorables.parallelStream()
                    .filter(x -> match.test(x.getId(),i)).findAny();
        }

        @Override
        public SuggestedFix.Builder visitVariable(VariableTree v, Pair<VisitorState, State> s){
            return combiner.apply(getFix(s), manageTypeDependent(s));
        }

        @Override
        public SuggestedFix.Builder visitMethod(MethodTree m, Pair<VisitorState, State> s){
            return combiner.apply(getFix(s), manageTypeDependent(s));
        }

        @Override
        public SuggestedFix.Builder visitParameterizedType(ParameterizedTypeTree i, Pair<VisitorState, State> s) {
            return combiner.apply(getFix(s), manageTypeDependent(s));
        }

        @Override
        public SuggestedFix.Builder visitMethodInvocation(MethodInvocationTree root, Pair<VisitorState, State> s){
            Builder fix = getFix(s);
            if(fix.isEmpty()){
                fix = getFix1(s);
            }
            return combiner.apply(fix, manageTypeDependent(s));
        }

        @Override
        public SuggestedFix.Builder visitMemberSelect (MemberSelectTree m, Pair<VisitorState, State> s){
            return combiner.apply(getFix(s),manageTypeDependent(s));
        }

        @Override
        public SuggestedFix.Builder visitNewClass (NewClassTree m, Pair<VisitorState, State> s){
            if(m.getClassBody()!=null){
                return combiner.apply(FIX_GEN.visitClass(m.getClassBody(),s), manageTypeDependent(s));
            }
            return combiner.apply(getFix(s),manageTypeDependent(s));
        }

        @Override
        public SuggestedFix.Builder visitIdentifier (IdentifierTree m, Pair<VisitorState, State> s){
            return combiner.apply(getFix(s), manageTypeDependent(s));
        }

        @Override
        public SuggestedFix.Builder visitClass(ClassTree root, Pair<VisitorState, State> s){
            SuggestedFix.Builder acc = empty;
            for (Pair<State, String> t : s.snd().getTypeDependents()) {
                final SuggestedFix.Builder dependent = FIX_GEN.scan(t.fst().getRoot(), P(s.fst(), t.fst()));
                if (dependent != null) {
                    List<State> ovrMthd = root.getMembers().stream().filter(x -> x.getKind().equals(Kind.METHOD))
                            .map(x -> (MethodTree) x)
                            .filter(x -> ASTHelpers.findSuperMethodInType(ASTHelpers.getSymbol(x), ASTHelpers.getType(t.fst().getRoot()), s.fst().getTypes()) != null)
                            .map(x -> TREE2STATE.scan(x, s.snd().getRootID())).collect(toList());
                    for(State om : ovrMthd){
                        acc = combiner.apply(acc, getFix(P(s.fst(),om)));
                        acc= combiner.apply(acc, manageTypeDependent(P(s.fst(),om)));
                    }
                }
            }
            return combiner.apply(acc, manageTypeDependent(s));
        }

        @Override
        public SuggestedFix.Builder visitBinary(BinaryTree m, Pair<VisitorState, State> s) {
            return  manageTypeDependent(s);
        }

        @Override
        public SuggestedFix.Builder reduce(SuggestedFix.Builder u1, SuggestedFix.Builder u2){
            return combiner.apply(u1,u2);
        }

        private SuggestedFix.Builder manageTypeDependent(Pair<VisitorState, State> s) {
            SuggestedFix.Builder acc = SuggestedFix.builder();
            for (Pair<State, String> t : s.snd().getTypeDependents()) {
                if (t.fst() != null && t.fst().getRoot() != null) {
                    final SuggestedFix.Builder dependent =
                    FIX_GEN.scan(t.fst().getRoot(), P(s.fst(),t.fst()));
                    if (dependent != null && !dependent.isEmpty())
                        acc = combiner.apply(acc, dependent);
                }
            }
            return acc;
        }

        private Builder getSuggestionFix(Tree t, VisitorState s, List<EditInstruction> instrs) {
            Builder fix = SuggestedFix.builder();
            for(EditInstruction instr : instrs) {
                if (instr.hasEditType()) {
                    if (t.getKind().equals(Kind.VARIABLE)) {
                        VariableTree v = (VariableTree) t;
                        fix.replace(v.getType(), getTypeString(instr.getEditType()));
                    } else if (t.getKind().equals(Kind.METHOD)) {
                        MethodTree m = (MethodTree) t;
                        fix.replace(m.getReturnType(), getTypeString(instr.getEditType()));
                    }
                    else if (t.getKind().equals(Kind.PARAMETERIZED_TYPE)) {
                        ParameterizedTypeTree m = (ParameterizedTypeTree) t;
                        fix.replace(m, getTypeString(instr.getEditType()));
                    }
                    if(!instr.getEditType().getOf().getInterfaceName().isEmpty()
                            && instr.getEditType().getOf().getInterfaceName().contains(".")) {
                        fix.addImport(instr.getEditType().getOf().getInterfaceName());
                    }
                }
                else if (instr.hasEditName()) {
                    if (t.getKind().equals(Kind.METHOD_INVOCATION)) {
                        MethodInvocationTree mi = (MethodInvocationTree) t;
                        fix.merge(SuggestedFixes.renameMethodInvocation(mi, instr.getEditName(), s));
                    }
                    else if (t.getKind().equals(Kind.METHOD)) {
                        MethodTree mi = (MethodTree) t;
                        fix.merge(SuggestedFixes.renameMethod(mi, instr.getEditName(), s));
                    }
                }
                else if(instr.hasCmd()){
                    if(instr.getCmd().getNumber() == 4){
                       if(t.getKind().equals(Kind.METHOD_INVOCATION)){
                           MethodInvocationTree mi = (MethodInvocationTree) t;
                           int miEnds = s.getEndPosition(mi);
                           MemberSelectTree ms = (MemberSelectTree) mi.getMethodSelect();
                           int recEnds = s.getEndPosition(ms.getExpression());
                           fix.replace(recEnds, miEnds, "");
                       }
                    }
                }
            }

            return fix;
        }

    };


    private static String getTypeString(TypeInfo ti){
        if(ti.hasOf()) {
            String z = ti.getOf().getInterfaceName();
            String i = z.substring(z.lastIndexOf(".") + 1);
            String tp = ti.getOf().getTypeParameterCount() > 0 ? "<" + ti.getOf().getTypeParameterList().stream()
                    .map(x -> getTypeString(x))
                    .collect(Collectors.joining(",")) + ">" : "";
            return i + tp;
        }
        return ti.getAnyType();
    }


    private static final Function<Pair<VisitorState,State>, Builder> getTranslator
            = x -> FIX_GEN.scan(x.snd().getRoot(),P(x.fst(),x.snd()));

    public static final Tree2State2U<Builder> FIX_CREATOR (VisitorState s){
        return new Tree2State2U<>(empty,combiner, getTranslator, s);
    }


}

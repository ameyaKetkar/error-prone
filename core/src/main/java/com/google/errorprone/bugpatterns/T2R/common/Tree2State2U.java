package com.google.errorprone.bugpatterns.T2R.common;

import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.ID;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.TREE2ID;
import static com.google.errorprone.bugpatterns.T2R.common.Tree2Id.getIdFromSymbol;
import static com.google.errorprone.bugpatterns.T2R.common.Util.L;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static com.google.errorprone.bugpatterns.T2R.common.Util.nullHandleReduce;
import static java.util.stream.Collectors.toList;

import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.lang.model.element.ElementKind;


public class Tree2State2U<U> extends TreeScanner<U, Identification> {

    public static final String ASSIGNMENT = "ASSIGNMENT";
    public static final String RETURNS = "RETURNS";
    public static final String ARGUMENT = "ARGUMENT";
    public static final String RECEIVER = "RECEIVER";
    public static final String IMPLEMENTS = "IMPLEMENTS";
    public static final String EXTENDS = "EXTENDS";
    public static final String INITIALIZED_AS = "INITIALIZED_AS";
    public static final String PARAM = "PARAM";
    public static final String BIN_OP = "BIN_OP";


    public static final String THEN_STMT ="THEN_STATEMENT";
    public static final String ELSE_STMT ="ELSE_STATEMENT";
    public static final String FOR_LOOP = "FOR_LOOP";


    private U empty;
    private BinaryOperator<U> combiner;
    private Function<Pair<VisitorState, State>, U> translator;
    private VisitorState vs ;


    public Tree2State2U(U empty, BinaryOperator<U> combiner, Function<Pair<VisitorState, State>, U> translator, VisitorState vs) {
        this.empty = empty;
        this.combiner = combiner;
        this.translator = translator;
        this.vs = vs;
    }

    @Override
    public U visitVariable(VariableTree v, Identification s){
        return translator.apply(P(vs,TREE2STATE.scan(v,s)));
    }

    @Override
    public U visitMethod(MethodTree m, Identification s) {
        final State mthd = TREE2STATE.scan(m,s);
        final U mth = translator.apply(P(vs,mthd));
        final U body = (new Tree2State2U<>(empty, combiner, translator,vs)).scan(m.getBody(),mthd.getRootID());
        return reduce(mth,body);
    }

    @Override
    public U visitAssignment(AssignmentTree a, Identification s) {
        return translator.apply(P(vs,TREE2STATE.scan(a,s)));
    }
    @Override
    public U visitNewClass(NewClassTree nc, Identification s){
        return translator.apply(P(vs,TREE2STATE.scan(nc,s)));
    }

    @Override
    public U visitIdentifier(IdentifierTree id, Identification s) {
        return translator.apply(P(vs,TREE2STATE.scan(id,s)));
    }

//    @Override
//    public U visitIf(IfTree e, Identification p) {
//        final U cnd = (new Tree2State2U<>(empty, combiner, translator,vs)).scan(e.getCondition(), p);
//        final U ifBranch = (new Tree2State2U<>(empty, combiner, translator,vs)).scan(e.getThenStatement()
//                ,ID(null, THEN_STMT, null, p));
//        final U  thenBranch = (new Tree2State2U<>(empty, combiner, translator,vs)).scan(e.getElseStatement()
//                , ID(null, ELSE_STMT, null, p));
//        return Stream.of(cnd,ifBranch,thenBranch).reduce(empty,combiner);
//    }
        @Override
        public U visitIf(IfTree e, Identification p) {
            final U cnd = (new Tree2State2U<>(empty, combiner, translator,vs)).scan(e.getCondition(), p);
            final U ifBranch = (new Tree2State2U<>(empty, combiner, translator,vs)).scan(e.getThenStatement()
                    ,ID(null, THEN_STMT, null, p));
            final U  thenBranch = (new Tree2State2U<>(empty, combiner, translator,vs)).scan(e.getElseStatement()
                    , ID(null, ELSE_STMT, null, p));
            return Stream.of(cnd,ifBranch,thenBranch).reduce(empty,combiner);
        }
//    @Override
//    public U visitForLoop(ForLoopTree e, Identification p) {
//        return (new Tree2State2U<>(empty, combiner, translator,vs)).scan(e.getStatement()
//                ,ID(null, FOR_LOOP, null, p));
//
//    }

    @Override
    public U visitLambdaExpression (LambdaExpressionTree lmbd, Identification s){
        return translator.apply(P(vs,TREE2STATE.scan(lmbd,s)));
    }

    @Override
    public U visitMethodInvocation(MethodInvocationTree m, Identification s) {
        return translator.apply(P(vs,TREE2STATE.scan(m,s)));
    }

    @Override
    public U visitMemberSelect (MemberSelectTree m, Identification s){
        return translator.apply(P(vs,TREE2STATE.scan(m,s)));
    }

    @Override
    public U visitLiteral (LiteralTree l, Identification s){
        return translator.apply( P(vs,TREE2STATE.scan(l,s)));
    }

    @Override
    public U visitClass(ClassTree c, Identification s){
        final State cls = TREE2STATE.scan(c,s);
        final U cl = translator.apply(P(vs,cls));
        final U body = (new Tree2State2U<>(empty, combiner, translator,vs)).scan(c.getMembers(), cls.getRootID());
        return combiner.apply(body,cl);
    }



    @Override
    public U reduce(U u1, U u2){
        return combiner.apply(u1,u2);

    }


    public static final TreeScanner<State,Identification> TREE2STATE = new TreeScanner<State, Identification>() {
        @Override
        public State visitVariable(VariableTree root,Identification s) {
            // Foo f = new Foo();
            // Foo ff = getFooInstance();
            // Foo fff = ff
            // ff = f


            final Identification rootID = TREE2ID.scan(root, s);
            List<Pair<State, String>> typeDependents = new ArrayList<>();
            if (null != root.getInitializer() ) {
                State init = TREE2STATE.scan(root.getInitializer(), rootID);
                typeDependents.add(P(init, INITIALIZED_AS));
            }
            return new State(root, rootID, typeDependents);
        }


        @Override
        public State visitMethod(MethodTree root, Identification s) {
            final Identification rootID = TREE2ID.scan(root, s);
            List<Pair<State, String>> typeDependents = new ArrayList<>();
            if(null != root.getParameters()) {
                for (int i = 0; i < root.getParameters().size(); i++) {
                    State param = TREE2STATE.scan(root.getParameters().get(i), rootID);
                    typeDependents.add(P(param, PARAM + i));
                }
            }
            final TreeScanner<List<Tree>, Identification> RETURN_ID_GEN =
                    new TreeScanner<List<Tree>,Identification>() {
                        @Override
                        public List<Tree> visitReturn(ReturnTree r, Identification p){
                            return L(r.getExpression());
                        }
                        @Override
                        public List<Tree> reduce(List<Tree> a, List<Tree> b){
                            return nullHandleReduce(a,b
                                    ,(l1,l2) -> Stream.concat(l1.stream(),l2.stream()).collect(toList())
                                    ,new ArrayList<>());
                        }
                    };
            if(root.getBody() != null) {
                List<Tree> returnStmts = RETURN_ID_GEN.scan(root.getBody().getStatements(), rootID);
                if (returnStmts != null) {
                    typeDependents.addAll(returnStmts
                            .stream()//.filter(t -> t!=null && rootID!= null)
                            .map(t -> TREE2STATE.scan(t, rootID))
                            .map(t -> P(t, RETURNS)).collect(toList()));
                }
            }

            return new State(root, rootID, typeDependents);
        }

        @Override
        public State visitExpressionStatement(ExpressionStatementTree e, Identification p) {
            return TREE2STATE.scan(e.getExpression(), p);
        }

        @Override
        public State visitAssignment(AssignmentTree a, Identification s) {
            final Identification rootID = TREE2ID.scan(a.getVariable(), s);
            final Tree root = a.getVariable();
            List<Pair<State, String>> typeDependents = new ArrayList<>();
            if (null != a.getExpression()) {
                State init = TREE2STATE.scan(a.getExpression(), rootID);
                typeDependents.add(P(init, ASSIGNMENT));
            }
            return new State(root, rootID, typeDependents);
        }

        @Override
        public State visitNewClass(NewClassTree root, Identification s){
            if(root.getClassBody() != null){
                return TREE2STATE.scan(root.getClassBody(),s);
            }
            final Identification rootID = TREE2ID.scan(root, s);
            List<Pair<State, String>> typeDependents = new ArrayList<>();
            if(null != root.getArguments()) {
                for (int i = 0; i < root.getArguments().size(); i++) {
                    State param = TREE2STATE.scan(root.getArguments().get(i),rootID);
                    typeDependents.add(P(param, ARGUMENT + i));
                }
            }
            return new State(root, rootID, typeDependents);
        }

        @Override
        public State visitIdentifier(IdentifierTree root, Identification s) {
            return new State(root, TREE2ID.scan(root,s), new ArrayList<>());
        }


        @Override
        public State visitLambdaExpression (LambdaExpressionTree root,Identification s){
            return new State(root, TREE2ID.scan(root,s), new ArrayList<>());
        }

        @Override
        public State visitMethodInvocation(MethodInvocationTree root, Identification s) {
            final State m = TREE2STATE.scan(root.getMethodSelect(),s);
            List<Pair<State, String>> typeDependents = new ArrayList<>();
            typeDependents.addAll(m.getTypeDependents());
            if(null != root.getArguments()) {
                for (int i = 0; i < root.getArguments().size(); i++) {
                    State param = TREE2STATE.scan(root.getArguments().get(i), m.getRootID());
                    typeDependents.add(P(param, ARGUMENT + i));
                }
            }
            return new State(root, m.getRootID(), typeDependents);
        }

        @Override
        public State visitMemberSelect (MemberSelectTree m,Identification s){

            if(ASTHelpers.getSymbol(m) == null && s.getKind().equals("PACKAGE")){
               return new State(m,s,L());
            }

            if(ASTHelpers.getSymbol(m).getKind().equals(ElementKind.PACKAGE)) {
                final Identification id = getIdFromSymbol(ASTHelpers.getSymbol(m)).toBuilder().setOwner(s).build();
                return new State(m, id, new ArrayList<>());
            }
            final Identification rootID = TREE2ID.scan(m, s);
            final State expr = TREE2STATE.scan(m.getExpression(),s);
            return new State(m, rootID, L(P(expr, RECEIVER)));
        }

        @Override
        public State visitLiteral (LiteralTree m, Identification s){
            return new State(m, TREE2ID.scan(m,s), new ArrayList<>());

        }

        @Override
        public State visitParameterizedType(ParameterizedTypeTree i, Identification s) {
            return new State(i, TREE2ID.scan(i,s), new ArrayList<>());
        }

        @Override
        public State visitBinary(BinaryTree root, Identification s){
            final Identification rootID = TREE2ID.scan(root, s);
            List<Pair<State, String>> typeDependents = new ArrayList<>();
            typeDependents.add(P(TREE2STATE.scan(root.getLeftOperand(),rootID), BIN_OP));
            typeDependents.add(P(TREE2STATE.scan(root.getRightOperand(),rootID), BIN_OP));
            return new State(root, rootID,typeDependents);
        }



        @Override
        public State visitClass(ClassTree root, Identification s){
            final Identification rootID = TREE2ID.scan(root, s);
            List<Pair<State, String>> typeDependents = new ArrayList<>();
            if(null != root.getImplementsClause()) {
                for (Tree t : root.getImplementsClause()) {
                    State impl = TREE2STATE.scan(t, rootID);
                    typeDependents.add(P(impl, IMPLEMENTS));
                }
            }
            if(null != root.getExtendsClause()){
                State extd = TREE2STATE.scan(root.getExtendsClause(),rootID);
                typeDependents.add(P(extd,EXTENDS));
            }
            return new State(root, rootID,typeDependents);

        }
    };
}



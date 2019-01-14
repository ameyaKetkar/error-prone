package com.google.errorprone.bugpatterns.T2R.Collect;

import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;

import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.T2R.Analysis.Migrate;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeInfo;
import com.google.errorprone.matchers.ChildMultiMatcher;
import com.google.errorprone.matchers.ChildMultiMatcher.MatchType;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by ameya on 11/23/18.
 */
public class CollectMatchers {

    public static boolean isSubType (Type t, VisitorState s){
        return Migrate.from.stream().map(HasMatchingType::new)
                .anyMatch(x -> x.isSubType(t,s) &&  !x.isSameType(t,s));
    }

    public static <T extends Tree> Matcher<T> matchesTypeT(List<Program> ps){
        return Matchers.anyOf(ps.stream().map(x -> x.getFrom()).map(CollectMatchers::hasMatchingType).collect(Collectors.toSet()));
    }

    public static <T extends Tree> Matcher<T> matchesTypeT(){
        return Matchers.anyOf(Migrate.from.stream().map(CollectMatchers::hasMatchingType).collect(Collectors.toSet()));
    }


    public static <T extends Tree> Matcher<T> hasMatchingType(TypeInfo typeInfo) {
        return new HasMatchingType<>(typeInfo);
    }

    public static Matcher<ClassTree> superIs(Matcher<Tree> matcher) {
        return new SuperIs(matcher);
    }

    public static final Matcher<NewClassTree> NEW_CLASS_TREE_MATCHER = newClassHasArguments(AT_LEAST_ONE,matchesTypeT());

    public static final Function<TypeInfo, Matcher<Tree>> subTMatcher = t -> new HasMatchingType<Tree>(t){
        @Override
        public boolean matches(Tree t, VisitorState s) {
            if(t == null)
                return false;
            if(isAnyType) return true;
            Type type = ASTHelpers.getResultType((ExpressionTree) t);
            //type = (type instanceof MethodType) ? type.getReturnType() : type;
            return  isSubType(type,s);
        }

    };

    public static <T extends Tree> Matcher<T> matchesTypeSubT(List<Program> ps){
        return Matchers.anyOf(ps.stream().map(Program::getFrom).map(subTMatcher).collect(Collectors.toSet()));
    }

    public static Matcher<NewClassTree> isNewClassInitOf(Matcher<Tree> matcher) {
        return new NewClassInitOfT(matcher);
    }

    public static MultiMatcher<NewClassTree, ExpressionTree> newClassHasArguments(
            MatchType matchType, Matcher<ExpressionTree> argumentMatcher) {
        return new NewClassHasArguments(matchType, argumentMatcher);
    }


    private static class NewClassHasArguments  extends ChildMultiMatcher<NewClassTree, ExpressionTree> {
        NewClassHasArguments(MatchType matchType, Matcher<ExpressionTree> nodeMatcher) {
            super(matchType, nodeMatcher);
        }
        @Override
        protected Iterable<? extends ExpressionTree> getChildNodes(NewClassTree tree, VisitorState state) {
            return tree.getArguments();
        }
    }

    private static class NewClassInitOfT extends ChildMultiMatcher<NewClassTree, Tree> {
        NewClassInitOfT(Matcher<Tree> typeMatcher) {
            super(AT_LEAST_ONE, typeMatcher);
        }
        @Override
        protected Iterable<? extends Tree> getChildNodes(NewClassTree nc, VisitorState s) {
            List<Tree> testFor = new ArrayList<>();
            if(nc.getClassBody() == null)
                testFor.add(nc);
            else
                testFor.addAll(nc.getClassBody().getImplementsClause());
            return testFor;
        }
    }



    private static class SuperIs extends ChildMultiMatcher<ClassTree, Tree> {
        SuperIs(Matcher<Tree> classMatcher) {
            super(AT_LEAST_ONE, classMatcher);
        }
        @Override
        protected Iterable<? extends Tree> getChildNodes(ClassTree classTree, VisitorState state) {
            List<Tree> supers = new ArrayList<>();
            if(classTree.getImplementsClause().size() > 0)
                supers.addAll(classTree.getImplementsClause());
            if(classTree.getExtendsClause() != null )
                supers.add(classTree.getExtendsClause());
            return supers;
        }
    }
}

//    public static final Matcher<Tree> HAS_TYPE_T = matchesTypeT();
//    public static final Matcher<VariableTree> VARIABLE_TREE_MATCHER = variableType(matchesTypeT());
//    public static final Matcher<MethodTree> RETURN_TYPE_MATCHES = methodReturns(matchesTypeT());
//    public static final Matcher<MethodTree> PARAM_TYPE_MATCHES = methodHasParameters(AT_LEAST_ONE, matchesTypeT());
//    public static final Matcher<MethodInvocationTree> IS_CALLSITE = receiverOfInvocation(matchesTypeT());
//    public static final Matcher<MethodInvocationTree> ARGUMENT_TYPES_MATCH = hasArguments(AT_LEAST_ONE, matchesTypeT());
//    public static final Matcher<MethodInvocationTree> RETURN_TYPE_MATCH = matchesTypeT();
//    public static final Matcher<ClassTree> CLASS_TREE_MATCHER = superIs(matchesTypeT());
//    public static final Matcher<LambdaExpressionTree> LAMBDA_EXPRESSION_TREE_MATCHER =  matchesTypeT();
//    public static final Matcher<IdentifierTree> IDENTIFIER_TREE_MATCHER =  matchesTypeT();
//    public static final Matcher<AssignmentTree> ASSIGNMENT_TREE_MATCHER = assignment(matchesTypeT(),matchesTypeT());
//    public static final Matcher<NewClassTree> IS_NEW_CLASS_INIT_OF = isNewClassInitOf(matchesTypeT());

//    public static final Matcher<MethodInvocationTree> AFFECTED_BY_CALLSITE = (Matcher<MethodInvocationTree>) (mi, vs) ->
//            mi.getMethodSelect() != null && mi.getMethodSelect().getKind().equals(Kind.MEMBER_SELECT) &&
//                    ((MemberSelectTree) mi.getMethodSelect()).getExpression().getKind().equals(Kind.METHOD_INVOCATION) &&
//                    IS_CALLSITE.matches((MethodInvocationTree) ((MemberSelectTree) mi.getMethodSelect()).getExpression(), vs);

//    public static boolean matchTypeT (Type t, VisitorState s){
//        return Migrate.from.stream().map(HasMatchingType::new)
//                .anyMatch(x -> x.isSubType(t,s) || x.isSameType(t,s));
//    }

//
//
//
//    public static final Matcher<MemberReferenceTree> MEMBER_REFERENCE_TREE_MATCHER =
//            (t,s) -> matchesTypeT().matches(t.getQualifierExpression(),s);
//
////    public static final Matcher<EnhancedForLoopTree> ENHANCED_FOR_LOOP_TREE_MATCHER =
////           enhancedForLoop(VARIABLE_TREE_MATCHER,anything(),anything());

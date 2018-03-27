package com.google.errorprone.bugpatterns.refactoringexperiment.collect;

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.bugpatterns.refactoringexperiment.Constants.LAMBDA_EXPRESSION;
import static com.google.errorprone.bugpatterns.refactoringexperiment.Constants.METHOD_INVOCATION;
import static com.google.errorprone.bugpatterns.refactoringexperiment.Constants.PRIMITIVE_WRAPPER;
import static com.google.errorprone.bugpatterns.refactoringexperiment.Constants.WRAPPER_CLASSES;
import static com.google.errorprone.bugpatterns.refactoringexperiment.IdentificationExtractionUtil.infoFromSymbol;
import static com.google.errorprone.bugpatterns.refactoringexperiment.IdentificationExtractionUtil.infoFromTree;
import static com.google.errorprone.bugpatterns.refactoringexperiment.IdentificationExtractionUtil.infoOfTree;
import static java.util.stream.Collectors.collectingAndThen;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.refactoringexperiment.DataFilter;
import com.google.errorprone.bugpatterns.refactoringexperiment.ProtoBuffPersist;
import com.google.errorprone.bugpatterns.refactoringexperiment.models.AssignmentOuterClass.Assignment;
import com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration;
import com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.refactoringexperiment.models.MethodDeclarationOuterClass.MethodDeclaration;
import com.google.errorprone.bugpatterns.refactoringexperiment.models.MethodInvocationOuterClass.MethodInvocation;
import com.google.errorprone.bugpatterns.refactoringexperiment.models.MethodInvocationOuterClass.MethodInvocation.Builder;
import com.google.errorprone.bugpatterns.refactoringexperiment.models.VariableOuterClass.Variable;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AutoService(BugChecker.class)
@BugPattern(
        name = "TypeUsageCollector",
        category = JDK,
        summary = "String formatting inside print method",
        severity = ERROR,
        linkType = CUSTOM,
        link = "example.com/bugpattern/MyCustomCheck"
)
public class TypeUsageCollector extends BugChecker implements BugChecker.MethodTreeMatcher, BugChecker.MethodInvocationTreeMatcher, BugChecker.NewClassTreeMatcher, BugChecker.VariableTreeMatcher
        , BugChecker.AssignmentTreeMatcher, BugChecker.ClassTreeMatcher,BugChecker.MemberReferenceTreeMatcher {

    @Override
    public Description matchMethod(MethodTree methodTree, VisitorState state) {
        boolean paramsMatter = methodTree.getParameters().stream().filter(x -> DataFilter.apply(x, state)).collect(Collectors.toList()).size() > 0;
        boolean returnMatter = DataFilter.apply(methodTree.getReturnType(), state);
        if (paramsMatter || returnMatter) {
            MethodDeclaration.Builder mthdDcl = manageMethodDecl(state, ASTHelpers.getSymbol(methodTree));
            if (returnMatter)
                mthdDcl.setReturnType(DataFilter.getFilteredType(methodTree.getReturnType(), state));

            ProtoBuffPersist.write(mthdDcl, "METHOD");
        }
        return null;
    }

    private MethodDeclaration.Builder manageMethodDecl(VisitorState state, MethodSymbol symb) {
        MethodDeclaration.Builder mthdDcl = MethodDeclaration.newBuilder();
        infoFromSymbol(symb).map(mthdDcl::setId);

        java.util.Optional<MethodSymbol> y = ASTHelpers.findSuperMethods(symb, state.getTypes()).stream().findFirst();
        if (y.isPresent())
            mthdDcl.setSuperMethod(manageMethodDecl(state, y.get()));

        mthdDcl.putAllParameters(Collections.unmodifiableMap(symb.getParameters().stream().filter(x -> DataFilter.apply(x.asType(), state))
                .collect(Collectors.toMap(x -> symb.getParameters().indexOf(x), x -> infoFromSymbol(x).get()))));

        mthdDcl.addAllModifier(symb.getModifiers().stream().map(x -> x.toString()).collect(collectingAndThen(Collectors.toList(),
                Collections::unmodifiableList)));


        return mthdDcl;
    }

    //TODO: Removing method invocation returning lambda for now.
    //mthod returning lambda shud be caught here because i could have something
    //like : getLambda().apply(8);
    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        boolean paramLT = tree.getArguments().stream().filter(x -> DataFilter.apply(x, state))
                .count() > 0;

        boolean ofLT = DataFilter.apply(ASTHelpers.getReceiverType(tree), state);
        boolean returnMatter = DataFilter.apply(ASTHelpers.getReturnType(tree), state);
        if (paramLT || ofLT || returnMatter) {
            MethodInvocation.Builder mthdInvc = MethodInvocation.newBuilder();
            infoFromTree(tree).ifPresent(id -> mthdInvc.setId(id));
            for (ExpressionTree arg : tree.getArguments()) {
                if (DataFilter.apply(arg, state)) {
                    mthdInvc.putArguments(tree.getArguments().indexOf(arg), infoOfTree(arg)).build();
                }
            }
            if (ofLT) {
                infoFromTree(ASTHelpers.getReceiver(tree)).ifPresent(id -> mthdInvc.setReceiver(id));
            }

            mthdInvc.getArgumentsMap().entrySet().stream()
                    .filter(en -> en.getValue().getKind().equals(LAMBDA_EXPRESSION) && lookForWrapperusage(tree.getArguments().get(en.getKey())))
                    .forEach(en -> {
                        Builder wrapper = MethodInvocation.newBuilder();
                        wrapper.setId(Identification.newBuilder()
                                .setName(PRIMITIVE_WRAPPER).setKind(METHOD_INVOCATION).setType(PRIMITIVE_WRAPPER)
                                .setOwner(en.getValue().toBuilder().setOwner(mthdInvc.getId()).build())
                                .build());
                        ProtoBuffPersist.write(wrapper, METHOD_INVOCATION);
                    });
            ProtoBuffPersist.write(mthdInvc, tree.getKind().toString());

        }
        return null;
    }

    @Override
    public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
        if (DataFilter.apply(tree.getQualifierExpression(), state)) {
            MethodInvocation.Builder mthdInvc = MethodInvocation.newBuilder();
            infoFromTree(tree).ifPresent(id -> mthdInvc.setId(id.toBuilder().setKind(METHOD_INVOCATION).build()));
            infoFromTree(tree.getQualifierExpression()).ifPresent(id -> mthdInvc.setReceiver(id));
            ProtoBuffPersist.write(mthdInvc, METHOD_INVOCATION);
        }

        return null;
    }

    @Override
    public Description matchNewClass(NewClassTree var1, VisitorState state) {
        boolean paramMatters = var1.getArguments().stream().filter(x -> DataFilter.apply(x, state))
                .count() > 0;
        if (paramMatters) {
            MethodInvocation.Builder mthdInvc = MethodInvocation.newBuilder();
            infoFromTree(var1).ifPresent(id -> mthdInvc.setId(id));
            for (ExpressionTree arg : var1.getArguments())
                if (DataFilter.apply(arg, state))
                    // mthdInvc.putArguments(var1.getArguments().indexOf(arg),Identifications.newBuilder().addAllId(infoOfTree(arg)).build());
                    mthdInvc.putArguments(var1.getArguments().indexOf(arg), infoOfTree(arg)).build();
            mthdInvc.getArgumentsMap().entrySet().stream()
                    .filter(en -> en.getValue().getKind().equals(LAMBDA_EXPRESSION) && lookForWrapperusage(var1.getArguments().get(en.getKey())))
                    .forEach(en -> {
                        Builder wrapper = MethodInvocation.newBuilder();
                        wrapper.setId(Identification.newBuilder()
                                .setName(PRIMITIVE_WRAPPER).setKind(METHOD_INVOCATION).setType(PRIMITIVE_WRAPPER)
                                .setOwner(en.getValue().toBuilder().setOwner(mthdInvc.getId()).build())
                                .build());
                        ProtoBuffPersist.write(wrapper, METHOD_INVOCATION);
                    });
            ProtoBuffPersist.write(mthdInvc, var1.getKind().toString());
        }
        return null;
    }


    @Override
    public Description matchVariable(VariableTree var1, VisitorState state) {
        if (DataFilter.apply(var1, state)) {
            Variable.Builder vrbl = Variable.newBuilder();
            infoFromTree(var1).map(id -> vrbl.setId(id));
            if (var1.getInitializer() != null)
                vrbl.setInitializer(infoOfTree(var1.getInitializer()));
            vrbl.setFilteredType(DataFilter.getFilteredType(var1, state));
            ProtoBuffPersist.write(vrbl, var1.getKind().toString());
        }
        return null;
    }

    @Override
    public Description matchAssignment(AssignmentTree var1, VisitorState state) {
        ExpressionTree lhs = var1.getVariable();
        if ((lhs.getKind().equals(Tree.Kind.IDENTIFIER) || lhs.getKind().equals(Tree.Kind.MEMBER_SELECT)
                || lhs.getKind().equals(Kind.VARIABLE)) && DataFilter.apply(ASTHelpers.getType(var1), state)) {
            Assignment.Builder asgn = Assignment.newBuilder();
            infoFromTree(lhs).ifPresent(x -> asgn.setLhs(x));
            asgn.setRhs(infoOfTree(var1.getExpression()));
            if (asgn.getRhs().getKind().equals(LAMBDA_EXPRESSION) && lookForWrapperusage(var1.getExpression())) {
                MethodInvocation.Builder wrapper = MethodInvocation.newBuilder();
                wrapper.setId(Identification.newBuilder()
                        .setName(PRIMITIVE_WRAPPER).setKind(METHOD_INVOCATION).setType(PRIMITIVE_WRAPPER)
                        .setOwner(asgn.getRhs().toBuilder().setOwner(asgn.getLhs()).build())
                        .build());
                ProtoBuffPersist.write(wrapper, METHOD_INVOCATION);
            }
            ProtoBuffPersist.write(asgn, var1.getKind().toString());
        }

        return null;
    }

    @Override
    public Description matchClass(ClassTree classTree, VisitorState state) {
        boolean implementsLt = classTree.getImplementsClause().stream().filter(x -> DataFilter.apply(x, state)).count() > 0;
        boolean isLT = DataFilter.apply(classTree, state);
        ClassDeclaration.Builder clsDcl = ClassDeclaration.newBuilder();
        if ((implementsLt || isLT)) {
            infoFromTree(classTree).map(id -> clsDcl.setId(id));
            clsDcl.setSuperType(DataFilter.getFilteredType(classTree, state));
            ProtoBuffPersist.write(clsDcl, classTree.getKind().toString());

        }
        return null;
    }

    private boolean lookForWrapperusage(ExpressionTree ex) {
        LambdaExpressionTree lambda = (LambdaExpressionTree) ex;//
        List<? extends VariableTree> params = lambda.getParameters().stream()
                .filter(x -> WRAPPER_CLASSES.contains(ASTHelpers.getSymbol(x).type.toString()))
                .collect(Collectors.toList());
        PrimitiveUsageCollector visitor = new PrimitiveUsageCollector();
        lambda.accept(visitor, null);
        return visitor.primitiveWrapperUsageCounter > 0 || params.stream().anyMatch(x -> !x.getType().getKind().equals(Kind.MEMBER_SELECT));
    }
}
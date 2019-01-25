package com.google.errorprone.bugpatterns.T2R.common;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.util.stream.Collectors.toList;

import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.FilteredType;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.MethodSign;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeInfo;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeSignature;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ForAll;
import com.sun.tools.javac.code.Type.MethodType;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;

/**
 * This tree visitor, translates an AST element to Identification
 * This translation requires: 
 *  * Visitor state of the compilation unit to query for types
 *  * Identification of its scope. 
 * 
  * Created by ameya on 12/9/18.
 */
public class Tree2Id {

    public static final String INFERRED_ = "INFERRED_";
    public static final String SUPER_CLAUSE = "SUPER_CLAUSE";
    public static final String ANONYMOUS_ = "ANONYMOUS_";

    public static TreeScanner<Identification, Identification> TREE2ID =
            new TreeScanner<Identification,Identification>() {

                @Override
                public Identification visitIdentifier(IdentifierTree i, Identification p) {
                    ElementKind kind = ASTHelpers.getSymbol(i).getKind();
                    if(kind.equals(ElementKind.PACKAGE) || kind.equals(ElementKind.CLASS)){
                        return getIdFromSymbol( ASTHelpers.getSymbol(i)).toBuilder().setOwner(p).build();
                    }
                    if((i.getName().contentEquals("this") || (i.getName().contentEquals("super")))
                            && !kind.equals(ElementKind.CONSTRUCTOR)){
                        return getIdFromSymbol(ASTHelpers.getSymbol(i).owner);
                    }
                    return  getIdFromSymbol( ASTHelpers.getSymbol(i));
                }
                @Override
                public Identification visitParameterizedType(ParameterizedTypeTree i, Identification p) {
                    return ID(null,SUPER_CLAUSE,TypeFor(ASTHelpers.getType(i)),p);
                }

                @Override
                public Identification visitLambdaExpression (LambdaExpressionTree l, Identification p){
                    return ID(null, l.getKind().toString(), TypeFor(ASTHelpers.getType(l)), p);
                }

                @Override
                public Identification visitMethodInvocation(MethodInvocationTree m, Identification p){
                    return getIdFromSymbol(ASTHelpers.getSymbol(m));
                }

                @Override
                public Identification visitMemberSelect (MemberSelectTree m, Identification p){
                    if(ASTHelpers.getSymbol(m).getKind().equals(ElementKind.PACKAGE))
                        return getIdFromSymbol(ASTHelpers.getSymbol(m)).toBuilder().setOwner(p).build();
                    return ID(m.getIdentifier().toString(), INFERRED_+ ASTHelpers.getSymbol(m).getKind().toString(),
                            TypeFor(ASTHelpers.getType(m)), TREE2ID.scan(m.getExpression(), p));
                }

                @Override
                public Identification visitLiteral (LiteralTree m, Identification p){
                    return ID(null, m.getKind().toString(), TypeFor(ASTHelpers.getType(m)), p);
                }

                @Override
                public Identification visitVariable(VariableTree v, Identification p){
                    VarSymbol vs = ASTHelpers.getSymbol(v);
                    return ID(vs.name.toString(), vs.getKind().toString(), TypeFor(ASTHelpers.getType(v)), p);
                }

                @Override
                public Identification visitMethod(MethodTree m, Identification p){
                    MethodSymbol ms = ASTHelpers.getSymbol(m);
                    Type t = ASTHelpers.getType(m);
                    if( t instanceof MethodType) {
                        return ID(getName(ms), ms.getKind().toString(), TypeFor(t), p);
                    }else if(t instanceof ForAll){
                        return ID(getName(ms), ms.getKind().toString(), TypeFor(t.asMethodType()), p);
                    }
                    else{
                        throw new RuntimeException("Could not create method declaration type");
                    }
                }

                @Override
                public Identification visitClass(ClassTree c, Identification p){
                    ClassSymbol cs = ASTHelpers.getSymbol(c);
                    return cs.isAnonymous()
                        ? c.getImplementsClause().isEmpty() ?  ID(null, ANONYMOUS_+ cs.getKind().toString(),TypeFor(ASTHelpers.getType(c.getExtendsClause())),p)
                            :ID(null,ANONYMOUS_ + cs.getKind().toString(),TypeFor(ASTHelpers.getType(c.getImplementsClause().get(0))),p)
                        : ID(cs.name.toString(),cs.getKind().toString(),TypeFor(ASTHelpers.getType(c)),p);
                }

                @Override
                public Identification visitNewClass(NewClassTree c, Identification p){
                    if(c.getClassBody() == null) {
                        MethodSymbol s = ASTHelpers.getSymbol(c);
                        return ID(getName(s),INFERRED_+s.getKind().toString(),TypeFor(s.asType()),p);
                    }
                    return TREE2ID.scan(c.getClassBody(),p);
                }

                @Override
                public Identification visitBinary(BinaryTree x, Identification id){
                    return ID(null,x.getKind().toString(),null, id);
                }

            };



    public static Identification ID(String name, String kind, TypeSignature type, Identification owner){
        Identification.Builder id_builder = Identification.newBuilder();
        if(name!=null && !name.isEmpty()) { id_builder.setName(name);   }
        if(!kind.isEmpty())               { id_builder.setKind(kind);   }
        if(type != null)                  { id_builder.setType(type);   }
        if(owner!=null)                   { id_builder.setOwner(owner); }
        return id_builder.build();
    }


    public static Identification getIdFromSymbol(Symbol s){

        if(s.getKind().equals(ElementKind.OTHER))
            return Identification.newBuilder().setName("Ow!").build();

        return  s.getKind().equals(ElementKind.PACKAGE)
                ? ID(((PackageSymbol) s).fullname.toString(),PACKAGE.toString(),null,null)
                : ID(getName(s),INFERRED_+s.getKind().toString(),TypeFor(s.asType()),getIdFromSymbol(s.owner));
    }

    private static String getName(Symbol s) {
        return  s.isConstructor() ? s.enclClass().toString() : s.name.toString();
    }


    private static TypeSignature TypeFor(Type t){
        if(null == t)
            return null;

        if(t instanceof ForAll) {
            return getMethodSignature(((ForAll)t).asMethodType());
        }

        return (t instanceof MethodType) ? getMethodSignature((MethodType) t)
                :TypeSignature.newBuilder().setTypeSign(getTypeInfo(t)).build();
    }

    private static TypeInfo getTypeInfo (Type t){
        return (t.getKind().equals(TypeKind.TYPEVAR)) || t.getKind().equals(TypeKind.WILDCARD)
                ? TypeInfo.newBuilder().setAnyType(t.toString()).build()
                : TypeInfo.newBuilder().setOf(FilteredType.newBuilder().setInterfaceName(t.asElement().toString())
                   .addAllTypeParameter(t.getTypeArguments().stream().map(Tree2Id::getTypeInfo).collect(toList()))).build();
    }

    private static TypeSignature getMethodSignature(MethodType  m){
        return TypeSignature.newBuilder().setMthdSign(MethodSign.newBuilder()
                .setReturnType(getTypeInfo(m.getReturnType()))
                .addAllParam(m.getParameterTypes().stream()
                        .map(Tree2Id::getTypeInfo).collect(toList()))).build();
    }

    public static Identification getPackageId(CompilationUnitTree cu) {
        return Identification.newBuilder().setName(cu.getPackageName().toString())
                .setKind((PACKAGE.toString())).build();
    }

}

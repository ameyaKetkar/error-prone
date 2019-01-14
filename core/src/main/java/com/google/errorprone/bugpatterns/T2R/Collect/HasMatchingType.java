package com.google.errorprone.bugpatterns.T2R.Collect;


import com.google.common.collect.Streams;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeInfo;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.MethodType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


/**
 * Created by ameya on 11/25/18.
 */

public class HasMatchingType<T extends Tree> implements Matcher<T> {

    private String baseTypeName;
    private List<TypeInfo> typeArgs;
    private Function<VisitorState,Type> baseType;
    protected boolean isAnyType;

    HasMatchingType(TypeInfo typeInfo) {
        if(!typeInfo.hasAnyType() ) {
            this.baseTypeName = typeInfo.getOf().getInterfaceName();
            this.typeArgs = typeInfo.getOf().getTypeParameterList();
            this.baseType = s -> s.getTypeFromString(baseTypeName);

        }
        else { isAnyType = true;}
    }
    @Override
    public boolean matches(T t, VisitorState s) {
        if(t == null)
            return false;
        if(isAnyType) return true;
        Type type = ASTHelpers.getType(t);
        type = (type instanceof MethodType) ? type.getReturnType() : type;
        return isSameType(type,s) || isSubType(type,s);
    }

    private boolean matchTypeParams(List<TypeInfo> typArgs, List<Type> args, VisitorState s){
        //(typArgs.size() == 0 && args.size() == 0) ||
        return  typArgs.size() == args.size()
                && Streams.zip(typArgs.stream(), args.stream(),
                (ti, t) -> new HasMatchingType<>(ti).isSameType(t, s)).allMatch(x -> x);
    }

    private List<Type> getTypeArgsAsSuper(Type baseType, Type superType, VisitorState state) {
        if(superType == null)
            return new ArrayList<>();

            Type projectedType = state.getTypes().asSuper(baseType, superType.tsym);
            return projectedType != null ? projectedType.getTypeArguments()
                    : new ArrayList<>();

    }

    boolean isSameType (Type t, VisitorState s){
        if(isAnyType) return true;
        List<Type> type_Args = getTypeArgsAsSuper(t, s.getTypeFromString(baseTypeName), s);
        return ASTHelpers.isSameType(t,s.getTypeFromString(baseTypeName),s) && matchTypeParams(typeArgs,type_Args,s);
    }
    boolean isSubType (Type t, VisitorState s){
        if(isAnyType) return true;
        List<Type> type_Args = getTypeArgsAsSuper(t, s.getTypeFromString(baseTypeName), s);
        return (ASTHelpers.isSubtype(t,s.getTypeFromString(baseTypeName),s) && !ASTHelpers.isSameType(t,s.getTypeFromString(baseTypeName),s))
                && matchTypeParams(typeArgs,type_Args,s);
    }




}

package com.google.errorprone.bugpatterns.T2R.Analysis;

import static com.google.errorprone.bugpatterns.T2R.Analysis.Analysis.METHOD;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.ARG_INDEX;
import static com.google.errorprone.bugpatterns.T2R.Collect.GenerateTFG.PARAM_INDEX;
import static com.google.errorprone.bugpatterns.T2R.common.Util.L;
import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static java.util.stream.Collectors.toList;

import com.google.errorprone.bugpatterns.T2R.common.Models.EditInstructionOuterClass.EditCmd;
import com.google.errorprone.bugpatterns.T2R.common.Models.EditInstructionOuterClass.EditInstruction;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program.ChangeInstruction;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program.ChangeType;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program.MethodChange;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.FilteredType;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.MethodSign;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeInfo;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeSignature;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by ameya on 12/13/18.
 */
public class Migrate {
    private static TypeSignature methodSign(TypeInfo ret, List<TypeInfo> ps){
        return ps.size() > 0 ? TypeSignature.newBuilder().setMthdSign(MethodSign.newBuilder().setReturnType(ret).addAllParam(ps)).build()
                :TypeSignature.newBuilder().setMthdSign(MethodSign.newBuilder().setReturnType(ret)).build();
    }

    public static TypeInfo type(String name, List<TypeInfo> typeParams){
        return  typeParams.size() > 0 ? TypeInfo.newBuilder().setOf(FilteredType.newBuilder().setInterfaceName(name)
                .addAllTypeParameter(typeParams)).build() : TypeInfo.newBuilder().setOf(FilteredType.newBuilder().setInterfaceName(name)).build();

    }
    private static ChangeInstruction changeType(Program p){
        return ChangeInstruction.newBuilder().setChngType(ChangeType.newBuilder().setValue(p)).build();
    }

    private static ChangeInstruction changeType(Program p, String navigateTo){
        return ChangeInstruction.newBuilder().setChngType(ChangeType.newBuilder().setValue(p).setNavigateTo(navigateTo)).build();
    }

    private static EditInstruction editType(TypeInfo p){
        return EditInstruction.newBuilder().setEditType(p).build();
    }

    private static EditInstruction editName(String n){
        return EditInstruction.newBuilder().setEditName(n).build();
    }

    private static ChangeInstruction editAsChng(EditInstruction e){
        return ChangeInstruction.newBuilder().setEditInstr(e).build();
    }

    private static MethodChange methodChange(Identification i, List<ChangeInstruction> c){
        return  MethodChange.newBuilder().setId(i).addAllChange(c).build();
    }

    private static EditInstruction Identity = EditInstruction.newBuilder().setCmd(EditCmd.Identity).build();
    private static EditInstruction DoNotMigrate = EditInstruction.newBuilder().setCmd(EditCmd.DoNotMigrate).build();

    public static final String JAVA_UTIL_FUNCTION_FUNCTION = "java.util.function.Function";
    public static final String JAVA_UTIL_FUNCTION_BIFUNCTION = "java.util.function.BiFunction";
    public static final String INT_UNARY_OPERATOR = "java.util.function.IntUnaryOperator";
    public static final String DOUBLE_UNARY_OPERATOR = "java.util.function.DoubleUnaryOperator";
    public static final String LONG_UNARY_OPERATOR = "java.util.function.LongUnaryOperator";
    public static final String TO_INT_FUNCTION = "java.util.function.ToIntFunction";
    public static final String TO_DOUBLE_FUNCTION = "java.util.function.ToDoubleFunction";
    public static final String TO_LONG_FUNCTION = "java.util.function.ToLongFunction";
    public static final String INT_FUNCTION = "java.util.function.IntFunction";
    public static final String DOUBLE_FUNCTION = "java.util.function.DoubleFunction";
    public static final String LONG_FUNCTION = "java.util.function.LongFunction";
    public static final String INT_TO_DOUBLE_FUNCTION = "java.util.function.IntToDoubleFunction";
    public static final String INT_TO_LONG_FUNCTION = "java.util.function.IntToLongFunction";
    public static final String INT_PREDICATE = "java.util.function.IntPredicate";
    public static final String LONG_PREDICATE = "java.util.function.LongPredicate";
    public static final String TO_LONG_BIFUNCTION = "java.util.function.ToLongBiFunction";
    public static final String BI_CONSUMER = "java.util.function.BiConsumer";
    public static final String OBJ_LONG_COMSUMER = "java.util.function.ObjLongConsumer";
    public static final String PREDICATE = "java.util.function.Predicate";
    public static TypeInfo WInt = type("java.lang.Integer",L());
    public static TypeInfo PInt = type("int", L());
    public static TypeInfo WDbl = type("java.lang.Double", L());
    public static TypeInfo PDbl = type("double", L());
    public static TypeInfo WBln = type("java.lang.Boolean", L());
    public static TypeInfo PBln = type("boolean", L());
    public static TypeInfo WLng = type("java.lang.Long", L());
    public static TypeInfo PLng = type("long", L());
    public static TypeInfo Void = type("void", L());
    public static TypeInfo ANY = TypeInfo.newBuilder().setAnyType("T").build();
    public static final String APPLY = "apply";
    public static final String APPLY_AS_DOUBLE = "applyAsDouble";
    public static final String APPLY_AS_LONG = "applyAsLong";
    public static final String APPLY_AS_INT = "applyAsInt";
    public static final String TEST = "test";

    public static ChangeInstruction asChangeInstr(EditInstruction e){
        return ChangeInstruction.newBuilder().setEditInstr(e).build();
    }

    private static Program NotoINTBoxing(){
        return Program.newBuilder()
                .setFrom(WInt).setTo(PInt)
                .setDeclarations(editType(PInt))
                .setIdentifiers(Identity)
                .build();
    }

    private static Program NotoDblBoxing(){
        return Program.newBuilder()
                .setFrom(WDbl).setTo(PDbl)
                .setDeclarations(editType(PDbl))
                .setIdentifiers(Identity)
                .build();
    }

    private static Program NotoLngBoxing(){
        return Program.newBuilder()
                .setFrom(WLng).setTo(PLng)
                .setDeclarations(editType(PLng))
                .setIdentifiers(Identity)
                .build();
    }

    private static Program IntegerToInt(){
        Identification intValue = Identification.newBuilder().setName("intValue").setType(methodSign(PInt,L())).setKind(METHOD).build();
        return Program.newBuilder()
                .setFrom(WInt).setTo(PInt)
                .setDeclarations(editType(PInt))
                .addMethodChange(methodChange(intValue,L(ChangeInstruction.newBuilder().setCmd(EditCmd.Remove).build()
                        , changeType(NotoINTBoxing()))))
                .setIdentifiers(Identity)
                .addNonEditableExpr(Identity)
                .build();
    }

    private static Program Booleantoboolean(){
        return Program.newBuilder()
                .setFrom(WBln).setTo(PBln)
                .setDeclarations(editType(PBln))
                .setIdentifiers(Identity)
                .addNonEditableExpr(Identity)
                .build();
    }

    private static Program DoubleTodbl() {
        Identification intValue = Identification.newBuilder().setName("doubleValue").setType(methodSign(PInt,L())).setKind(METHOD).build();
        return Program.newBuilder()
                .setFrom(WDbl).setTo(PDbl)
                .setDeclarations(editType(PDbl))
                .addMethodChange(methodChange(intValue,L(ChangeInstruction.newBuilder().setCmd(EditCmd.Remove).build(), changeType(NotoDblBoxing()))))
                .addNonEditableExpr(Identity)
                .build();
    }

    private static Program LongTolong() {
        Identification intValue = Identification.newBuilder().setName("longValue").setType(methodSign(PLng,L())).setKind(METHOD).build();
        return Program.newBuilder()
                .setFrom(WLng).setTo(PLng)
                .setDeclarations(editType(PLng))
                .addMethodChange(methodChange(intValue,L(ChangeInstruction.newBuilder().setCmd(EditCmd.Remove).build(), changeType(NotoLngBoxing()))))
                .addNonEditableExpr(Identity)
                .build();
    }

    private static Program funcIntIntToIntUnaryOp(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WInt,L(WInt))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(WInt,WInt));
        TypeInfo IntUnary = type(INT_UNARY_OPERATOR, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(IntUnary)
                .setDeclarations(editType(IntUnary))
                .addMethodChange(methodChange(apply,L(asChangeInstr(editName(APPLY_AS_INT))
                        ,changeType(IntegerToInt())
                        , changeType(IntegerToInt(),PARAM_INDEX + 0)
                        , changeType(IntegerToInt(),ARG_INDEX + 0) )))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcDblDblToDblUnaryOp(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WDbl,L(WDbl))).setKind(METHOD).build();
        TypeInfo funcDblDbl = type(JAVA_UTIL_FUNCTION_FUNCTION, L(WDbl,WDbl));
        TypeInfo DblUnary = type(DOUBLE_UNARY_OPERATOR, L());
        return Program.newBuilder()
                .setFrom(funcDblDbl).setTo(DblUnary)
                .setDeclarations(editType(DblUnary))
                .addMethodChange(methodChange(apply,
                        L(editAsChng(editName(APPLY_AS_DOUBLE))
                                , changeType(DoubleTodbl())
                                , changeType(DoubleTodbl(), PARAM_INDEX + 0)
                                , changeType(DoubleTodbl(), ARG_INDEX + 0))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcLngLngToLngUnaryOp(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WLng,L(WLng))).setKind(METHOD).build();
        TypeInfo funcDblDbl = type(JAVA_UTIL_FUNCTION_FUNCTION, L(WLng,WLng));
        TypeInfo DblUnary = type(LONG_UNARY_OPERATOR, L());
        return Program.newBuilder()
                .setFrom(funcDblDbl).setTo(DblUnary)
                .setDeclarations(editType(DblUnary))
                .addMethodChange(methodChange(apply,
                        L(editAsChng(editName(APPLY_AS_LONG))
                                , changeType(LongTolong())
                                , changeType(LongTolong(), PARAM_INDEX + 0)
                                , changeType(LongTolong(), ARG_INDEX + 0))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcTIntToToIntFunc(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WInt,L(ANY))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(ANY,WInt));
        TypeInfo ToIntFunc = type(TO_INT_FUNCTION, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(ToIntFunc)
                .setDeclarations(editType(ToIntFunc))
                .addMethodChange(methodChange(apply,L(asChangeInstr(editName(APPLY_AS_INT))
                        ,changeType(IntegerToInt()))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcTDblToToDblFunc(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WDbl,L(ANY))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(ANY,WDbl));
        TypeInfo ToIntFunc = type(TO_DOUBLE_FUNCTION, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(ToIntFunc)
                .setDeclarations(editType(ToIntFunc))
                .addMethodChange(methodChange(apply,L(asChangeInstr(editName(APPLY_AS_DOUBLE))
                        ,changeType(DoubleTodbl()))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcTLngToToLngFunc(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WLng,L(ANY))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(ANY,WLng));
        TypeInfo ToIntFunc = type(TO_LONG_FUNCTION, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(ToIntFunc)
                .setDeclarations(editType(ToIntFunc))
                .addMethodChange(methodChange(apply,L(asChangeInstr(editName(APPLY_AS_LONG))
                        ,changeType(LongTolong()))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcTBlnToPred(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WBln,L(ANY))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(ANY,WBln));
        TypeInfo ToIntFunc = type(PREDICATE, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(ToIntFunc)
                .setDeclarations(editType(ToIntFunc))
                .addMethodChange(methodChange(apply,L(asChangeInstr(editName(TEST))
                        ,changeType(Booleantoboolean()))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcIntTToIntFunc(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(ANY,L(WInt))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(WInt,ANY));
        TypeInfo ToIntFunc = type(INT_FUNCTION, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(ToIntFunc)
                .setDeclarations(editType(ToIntFunc))
                .addMethodChange(methodChange(apply,L(changeType(IntegerToInt()))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcLngTToLngFunc(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(ANY,L(WLng))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(WLng,ANY));
        TypeInfo ToIntFunc = type(LONG_FUNCTION, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(ToIntFunc)
                .setDeclarations(editType(ToIntFunc))
                .addMethodChange(methodChange(apply,L(changeType(LongTolong()))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcDblTToDblFunc(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(ANY,L(WDbl))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(WDbl,ANY));
        TypeInfo ToIntFunc = type(DOUBLE_FUNCTION, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(ToIntFunc)
                .setDeclarations(editType(ToIntFunc))
                .addMethodChange(methodChange(apply,L(changeType(DoubleTodbl()))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcIntDblToIntToDblFunc(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WDbl,L(WInt))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(WInt,WDbl));
        TypeInfo ToIntFunc = type(INT_TO_DOUBLE_FUNCTION, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(ToIntFunc)
                .setDeclarations(editType(ToIntFunc))
                .addMethodChange(methodChange(apply,L(asChangeInstr(editName(APPLY_AS_DOUBLE))
                        ,changeType(DoubleTodbl()), changeType(IntegerToInt(),PARAM_INDEX + 0), changeType(IntegerToInt(), ARG_INDEX + 0))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcIntLngToIntToLngFunc(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WLng,L(WInt))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(WInt,WLng));
        TypeInfo ToIntFunc = type(INT_TO_LONG_FUNCTION, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(ToIntFunc)
                .setDeclarations(editType(ToIntFunc))
                .addMethodChange(methodChange(apply,L(asChangeInstr(editName(APPLY_AS_LONG))
                        ,changeType(LongTolong()), changeType(IntegerToInt(),PARAM_INDEX + 0), changeType(IntegerToInt(), ARG_INDEX + 0))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcIntBlnToIntPred(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WBln,L(WInt))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(WInt,WBln));
        TypeInfo IntPred = type(INT_PREDICATE, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(IntPred)
                .setDeclarations(editType(IntPred))
                .addMethodChange(methodChange(apply,L(asChangeInstr(editName(TEST))
                        ,changeType(Booleantoboolean()), changeType(IntegerToInt(),PARAM_INDEX + 0), changeType(IntegerToInt(), ARG_INDEX + 0))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program funcLongBlnToLongPred(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WBln,L(WLng))).setKind(METHOD).build();
        TypeInfo funcIntInt = type(JAVA_UTIL_FUNCTION_FUNCTION, L(WLng,WBln));
        TypeInfo LongPred = type(LONG_PREDICATE, L());
        return Program.newBuilder()
                .setFrom(funcIntInt)
                .setTo(LongPred)
                .setDeclarations(editType(LongPred))
                .addMethodChange(methodChange(apply,L(asChangeInstr(editName(TEST))
                        ,changeType(Booleantoboolean()), changeType(LongTolong(),PARAM_INDEX + 0), changeType(LongTolong(), ARG_INDEX + 0))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program PredLongToLongPred(){
        Identification test = Identification.newBuilder().setName(TEST).setType(methodSign(PBln,L(WLng))).setKind(METHOD).build();
        TypeInfo PredLong = type(PREDICATE, L(WLng));
        TypeInfo LongPred = type(LONG_PREDICATE, L());
        return Program.newBuilder()
                .setFrom(PredLong)
                .setTo(LongPred)
                .setDeclarations(editType(LongPred))
                .addMethodChange(methodChange(test,L(changeType(LongTolong(),PARAM_INDEX + 0), changeType(LongTolong(), ARG_INDEX + 0))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program bifuncTTLongtoToLongBiFunc(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(WLng,L(ANY, ANY))).setKind(METHOD).build();
        TypeInfo biFuncTTLng = type(JAVA_UTIL_FUNCTION_BIFUNCTION, L(ANY,ANY,WLng));
        TypeInfo longBiFunc = type(TO_LONG_BIFUNCTION, L());
        return Program.newBuilder()
                .setFrom(biFuncTTLng)
                .setTo(longBiFunc)
                .setDeclarations(editType(longBiFunc))
                .addMethodChange(methodChange(apply,L(asChangeInstr(editName(APPLY_AS_LONG))
                        ,changeType(LongTolong()))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    private static Program BiConsumerTLngToObjLngConsumer(){
        Identification apply = Identification.newBuilder().setName(APPLY).setType(methodSign(Void,L(ANY, WLng))).setKind(METHOD).build();
        TypeInfo biFuncTTLng = type(BI_CONSUMER, L(ANY,WLng));
        TypeInfo longBiFunc = type(OBJ_LONG_COMSUMER, L());
        return Program.newBuilder()
                .setFrom(biFuncTTLng)
                .setTo(longBiFunc)
                .setDeclarations(editType(longBiFunc))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    public static final String SUPPLIER = "java.util.function.Supplier";
    public static final String INT_SUPPlIER = "java.util.function.IntSupplier";
    public static final String GET = "get";
    public static final String GET_AS_INT = "getAsInt";

    private static Program SupplierIntToIntSupplier(){
        Identification apply = Identification.newBuilder().setName(GET).setType(methodSign(WInt,L())).setKind(METHOD).build();
        TypeInfo supplierInt = type(SUPPLIER, L(WInt));
        TypeInfo intSupplier = type(INT_SUPPlIER, L());
        return Program.newBuilder()
                .setFrom(supplierInt)
                .setTo(intSupplier)
                .setDeclarations(editType(intSupplier))
                .addMethodChange(methodChange(apply,L(asChangeInstr(editName(GET_AS_INT))
                        ,changeType(IntegerToInt()))))
                .setLambda(Identity)
                .setIdentifiers(Identity)
                .addNonEditableExpr(DoNotMigrate)
                .build();
    }

    public static List<Program> mapping = Stream.of(funcIntIntToIntUnaryOp(),funcDblDblToDblUnaryOp(), funcIntDblToIntToDblFunc(),
            funcTIntToToIntFunc(), funcTDblToToDblFunc(), funcLongBlnToLongPred(),funcTBlnToPred(), bifuncTTLongtoToLongBiFunc(),
            PredLongToLongPred(), BiConsumerTLngToObjLngConsumer(), funcIntTToIntFunc(), SupplierIntToIntSupplier()).collect(toList());

    public static List<TypeInfo> from = mapping.stream().map(Program::getFrom).collect(toList());

    public static List<Refactorable> to = new ArrayList<>();

    public static boolean match(Pair<Identification,Identification> p){
        final Identification id = p.snd();
        final Identification ref = p.fst();
        boolean result = true;

        if(ref.hasType())
            result = ref.getType().equals(id.getType());
        if(ref.hasOwner())
            result = result && match(P(ref.getOwner(),id.getOwner()));

        return result;
    }
}

package com.google.errorprone.bugpatterns.T2R.common;

import com.google.errorprone.bugpatterns.T2R.common.Models.EditInstructionOuterClass.EditInstruction;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeInfo;
import com.google.errorprone.bugpatterns.T2R.common.Models.TypeSignatureOuterClass.TypeSignature;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by A on 12/17/18.
 */
public class Visualizer {

//    public static void visualizeGraph(String fileName, List<TFG> tfg) {
//
//        for(int i = 0; i < tfg.size(); i++) {
//            final List<Node> ns = generateEdge(tfg.get(i));
//            Node[] t = ns.toArray(new Node[ns.size()]);
//            Graph g = graph().directed().with(t);
//            try {
//                Graphviz.fromGraph(g).height(200).width(100).render(Format.SVG).toFile(new File("/Users/ameya/" + fileName + i +".svg"));
//            } catch (IOException e) {
//                System.out.println(e.toString());
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void visualizeGraphR(String fileName, List<TFGRefactorable> tfg) {
//
//        for(int i = 0; i < tfg.size(); i++) {
//            final List<Node> ns = generateEdge(tfg.get(i));
//            Node[] t = ns.toArray(new Node[ns.size()]);
//            Graph g = graph().directed().with(t);
//            try {
//                Graphviz.fromGraph(g).height(200).width(100).render(Format.SVG).toFile(new File("/Users/ameya/" + fileName + i +"R.svg"));
//            } catch (IOException e) {
//                System.out.println(e.toString());
//                e.printStackTrace();
//            }
//        }
//    }

//    private static Node getNode(Identification id){
//        if(!id.hasOwner())
//            return  node(id.getName() + "\n" + id.getKind());
//        return node(id.getName() + "\n" + id.getKind() + "\n" + prettyType(id.getType()) + "\n" + qualifiedName(id)+ "\n"+  id.getOwner().hashCode());
//    }
//
//    private static Node getNode(Refactorable r){
//        if(!r.getId().hasOwner())
//            return  node(r.getId().getName() + "\n" + r.getId().getKind());
//        return node(r.getId().getName() + "\n" + r.getId().getKind() + "\n" + prettyType(r.getId().getType()) + "\n" + qualifiedName(r.getId())+ "\n"
//                +"\n" + prettyEdits(r.getEditInstructionsList()) +  r.getId().getOwner().hashCode());
//
//    }


    public static String prettyState(State s){
        return s.getRoot() + "-- " + qualifiedName(s.getRootID()) + "--" + prettyType(s.getRootID().getType()) + s.getTypeDependents().stream().map(x -> prettyState(x.fst())).collect(Collectors.joining("\n.."));
    }

    public static String qualifiedName (Identification id){
        if(!id.hasOwner())
            return id.getName() + " " + id.getKind();
        return id.getName() + " " + id.getKind() + " || " + qualifiedName(id.getOwner());
    }


    public static String prettyType(TypeInfo ti){
        if(ti.hasAnyType())
            return "ANY";
        else
            return ti.getOf().getInterfaceName() + (ti.getOf().getTypeParameterCount() > 0
                    ? "<" + ti.getOf().getTypeParameterList().stream().map(z -> prettyType(z)).collect(Collectors.joining(",")) + ">"
                    : "");
    }


    public static String prettyType (TypeSignature ts){
        if(ts.hasTypeSign())
            return prettyType(ts.getTypeSign());
        else
            return  prettyType(ts.getMthdSign().getReturnType()) + "  (" +
                    ts.getMthdSign().getParamList().stream().map(z-> prettyType(z))
                            .collect(Collectors.joining(",")) + ") ";
    }

    public static String prettyEdits(List<EditInstruction> edits){
        return edits.stream().map(Visualizer::prettyEdit).collect(Collectors.joining("\n"));
    }
    private static String prettyEdit(EditInstruction e){
        if(e.hasEditName())
            return "EDIT NAME : " + e.getEditName();
        else if(e.hasEditType())
            return "EDIT TYPE : " + e.getEditType().getOf();
        else
            return "CMD : " + e.getCmd().toString();
    }


//    private static List<Node> generateEdge(TFG tfg){
//        final List<Identification> ns = tfg.getNodesList();
//        final Map<Integer, List<Edge>> g = tfg.getEdgesList().stream().collect(Collectors.groupingBy(x -> x.getFst()));
//        return g.entrySet().stream()
//                .map(e -> P(getNode(ns.get(e.getKey())),
//                        e.getValue().stream().map(x -> to(getNode(ns.get(x.getSnd()))).with(Label.of(x.getEdgeValue()))).collect(toList())))
//                .map(p -> p.fst().link(p.snd().toArray(new Link[p.snd().size()]))).collect(toList());
//    }
//
//    private static List<Node> generateEdge(TFGRefactorable tfg){
//        final List<Refactorable> ns = tfg.getNodesList();
//        final Map<Integer, List<REdge>> g = tfg.getEdgesList().stream().collect(Collectors.groupingBy(x -> x.getFst()));
//        return g.entrySet().stream()
//                .map(e -> P(getNode(ns.get(e.getKey())),
//                        e.getValue().stream().map(x -> to(getNode(ns.get(x.getSnd()))).with(Label.of(x.getEdgeValue()))).collect(toList())))
//                .map(p -> p.fst().link(p.snd().toArray(new Link[p.snd().size()]))).collect(toList());
//    }



}

package com.google.errorprone.bugpatterns.T2R.common;


import static com.google.errorprone.bugpatterns.T2R.common.Util.Pair.P;
import static com.google.errorprone.bugpatterns.T2R.common.Util.nullHandleReduce;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.Models.TFGOuterClass.TFG;
import com.google.errorprone.bugpatterns.T2R.common.Models.TFGOuterClass.TFG.Edge;
import com.google.errorprone.bugpatterns.T2R.common.Models.TFGRefactorableOuterClass.TFGRefactorable;
import com.google.errorprone.bugpatterns.T2R.common.Models.TFGRefactorableOuterClass.TFGRefactorable.REdge;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public class TypeFactGraph<U>{


    private ImmutableValueGraph<U,String> tfg;

    private TypeFactGraph(MutableValueGraph<U,String> g){
        tfg = ImmutableValueGraph.copyOf(g);
    }


    public ImmutableValueGraph<U, String> get() {
        return tfg;
    }

    public static <U> TypeFactGraph<U> of(MutableValueGraph<U,String> g){
        return new TypeFactGraph<>(g);
    }

    /**
     *
     * @param f : function which transforms the underlying graph as guava's Mutable Value Graphs
     * @return : returns a TFG where this the transformation function 'f' has been applied on TFG
     */
    public TypeFactGraph<U> map(Function<MutableValueGraph<U,String>, MutableValueGraph<U,String>> f){
        return of(f.apply(Graphs.copyOf(tfg)));
    }

    public TypeFactGraph<U> flatMap(
            Function<MutableValueGraph<U,String>, TypeFactGraph<U>> f){
        return  f.apply(Graphs.copyOf(tfg));
    }

    /**
     *
     * @param fs : list of functions to be applied on tfg
     * @return returns List of
     */
    public TypeFactGraph<U> mergeMap(List<Function<MutableValueGraph<U,String>, MutableValueGraph<U, String>>> fs){
        return map(fs.stream().reduce(Function.identity(),Function::andThen));

    }

    public boolean isEmpty(){
        return tfg.nodes().size() == 0;
    }

    // Basic graph operations

    /**
     *
     * @param u : node to be added
     * @param <U> : Type of the nodes in TFG
     * @return : Returns a function which adds node 'u' to a graph.
     */
    public static <U> Function<MutableValueGraph<U,String>, MutableValueGraph<U,String>> addNode(U u){
        return  g -> { g.addNode(u);return g; };
    }

    /**
     *
     * @param a From node
     * @param b To Node
     * @param aTob Edge Label
     * @return : Returns a function which establishes an directed labelled edge between two nodes of a graph
     */
    public static <U> Function<MutableValueGraph<U,String>, MutableValueGraph<U,String>> u_v(U a, U b, String aTob) {
        return g -> {
            g.putEdgeValue(a, b, aTob);
            return g;
        };
    }

    /**
     *
     * @param a Node
     * @param b Node
     * @param aBbA Pair(Edge label from 'a' to 'b', Edge label from 'b' to 'a')
     * @return Returns a function which establishes directional labelled edges from 'a' to 'b' and from 'b' to 'a'
     */
    public static <U> Function<MutableValueGraph<U,String>, MutableValueGraph<U,String>> uV_vU(U a, U b, Util.Pair<String, String> aBbA) {
        if(aBbA.fst().equals(NO_EDGE) || aBbA.snd().equals(NO_EDGE))
            return Function.identity();
        return u_v(a,b,aBbA.fst()).andThen(u_v(b,a,aBbA.snd()));
    }

    /**
     *
     * @param <U> Type of nodes
     * @return Returns a directed empty graph TFG
     */
    public static <U> TypeFactGraph<U> emptyTFG() {
        return new TypeFactGraph<>(ValueGraphBuilder.directed().allowsSelfLoops(true).build());
    }

    /**
     *
     * @param a input graph for merge operation
     * @param b input graph for merge operation
     * @param <T> Type of Nodes in the graph
     * @return It merges the graph while preserving the edge labels.
     */
    public static <T> TypeFactGraph<T> mergeGraphs(TypeFactGraph<T> a, TypeFactGraph<T> b) {
        final BinaryOperator<TypeFactGraph<T>> merge = (gl, gr) -> {
            ImmutableValueGraph<T, String> g1 = gl.get();
            ImmutableValueGraph<T, String> g2 = gr.get();
            MutableValueGraph<T, String> graph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
            g1.edges().forEach(e -> graph.putEdgeValue(e.nodeU(), e.nodeV(), g1.edgeValue(e.nodeU(), e.nodeV()).get()));
            g2.edges().forEach(e -> graph.putEdgeValue(e.nodeU(), e.nodeV(), g2.edgeValue(e.nodeU(), e.nodeV()).get()));
            g1.nodes().forEach(graph::addNode);
            g2.nodes().forEach(graph::addNode);
            return TypeFactGraph.of(graph);
        };
        return nullHandleReduce(a,b,merge, emptyTFG());
    }

    /**
     *
     * @param tfg Input graph
     * @return Returns a com.google.common.graph.MutableValueGraph<T> of the tfg.
     */
    private static <T> MutableValueGraph<T,String> getMutableOf(TypeFactGraph<T> tfg){
        MutableValueGraph<T,String> gr = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
        for(EndpointPair<T> uv : tfg.get().edges())
            gr.putEdgeValue(uv.nodeU(),uv.nodeV(), tfg.get().edgeValue(uv.nodeU(), uv.nodeV()).get());
        for(T t: tfg.get().nodes())
            gr.addNode(t);
        return gr;
    }

    /**
     *
     * @param tfg input graph for node replacement operation
     * @param replaceWith input pairs (replace * with)
     * @return This operation replaces the first element of the input pairs with their respective second
     * element. This operation preserves the edges, and then eliminates the 'replaced' element from the graph.
     */
    public static <T> TypeFactGraph<T> replace(TypeFactGraph<T> tfg, final List<Pair<T,T>> replaceWith) {
        MutableValueGraph<T,String> gr = getMutableOf(tfg);
        for(Pair<T,T> rw: replaceWith){
            final T replace = rw.fst();
            final T with  = rw.snd();
            final Set<T> succ = tfg.get().successors(replace);
            for(T s: succ){
                String edgeValue = tfg.get().edgeValue(replace,s).get();
                gr.putEdgeValue(with,s,edgeValue);
            }
            final Set<T> pred = tfg.get().successors(replace);
            for(T p: pred){
                String edgeValue = tfg.get().edgeValue(p,replace).get();
                gr.putEdgeValue(p,with,edgeValue);
            }
        }

        for(Pair<T,T> rw: replaceWith){
            gr.removeNode(rw.fst());
        }
        return of(gr);
    }

    /**
     *
     * @param tfg input graph for edge remove operation
     * @param removeEdgeBetween : input pairs of nodes
     * @return returns a tfg with removed edges between the input pairs of nodes
     */
    public static <T> TypeFactGraph<T> removeEdge(TypeFactGraph<T> tfg, List<Util.Pair<T,T>> removeEdgeBetween) {
        MutableValueGraph<T, String> gr = getMutableOf(tfg);
        for(Util.Pair<T,T> rw: removeEdgeBetween) {
            gr.removeEdge(rw.fst(), rw.snd());
            gr.removeEdge(rw.snd(), rw.fst());
        }

        return of(gr);
    }

    /**
     *
     * @param tfg input graph
     * @param elements elements to be removed
     * @param <T>
     * @return returns a graph with the elements removed
     */
    public static <T> TypeFactGraph<T> removeNodes(TypeFactGraph<T> tfg, Iterable<T> elements){
        MutableValueGraph<T,String> gr = getMutableOf(tfg);
        for(T r: elements){
            gr.removeNode(r);
        }
        return of(gr);
    }


    /**
     *
     * @return all the nodes of in the TFG
     */
    public Stream<U> nodes_p(){
        return tfg.nodes().parallelStream();
    }

    /**
     *
     * @param g input TFG proto object
     * @return TypeFactGraph of the TFG
     */
    public static TypeFactGraph<Identification> of(TFG g){
        List<Function<MutableValueGraph<Identification,String>, MutableValueGraph<Identification,String>>> addNodesEdges =
                Stream.concat(g.getNodesList().stream().map(TypeFactGraph::addNode)
                , g.getEdgesList().stream().map(e -> u_v(g.getNodes(e.getFst()),
                        g.getNodes(e.getSnd()), e.getEdgeValue()))).collect(toList());
        return TypeFactGraph.<Identification>emptyTFG().mergeMap(addNodesEdges);
    }

    /**
     *
     * @return returns a serializable form of TypeFactGraph using the protocol-buffer
     */
    public TFG asTFG(){
        final List<Identification> nodes = new ArrayList(tfg.nodes());
        List<Edge> edges = tfg.edges().stream().map(e -> P(e, tfg.edgeValue(e.nodeU(), e.nodeV()).get()))
                .map(p -> Edge.newBuilder().setFst(nodes.indexOf(p.fst().nodeU()))
                        .setSnd(nodes.indexOf(p.fst().nodeV())).setEdgeValue(p.snd())
                        .build())
                .collect(toList());
        return TFG.newBuilder().addAllNodes(nodes).addAllEdges(edges).build();
    }

    public TFGRefactorable asTFGRef(){
        final List<Refactorable> nodes = new ArrayList(tfg.nodes());
        List<REdge> edges = tfg.edges().stream().map(e -> P(e, tfg.edgeValue(e.nodeU(), e.nodeV()).get()))
                .map(p -> REdge.newBuilder().setFst(nodes.indexOf(p.fst().nodeU()))
                        .setSnd(nodes.indexOf(p.fst().nodeV())).setEdgeValue(p.snd())
                        .build())
                .collect(toList());
        return TFGRefactorable.newBuilder().addAllNodes(nodes).addAllEdges(edges).build();
    }

    public static<U> Optional<U> getSuccessorWithEdge(TypeFactGraph<U> t, U m, String s) {
        return t.get().successors(m).stream().filter(x -> t.get().edgeValue(m, x).get().contains(s)).findFirst();
    }

    public static <U> Set<U> getSuccessorsWithEdge(TypeFactGraph<U> t, U m, String s) {
        return t.get().successors(m).stream().filter(x -> t.get().edgeValue(m, x).get().contains(s)).collect(toSet());
    }

    public static <U> Set<U> getSuccessorsWithEdges(TypeFactGraph<U> t, U m, List<String> s) {
        return t.get().successors(m).stream()
                .filter(succ -> s.stream().anyMatch(ed -> t.get().edgeValue(m,succ).get().contains(ed)))
                .collect(toSet());
    }

    public static final String NO_EDGE = "NO_EDGE";
    public Pair<String,String> getEdgeValuesBetween(U u, U v){
        return P(tfg.edgeValueOrDefault(u,v,NO_EDGE),tfg.edgeValueOrDefault(v,u,NO_EDGE));
    }

    public static TypeFactGraph<Identification> induceGraph(TypeFactGraph<Identification> tfg, Set<Identification> ns){
        MutableValueGraph<Identification,String> gr = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
        for(Identification t: ns) {
            for(Identification q : ns){
                tfg.get().edgeValue(t,q).ifPresent(x -> gr.putEdgeValue(t,q,x));
            }
        }
        return of(gr);
    }
}

package com.google.errorprone.bugpatterns.T2R.common;

import com.google.errorprone.bugpatterns.T2R.common.Models.IdentificationOuterClass.Identification;
import com.google.errorprone.bugpatterns.T2R.common.Util.Pair;

import com.sun.source.tree.Tree;

import java.util.List;

/**
 * Created by ameya on 12/17/18.
 */
public class State {
    private final Tree root;
    private final List<Pair<State,String>> typeDependents;
    private final Identification rootID;

    public State(Tree root, Identification rootID, List<Pair<State,String>> typeDependents){
        this.root = root;
        this.typeDependents = typeDependents;
        this.rootID = rootID;
    }

    public Tree getRoot() {
        return this.root;
    }

    public List<Pair<State,String>> getTypeDependents() {
        return this.typeDependents;
    }

    public Identification getRootID() {
        return this.rootID;
    }
}


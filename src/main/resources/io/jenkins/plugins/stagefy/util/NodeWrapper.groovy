package io.jenkins.plugins.stagefy.util

/** Wraps a stage body in a {@code node(label)} block when the YAML {@code node} key is set. */
class NodeWrapper {
    /** Returns {@code content} unchanged when {@code nodeData} is null, otherwise a closure running it on that node. */
    static Closure wrap(def jenkins, Closure content, Object nodeData) {
        if (nodeData == null) return content
        def cur = content
        return { jenkins.node(nodeData.toString(), cur) }
    }
}

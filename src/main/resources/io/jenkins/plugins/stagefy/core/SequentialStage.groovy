package io.jenkins.plugins.stagefy.core

import io.jenkins.plugins.stagefy.util.NodeWrapper

/** Stage with a YAML {@code stages} list (plus optional {@code node}); children run one after another, each in its own Jenkins stage. */
class SequentialStage extends Stage {
    SequentialStage(String filename, String stagename, Stage parent, def jenkins) {
        super(filename, stagename, parent, jenkins)
    }

    void execute(Map data) {
        this.checkCircularLoop(this)
        List entries = resolveEntries((List) data.get("stages"), "stages")
        def content = { for (entry in entries) { entry.get("body")() } }
        if (flag) content = NodeWrapper.wrap(jenkins, content, data.get("node"))
        content()
    }
}

package io.jenkins.plugins.stagefy.core

/** Stage with a YAML {@code stages} list; children run one after another, each in its own Jenkins stage. */
class SequentialStage extends Stage {
    SequentialStage(String filename, String stagename, Stage parent, def jenkins) {
        super(filename, stagename, parent, jenkins)
    }

    void execute(Map data) {
        this.checkCircularLoop(this)
        List entries = resolveEntries((List) data.get("stages"), "stages")
        for (entry in entries) { entry.get("body")() }
    }
}

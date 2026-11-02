package io.jenkins.plugins.stagefy.core

/** Stage with a YAML {@code parallels} list; children run as concurrent Jenkins parallel branches. */
class ParallelStage extends Stage {
    ParallelStage(String filename, String stagename, Stage parent, def jenkins) {
        super(filename, stagename, parent, jenkins)
    }

    void execute(Map data) {
        this.checkCircularLoop(this)
        Map branches = [:]
        for (entry in resolveEntries((List) data.get("parallels"), "parallels")) {
            branches[entry.get("name")] = entry.get("body")
        }
        jenkins.parallel(branches)
    }
}

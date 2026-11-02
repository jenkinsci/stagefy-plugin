package io.jenkins.plugins.stagefy.core

/** Stage with a YAML {@code steps} list (plus optional {@code env}, {@code node}, {@code when}); expands template/iterated entries. */
class StepsStage extends Stage {
    StepsStage(String filename, String stagename, Stage parent, def jenkins) {
        super(filename, stagename, parent, jenkins)
    }

    void execute(Map data) {
        List resolvedSteps = resolveSteps(data)
        def content = { executeResolvedSteps(resolvedSteps) }
        content = wrapWithEnv(content, (Map) data.get("env"))
        content = wrapWithNode(content, data.get("node"))
        if (flag) content()
    }

    private List resolveSteps(Map data) {
        List resolved = []
        List steps = (List) data.get("steps")
        for (each in steps) {
            if (each.containsKey("template")) {
                resolved.addAll((List) expandTemplateRef(each, null, "steps").get("steps"))
            } else if (each.containsKey("iterated")) {
                for (r in expandIterated(each, null, "steps")) { resolved.addAll((List) r.get("steps")) }
            } else {
                resolved.add(each)
            }
        }
        return resolved
    }

    private Closure wrapWithEnv(Closure content, Map envData) {
        if (envData == null) return content
        List envList = []
        for (e in envData.entrySet()) { envList.add(e.key + "=" + e.value) }
        def cur = content
        return { jenkins.withEnv(envList, cur) }
    }

    private Closure wrapWithNode(Closure content, Object nodeData) {
        if (nodeData == null) return content
        def cur = content
        return { jenkins.node(nodeData.toString(), cur) }
    }
}

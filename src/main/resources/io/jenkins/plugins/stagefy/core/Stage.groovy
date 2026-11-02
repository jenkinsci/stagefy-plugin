package io.jenkins.plugins.stagefy.core

import io.jenkins.plugins.stagefy.util.*

/** Base class of stages: loads its YAML, evaluates {@code when}, guards against cycles, then delegates to execute(). */
abstract class Stage implements Serializable {
    String filename
    String stagename
    boolean flag = true
    Stage parent
    def jenkins

    Stage(String filename, String stagename, Stage parent, def jenkins) {
        this.filename = filename
        this.stagename = stagename
        this.parent = parent
        this.jenkins = jenkins
    }

    def load() { return jenkins.loadData(filename, stagename) }

    /** Walks up the parent chain and throws if the same file/stage appears again (circular reference). */
    void checkCircularLoop(Stage other) {
        if (parent == null) return
        if (parent.filename == other.filename && parent.stagename == other.stagename) {
            throw new RuntimeException("Circular Loop Execution ${stagename} from ${other.filename}")
        }
        parent.checkCircularLoop(other)
    }

    /** Rejects template stages, computes {@code flag} from {@code when} (ANDed with the parent's), then runs execute(). */
    void run() {
        Map data = load()
        if (data != null && data.containsKey("template")) {
            jenkins.error("Template stage '${stagename}' cannot be executed directly")
        }
        def whenData = data.get("when")
        flag = (whenData == null) ? true : jenkins.evaluate(whenData)
        if (parent != null) flag = parent.flag && flag
        jenkins.info("Stage ENTER: ${stagename}")
        execute(data)
        jenkins.info("Stage EXIT: ${stagename}")
    }

    /** Subclass hook: runs the stage body from its YAML {@code data}. */
    abstract void execute(Map data)

    // "name" or "name from file"; only whitespace-delimited "from" separates, so names containing "from" are safe
    protected List parseStageRef(String ref) {
        def parts = ref.trim().split(/\s+from\s+/, 2)
        if (parts.length == 2) return [parts[0].trim(), parts[1].trim()]
        return [ref.trim(), this.filename]
    }

    /** Creates the child stage {@code name} from {@code file}, with this stage as parent. */
    Stage constructChild(String file, String name) {
        def childData = jenkins.loadData(file, name)
        return StageFactory.create(file, name, this, jenkins, childData)
    }

    /** Runs already-resolved step maps in order via StepFactory. */
    void executeResolvedSteps(List resolvedSteps) {
        for (step in resolvedSteps) {
            jenkins.debug("Step: ${step.keySet()}")
            StepFactory.create(step, jenkins, this).run()
        }
    }

    /**
     * Resolves a {@code stages}/{@code parallels} list into entries of {@code [name, body]}; {@code body} runs the
     * entry inside its own Jenkins stage. Entries are plain stage refs, {@code template:} or {@code iterated:} maps.
     */
    protected List resolveEntries(List entries, String block) {
        List resolved = []
        List seenNames = []
        for (each in entries) {
            if (each instanceof String) {
                def parsed = parseStageRef((String) each)
                def capturedStage = parsed[0]
                def capturedFile = parsed[1]
                checkDuplicate(capturedStage, seenNames, block)
                seenNames.add(capturedStage)
                resolved.add([name: capturedStage, body: {
                    jenkins.stage(capturedStage) { constructChild(capturedFile, capturedStage).run() }
                }])
            } else if (each instanceof Map) {
                Map entry = (Map) each
                if (entry.containsKey("template")) {
                    Map result = expandTemplateRef(entry, seenNames, block)
                    resolved.add(stepsEntry(result.get("name"), result.get("steps")))
                    seenNames.add(result.get("name"))
                } else if (entry.containsKey("iterated")) {
                    List results = expandIterated(entry, seenNames, block)
                    for (r in results) {
                        resolved.add(stepsEntry(r.get("name"), r.get("steps")))
                        seenNames.add(r.get("name"))
                    }
                }
            }
        }
        return resolved
    }

    protected Map stepsEntry(def name, def steps) {
        def capturedName = name
        def capturedSteps = steps
        return [name: capturedName, body: { jenkins.stage(capturedName) { executeResolvedSteps(capturedSteps) } }]
    }

    protected void checkDuplicate(String name, List seenNames, String block) {
        if (seenNames != null && seenNames.contains(name)) {
            jenkins.error("Duplicate stage name '${name}' in ${block} block of '${this.stagename}'")
        }
    }

    /** Expands a {@code template:} entry into {@code [name, steps]}; {@code seenNames == null} skips name disambiguation and duplicate checks. */
    protected Map expandTemplateRef(Map entry, List seenNames, String block) {
        String templateName = (String) entry.get("template")
        Map bindings = [:]
        for (e in entry.entrySet()) {
            if (!"template".equals(e.key)) bindings.put(e.key, e.value.toString())
        }
        def tpl = new Template(templateName, this.filename, jenkins.loadData(this.filename, templateName))
        tpl.validate(jenkins)
        tpl.validateBindings(bindings, jenkins, this.stagename)
        List resolvedSteps = TemplateResolver.resolveStepsWithBindings(tpl.getSteps(), bindings)
        String name = StageNameBuilder.forTemplateRef(templateName, bindings, seenNames ?: [])
        checkDuplicate(name, seenNames, block)
        return [name: name, steps: resolvedSteps]
    }

    /** Expands an {@code iterated:} entry into a list of {@code [name, steps]}, one per item; {@code seenNames == null} skips duplicate checks. */
    protected List expandIterated(Map entry, List seenNames, String block) {
        Map iteratedData = (Map) entry.get("iterated")
        String templateName = (String) iteratedData.get("template")
        def tpl = new Template(templateName, this.filename, jenkins.loadData(this.filename, templateName))
        tpl.validate(jenkins)
        List items = IterationResolver.expandOver(iteratedData.get("over"), jenkins, this.stagename)
        List results = []
        for (int j = 0; j < items.size(); j++) {
            def item = items.get(j)
            Map bindings = IterationResolver.buildBindingsFromItem(templateName, tpl.getArguments(), item, jenkins, this.stagename)
            String name = (item instanceof String)
                    ? StageNameBuilder.forScalar(templateName, (String) item)
                    : StageNameBuilder.forMap(templateName, (Map) item, j)
            tpl.validateBindings(bindings, jenkins, this.stagename)
            checkDuplicate(name, seenNames, block)
            results.add([name: name, steps: TemplateResolver.resolveStepsWithBindings(tpl.getSteps(), bindings)])
        }
        return results
    }
}

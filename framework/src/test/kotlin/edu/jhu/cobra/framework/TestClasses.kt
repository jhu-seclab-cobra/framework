package edu.jhu.cobra.framework

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@WorkLicense
annotation class TestWorkLicense(val name: String)

data class TestResult(val value: String)
data class TestTask(override val uid: ITask.ID) : ITask

class TestWorker : IWorker<TestTask, TestResult> {
    override fun work(task: TestTask): TestResult =
        TestResult("Processed: ${task.uid.license}")
}

class TestDispatcher : IDispatcher<TestWorker> {
    private val workers = mutableMapOf<ITask.ID, TestWorker>()

    override fun dispatch(forTask: ITask.ID): TestWorker? = workers[forTask]
    override fun register(forTask: ITask.ID, toWorker: TestWorker) {
        workers[forTask] = toWorker
    }
}

open class TestWorkshop : AbcWorkshop<TestWorker>() {
    @TestWorkLicense("worker1")
    val worker1 = TestWorker()

    @TestWorkLicense("worker2")
    val worker2 = TestWorker()

    val unlicensedWorker = TestWorker()
}

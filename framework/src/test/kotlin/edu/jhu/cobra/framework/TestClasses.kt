package edu.jhu.cobra.framework

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@WorkLicense
annotation class TestWorkLicense(
    val name: String,
)

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@WorkLicense
annotation class TestModedLicense(
    val name: String,
    val mode: RegisterMode = RegisterMode.ATTACH,
)

data class TestResult(
    val value: String,
)

data class TestTask(
    override val uid: ITask.ID,
) : ITask

class TestWorker : IWorker<TestTask, TestResult> {
    override fun work(task: TestTask): TestResult = TestResult("Processed: ${task.uid.license}")
}

open class TestWorkshop : AbcWorkshop<TestWorker>() {
    @TestWorkLicense("worker1")
    val worker1 = TestWorker()

    @TestWorkLicense("worker2")
    val worker2 = TestWorker()

    val unlicensedWorker = TestWorker()
}

class TestModedWorkshop : AbcWorkshop<TestWorker>() {
    @TestModedLicense("modedWorker", mode = RegisterMode.REPLACE)
    val modedWorker = TestWorker()
}

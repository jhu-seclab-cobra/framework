package edu.jhu.cobra.framework

import kotlinx.coroutines.flow.FlowCollector
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible


@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@WorkLicense annotation class TestWorkLicense(val name: String){
    companion object {
        fun getTaskID(cls: Class<*>, vararg props: String): ITask.ID =
            ITask.ID(cls.simpleName, props.toSet())

        fun isTaskID(taskID: ITask.ID, forLicense: Class<*>): Boolean =
            taskID.license == forLicense.simpleName
    }
}

// Test implementations
data class TestProduct(val value: String) : IProduct
data class TestTask(override val uid: ITask.ID) : ITask

class TestWorker : IWorker<TestTask, TestProduct> {
    override suspend fun FlowCollector<TestProduct>.work(task: TestTask) {
        emit(TestProduct("Processed: ${task.uid.license}"))
    }
}

class TestDispatcher : IDispatcher<TestWorker> {
    private val workers = mutableMapOf<ITask.ID, TestWorker>()

    override fun dispatch(forTask: ITask.ID): TestWorker? = workers[forTask]
    override fun register(forTask: ITask.ID, toWorker: TestWorker) {
        workers[forTask] = toWorker
    }
}

// Test implementation of AbcWorkshop
open class TestWorkshop : AbcWorkshop<TestWorker>() {
    @TestWorkLicense("worker1")
    val worker1 = TestWorker()

    @TestWorkLicense("worker2")
    val worker2 = TestWorker()

    val unlicensedWorker = TestWorker()
}

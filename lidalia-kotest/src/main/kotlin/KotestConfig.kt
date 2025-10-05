package lidalia.kotest

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.IsolationMode.InstancePerLeaf
import io.kotest.engine.concurrency.TestExecutionMode.LimitedConcurrency
import org.slf4j.bridge.SLF4JBridgeHandler
import java.lang.Runtime.getRuntime

object KotestConfig : AbstractProjectConfig() {

  init {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }

  override val testExecutionMode = run {
    val threeQuartersOfProcessors = ((getRuntime().availableProcessors() * 3) / 4).coerceAtLeast(1)
    LimitedConcurrency(threeQuartersOfProcessors)
  }
  override val isolationMode: IsolationMode = InstancePerLeaf
}

package lidalia.kotest

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.IsolationMode.InstancePerLeaf
import org.slf4j.bridge.SLF4JBridgeHandler
import java.lang.Runtime.getRuntime

object KotestConfig : AbstractProjectConfig() {

  init {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }

  override val parallelism: Int = ((getRuntime().availableProcessors() * 3) / 4).coerceAtLeast(1)

  override val isolationMode: IsolationMode = InstancePerLeaf
}

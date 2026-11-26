package io.iggdrasil.mtm.web

import io.iggdrasil.mtm.db.metrics.MtMInspector
import io.iggdrasil.mtm.models.MtMStatusResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mtm/diagnostics")
class MtMDiagnosticsController(private val inspector: MtMInspector) {

    @GetMapping("/status")
    fun getStatus(): MtMStatusResponse = inspector.getStatus()
}
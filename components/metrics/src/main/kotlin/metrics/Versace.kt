package metrics

import java.nio.file.Path
import java.nio.file.Paths

fun versaceBblPath(): Path {
    return Paths.get(
        System.getenv("HOME"), "workspace", "deployments-metrics", "gcp-environments", "versace", "bbl-state.json"
    )
}

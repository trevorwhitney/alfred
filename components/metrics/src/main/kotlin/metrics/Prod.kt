package metrics

fun prodRemoteCommand(environmentName: String, boshCommand: String) =
    "bash -l -c \"direnv allow && gobosh -e prod -d $environmentName $boshCommand\""

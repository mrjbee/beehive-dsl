List<String> autoRunOnSeedChangeJobsList = []
def runOnSeedChange = { String jobName ->
    autoRunOnSeedChangeJobsList << jobName
    return jobName
}

String basePath = 'Logs'

folder(basePath) {
    description 'This folder contains jobs which watch for home logs.'
}

job(runOnSeedChange("$basePath/Test Log Watcher")) {
    triggers {
        scm 'H H * * *'
    }
    steps {
        shell('''
            ekho "Doing nothing, and should fail"
        ''')
    }
}

job ("Seed Job After Update Runner") {
    publishers {
        downstreamParameterized {
            trigger autoRunOnSeedChangeJobsList, {
                condition("SUCCESS")
            }
        }
    }
}

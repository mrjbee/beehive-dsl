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
        cron 'H H * * *'
    }
    steps {
        shell('''
            echo "Jenkins username is '$USER'"
        ''')
    }

    publishers {
        mailer('misterJbee+beehive@gmail.com', true)
    }
}

job(runOnSeedChange("$basePath/Puzzle Log Watcher")) {
    def fileName = "/var/log/custom/puzzle/main.log"
    triggers {
        cron 'H * * * *'
    }
    steps {
        shell("""
            [ -e '$fileName' ] || echo "File '$fileName' not found" && (grep -i -A10 -E "ERROR|exception" '$fileName'  && (exit 1;))
        """.trim())
    }

    publishers {
        mailer('misterJbee+beehive@gmail.com', true)
    }
}

job ("Seed Job After Update Runner") {
    publishers {
        mailer('misterJbee+beehive@gmail.com', true)
        downstreamParameterized {
            trigger autoRunOnSeedChangeJobsList, {
                condition("SUCCESS")
                triggerWithNoParameters()
            }
        }
    }
}

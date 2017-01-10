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
set +e
set +v
if [ -e '$fileName' ]
then
        GREP_OUT=\$(grep -i -A10 -E "ERROR|exception" '$fileName')
    if [ ! -z "\$GREP_OUT" ]
    then
        echo "======================== FOUND ==========================="
        echo \$GREP_OUT
        exit 4
    else
      echo "Found nothing. Everything goes as expected"
    fi
else
  echo "No file '$fileName' exist"
fi

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

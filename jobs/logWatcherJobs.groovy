
def defaultMailTo = System.getenv().DEFAULT_MAIL_TO

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
        mailer(defaultMailTo, true)
    }
}

job(runOnSeedChange("$basePath/Backup Log Watcher")) {
    def fileName = "/var/log/custom/default/media_backup.*"
    triggers {
        cron 'H H * * *'
    }
    steps {
        shell("""

set +x
set +e
cat /var/log/custom/default/media_backup.log
GREP_OUT=\$(grep -i -A10 -E "can't|WARN|ERROR|exception" $fileName)
if [ ! -z "\$GREP_OUT" ]
then
    echo "======================== FOUND ==========================="
    echo \$GREP_OUT
    exit 4
else
  echo "Found nothing. Everything goes as expected"
fi

        """.trim())
    }

    publishers {
        mailer(defaultMailTo, true)
    }
}


job(runOnSeedChange("$basePath/Puzzle Log Watcher")) {
    def fileName = "/var/log/custom/puzzle/main.log"
    triggers {
        cron 'H * * * *'
    }
    steps {
        shell("""
set +x
set +e
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
        mailer(defaultMailTo, true)
    }
}

job ("Seed Job After Update Runner") {
    publishers {
        mailer(defaultMailTo, true)
        downstreamParameterized {
            trigger autoRunOnSeedChangeJobsList, {
                condition("SUCCESS")
                triggerWithNoParameters()
            }
        }
    }
}

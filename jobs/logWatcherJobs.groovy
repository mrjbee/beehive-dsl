String basePath = 'Logs'

folder(basePath) {
    description 'This folder contains jobs which watch for home logs.'
}

job("$basePath/Test Log Watcher") {
    triggers {
        scm 'H H * * *'
    }
    steps {
        shell('''
            echo "Doing nothing, just a test"
        ''')
    }
}

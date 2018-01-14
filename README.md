crawler_master GCP
Usage: crawler_master [options] task-id

  task-id                  tasks for monitor
  --vm-amount <value>      default 2
  --vm-image <value>       default server-base
  --vm-region <value>      default us-west1
  --vm-zone <value>        default a
  --vm-core <value>        default 1
  --vm-memory <value>      default container-memory * container-amount-in-vm + 256
  --container-image <value>
                           default gcr.io/crawler-1383/fb-crawler
  --container-image-version <value>
                           default latest
  --container-amount-in-vm <value>
                           default 4
  --container-core <value>
                           WARN: not implement yet, default 0.25
  --container-memory <value>
                           default 1024
  -c, --cron <value>       default '0 0/3 * * * ?'
  --first-sleep-time <value>
                           default 60 (seconds)
  -u, --user <value>       default empty
  -b, --branch <value>     default master
  -t, --tag <value>        defaultgit push -u origin master ''
  -k, --secret-key <value>
                           key for decrypting PII
  -e, --environment <value>
                           must be 'product' or 'develop' default product
  --ssh-user <value>       default onlinecrm
  --ssh-key-path <value>   default /etc/ssh/onlinecrm/onlinecrm.pem
  --help                   prints this usage text
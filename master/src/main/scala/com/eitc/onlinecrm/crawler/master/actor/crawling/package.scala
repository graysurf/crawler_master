package com.eitc.onlinecrm.crawler.master.actor

package object crawling {

  def userData(task: Int, crawler: Int, secretKey: String, dbHost: String, dbUser: String, dbPassword: String, branch: String, tag: String): String =
    s"""#!/usr/bin/env bash
       |# i am root user in AWS cloudinit
       |
       |TASK_ID='$task'
       |CRAWLER_ID='$crawler'
       |SECRET_KEY='$secretKey'
       |DB_HOST='$dbHost'
       |DB_USER='$dbUser'
       |DB_PASSWORD='$dbPassword'
       |BRANCH='$branch'
       |TAG='$tag'
       |
       |EXIT_CMD='shutdown now'
       |EXIT_IN_HOUR=999
       |
       |USER='fbcrawler'
       |REPO='facebook_personal_data_crawler'
       |#-----------------------
       |
       |CRAWLER_HOME=/home/$${USER}/$${REPO}
       |
       |cat > $${CRAWLER_HOME}/conf/env.sh <<EOF
       |DB_HOST=$${DB_HOST}
       |DB_USER=$${DB_USER}
       |DB_PASSWORD=$${DB_PASSWORD}
       |SECRET_KEY=$${SECRET_KEY}
       |
       |BRANCH=$${BRANCH}
       |TAG=$${TAG}
       |TASK_ID=$${TASK_ID}
       |CRAWLER_ID=$${CRAWLER_ID}
       |
       |EXIT_CMD="$${EXIT_CMD}"
       |EXIT_IN_HOUR=$${EXIT_IN_HOUR}
       |
       |REPO=$${REPO}
       |USER=$${USER}
       |REPO_PATH=/home/$${USER}/$${REPO}
       |CRAWLER_HOME=$${CRAWLER_HOME}
       |
       |LC_ALL="zh_CN.UTF-8"
       |LANG="zh_CN.UTF-8"
       |
       |# See http://github.com/SeleniumHQ/docker-selenium/issues/87
       |DBUS_SESSION_BUS_ADDRESS=/dev/null
       |
       |EOF
       |chmod +x $${CRAWLER_HOME}/conf/env.sh
       |
       |source $${CRAWLER_HOME}/conf/env.sh
       |export DB_HOST DB_USER DB_PASSWORD SECRET_KEY BRANCH TASK_ID EXIT_CMD EXIT_IN_HOUR REPO USER REPO_PATH CRAWLER_HOME LC_ALL LANG DBUS_SESSION_BUS_ADDRESS
       |
       |# note that root don't have ssh key for git
       |# [ ! -d $${CRAWLER_HOME} ] &&  sudo -u $${USER} git clone --depth=1 -b $${BRANCH} --single-branch ssh://git@bitbucket.org/eitcocrm/$${REPO}.git &&
       |
       |cd $${CRAWLER_HOME} &&
       |
       |[ -z "$${BRANCH}" ] && echo "no git branch specified" || { sudo -u $${USER} git fetch origin $${BRANCH} && git checkout -f FETCH_HEAD ; } &&
       |[ -z "$${TAG}" ] && echo "no git tag specified" || { sudo -u $${USER} git fetch --tags && git checkout -f tags/$${TAG} ; } &&
       |
       |sudo docker pull onlinecrm/env &&
       |
       |echo "process start." &&
       |# ./bin/start.sh &>> fb-crawler.log &
       |sudo docker run -d \\
       |     --name fbcrawler \\
       |     --env-file $${CRAWLER_HOME}/conf/env.sh \\
       |     -v $${CRAWLER_HOME}:/home/seluser/facebook_crawler \\
       |     -w /home/seluser/facebook_crawler \\
       |     onlinecrm/env \\
       |     bin/start.sh &&
       |{ sudo docker logs --follow fbcrawler &>> fb-crawler.log & } &&
       |
       |sudo docker wait fbcrawler && echo "process completed." ||
       |{ echo 'something went wrong. wait 5 min.'; sleep 300;  }
       |
       |
       |sudo $${EXIT_CMD}
       |echo "command $${EXIT_CMD} executed." """.stripMargin

  def userData(task: Int, crawler: Int, key: String, branch: String, tag: String): String = userData(
    task,
    crawler,
    key,
    "online-crm.c3oguoknq1nq.ap-northeast-1.rds.amazonaws.com",
    "onlinecrm",
    "scalaocrm",
    branch,
    tag
  )
}

#!/usr/bin/env bash
################################################################################
################################################################################
# Build and local install the base and application docker images
################################################################################
################################################################################

################################################################################
## Resolves the directory this script is in. Tolerates symlinks.
SOURCE="${BASH_SOURCE[0]}" ;
while [[ -h "$SOURCE" ]] ; do TARGET="$(readlink "${SOURCE}")"; if [[ $SOURCE == /* ]]; then SOURCE="${TARGET}"; else DIR="$( dirname "${SOURCE}" )"; SOURCE="${DIR}/${TARGET}"; fi; done
BASE_DIR="$( cd -P "$( dirname "${SOURCE}" )" && pwd )" ;
PROJECT_HOME="$( cd -P "${BASE_DIR}/../" && pwd )"
################################################################################
source ${PROJECT_HOME}/bin/Functions.sh
source ${PROJECT_HOME}/bin/conf.sh
################################################################################

################################################################################
printHeader
################################################################################

isCommandInstalled sbt true

printMSG "Starting sbt and running dev on port "

pushd ${PROJECT_HOME} &> /dev/null
    sbt "project generator" "run 9002"
popd &> /dev/null;

################################################################################

#${APP_IMAGE_BUILD_CMD}
################################################################################
printFooter
################################################################################

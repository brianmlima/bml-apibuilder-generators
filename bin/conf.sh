#!/usr/bin/env bash


################################################################################
# Editable configuration.
################################################################################

ORG_NAME="bml"
ORG_KEY="${ORG_NAME}"
ORG_VISIBILITY="organization"
ORG_NAMESPACE="org.bml"

APP_HOST="localhost" ;
API_HOST="localhost" ;
GENERATOR_HOST="localhost";

APP_HOST_PORT="9000" ;
API_HOST_PORT="9001" ;
GENERATOR_PORT="9002" ;

################################################################################
DOCKER_PORT_OPTIONS="-p ${APP_HOST_PORT}:${APP_CONTAINER_PORT} -p ${API_HOST_PORT}:${API_CONTAINER_PORT}"
################################################################################
################################################################################
# DO NOT EDIT STOP.
################################################################################








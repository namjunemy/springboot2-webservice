#!/usr/bin/env bash

ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)

source ${ABSDIR}/profile.sh

REPOSITORY=/home/ec2-user/app/step3
PROJECT_NAME=springboot2-webservice

echo "> 빌드 파일 복사"
echo "cp $REPOSITORY/zip/*.jar $REPOSITORY/"

cp $REPOSITORY/zip/*.jar $REPOSITORY/

echo "> 새 애플리케이션 배포"
# jar중 최종 수정시간 역순
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

echo "> JAR Name: $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"
chmod -x $JAR_NAME

IDLE_PROFILE=$(find_idle_profile)

echo "> $JAR_NAME 를 profile=$IDLE_PROFILE 로 실행합니다."
nohup java -jar \
  -Dspring.config.location=classpath:/application.yml,classpath:/application-$IDLE_PROFILE.yml,home/ec2-user/app/application-oauth.yml,home/ec2-user/app/application-real-db.yml \
  -Dspring.profiles.active=$IDLE_PROFILE \
  $JAR_NAME >$REPOSITORY/nohup.out 2>&1 &

# 0 : 표준입력, 1 : 표준출력, 2 : 표준에러
# 2>&1 stderr도 stdout으로 이동합니다
# & 백그라운드 실행

FROM ubuntu:20.04

WORKDIR /planner
COPY . /planner

RUN apt-get update

RUN apt-get update \
  && apt-get -y install curl gnupg

RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list
RUN curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add

RUN apt-get update && apt-get -y install sbt

RUN mkdir deps && cd deps \
 &&  curl https://download.java.net/openjdk/jdk12/ri/openjdk-12+32_linux-x64_bin.tar.gz --output java.tar.gz \
 && tar xvf java.tar.gz \
 && rm -f ./java.tar.gz
ENV JAVA_HOME="/planner/deps/jdk-12"
ENV PATH="/planner/deps/jdk-12/bin:${PATH}"

RUN cd spiderplan && sbt compile publishM2
RUN cd grpc-server && sbt compile


EXPOSE 8061

CMD [ "grpc-server/start.sh" ]

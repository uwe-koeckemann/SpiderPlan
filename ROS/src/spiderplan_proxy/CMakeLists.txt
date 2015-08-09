cmake_minimum_required(VERSION 2.8.3)
project(spiderplan_proxy)

find_package(catkin REQUIRED)

catkin_package(DEPENDS)

install(PROGRAMS src/spiderplan_proxy.py
  DESTINATION ${CATKIN_PACKAGE_BIN_DESTINATION})

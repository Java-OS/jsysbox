cmake_minimum_required(VERSION 3.9.1) 
project(JOS_SHARED_OBJECTS) 
set(CMAKE_CXX_STANDARD 14) 

find_package(JNI REQUIRED)
if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif() 

SET(ARCH $ENV{ARCH} )

if ("aarch64" STREQUAL "${ARCH}") 
   message (STATUS "ENV ${ARCH}")
   set(CMAKE_CXX_COMPILER aarch64-linux-gnu-g++-12)
endif() 

include_directories(${JNI_INCLUDE_DIRS})

add_library(jsystem SHARED jsystem.cpp)
add_library(jnetwork SHARED jnetwork.cpp)
add_library(jdatetime SHARED jdatetime.cpp) 




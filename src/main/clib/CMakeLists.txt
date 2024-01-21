cmake_minimum_required(VERSION 3.9.1) 
project(JOS_SHARED_OBJECTS) 
set(CMAKE_CXX_STANDARD 14) 

find_package(JNI REQUIRED)
if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif() 

if ("aarch64" STREQUAL "$ENV{ARCH}") 
   message (STATUS "ENV aarch64")
   SET(ARCH "arm64")
   set(CMAKE_CXX_COMPILER aarch64-linux-gnu-g++)
else() 
   message (STATUS "ENV x86_64")
   SET(ARCH "x86_64")
endif() 

include_directories(${JNI_INCLUDE_DIRS})

add_library(jsystem_${ARCH} SHARED jsystem.cpp)
add_library(jnetwork_${ARCH} SHARED jnetwork.cpp)
add_library(jdatetime_${ARCH} SHARED jdatetime.cpp) 




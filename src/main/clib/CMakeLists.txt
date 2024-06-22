cmake_minimum_required(VERSION 3.9.1) 
project(JOS_SHARED_OBJECTS) 
set(CMAKE_CXX_STANDARD 14) 
set(CMAKE_SHARED_LINKER_FLAGS "-Wl,--strip-all")

set(JAVA_AWT_INCLUDE_PATH NotNeeded)
find_package(JNI REQUIRED)
if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif() 

include_directories(${JNI_INCLUDE_DIRS}) 

if ("aarch64" STREQUAL "$ENV{ARCH}") 
   message (STATUS "ENV aarch64")
   SET(ARCH "arm64")
   set(CMAKE_CXX_COMPILER aarch64-linux-gnu-g++)
else() 
   message (STATUS "ENV x86_64")
   SET(ARCH "x86_64")
endif() 

find_library(BLKID_LIBRARY NAMES blkid) 
find_library(KMOD_LIBRARY NAMES kmod) 

add_library(jsystem_${ARCH} SHARED jsystem.cpp)
add_library(jpartition_${ARCH} SHARED jpartition.cpp)
add_library(jnetwork_${ARCH} SHARED jnetwork.cpp)
add_library(jdatetime_${ARCH} SHARED jdatetime.cpp)

target_link_libraries(jsystem_${ARCH} ${KMOD_LIBRARY} stdc++fs)
target_link_libraries(jpartition_${ARCH} ${BLKID_LIBRARY} stdc++fs)
target_link_libraries(jnetwork_${ARCH} ${BLKID_LIBRARY}  stdc++fs)
target_link_libraries(jdatetime_${ARCH} ${BLKID_LIBRARY} stdc++fs)

set_target_properties(jsystem_${ARCH} PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/lib)
set_target_properties(jpartition_${ARCH} PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/lib)
set_target_properties(jnetwork_${ARCH} PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/lib)
set_target_properties(jdatetime_${ARCH} PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/lib)


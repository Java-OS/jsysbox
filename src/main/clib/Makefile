X86_64_CXX=g++
ARM64_CXX=aarch64-linux-gnu-g++-12
X86_64_STRIP=strip
ARM64_STRIP=aarch64-linux-gnu-strip
CFLAGS=-Wall -fPIC -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -lstdc++
BUILD_DIR=build

.PHONY: all clean

all: jsystem jnetwork jdatetime

init:
	mkdir -p $(BUILD_DIR)

jsystem: init
	$(X86_64_CXX) $(CFLAGS) -shared -o $(BUILD_DIR)/jsystem_x86_64.so jsystem.cpp
	$(X86_64_STRIP) --strip-all $(BUILD_DIR)/jsystem_x86_64.so

	$(ARM64_CXX) $(CFLAGS) -shared -o $(BUILD_DIR)/jsystem_arm64.so jsystem.cpp
	$(ARM64_STRIP) --strip-all $(BUILD_DIR)/jsystem_arm64.so

jnetwork: init
	$(X86_64_CXX) $(CFLAGS) -shared -o $(BUILD_DIR)/jnetwork_x86_64.so jnetwork.cpp
	$(X86_64_STRIP) --strip-all $(BUILD_DIR)/jnetwork_x86_64.so

	$(ARM64_CXX) $(CFLAGS) -shared -o $(BUILD_DIR)/jnetwork_arm64.so jnetwork.cpp
	$(ARM64_STRIP) --strip-all $(BUILD_DIR)/jnetwork_arm64.so

jdatetime: init
	$(X86_64_CXX) $(CFLAGS) -shared -o $(BUILD_DIR)/jdatetime_x86_64.so jdatetime.cpp
	$(X86_64_STRIP) --strip-all $(BUILD_DIR)/jdatetime_x86_64.so

	$(ARM64_CXX) $(CFLAGS) -shared -o $(BUILD_DIR)/jdatetime_arm64.so jdatetime.cpp
	$(ARM64_STRIP) --strip-all $(BUILD_DIR)/jdatetime_arm64.so

clean: 
	rm -rfv *.so $(BUILD_DIR)

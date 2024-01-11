CXX=cc
CSTRIP=strip
CFLAGS=-Wall -fPIC -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -lstdc++
BUILD_DIR=build


ifeq ($(ARCH),aarch64) 
	CXX=aarch64-linux-gnu-g++-11
	CSTRIP=aarch64-linux-gnu-strip
endif

.PHONY: all clean 

all: jsystem jnetwork jdatetime

init:
	mkdir -p $(BUILD_DIR)

jsystem: init
	$(CXX) $(CFLAGS) -shared -o $(BUILD_DIR)/jsystem.so jsystem.cpp
	$(CSTRIP) --strip-all $(BUILD_DIR)/jsystem.so

jnetwork: init
	$(CXX) $(CFLAGS) -shared -o $(BUILD_DIR)/jnetwork.so jnetwork.cpp
	$(CSTRIP) --strip-all $(BUILD_DIR)/jnetwork.so

jdatetime: init
	$(CXX) $(CFLAGS) -shared -o $(BUILD_DIR)/jdatetime.so jdatetime.cpp
	$(CSTRIP) --strip-all $(BUILD_DIR)/jdatetime.so

clean: 
	rm -rfv *.so $(BUILD_DIR)

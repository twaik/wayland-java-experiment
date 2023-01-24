TEMPLATE = lib
CONFIG += console c++20
CONFIG -= app_bundle
CONFIG -= qt

SOURCES += \
        hello.cpp \
        wayland-protocol.c \
        src/connection.c \
        src/event-loop.c \
        src/wayland-os.c \
        src/wayland-server.c \
        src/wayland-util.c \
        src/wayland-shm.c
QMAKE_CXXFLAGS += -I/home/twaik/.jdks/openjdk-19.0.1/include
QMAKE_CXXFLAGS += -I/home/twaik/.jdks/openjdk-19.0.1/include/linux
QMAKE_CFLAGS += -Wno-unused-parameter
#QMAKE_LIBS += -Wl,-rpath=/home/twaik/.jdks/openjdk-18.0.2.1/lib/server
#QMAKE_LIBS +=          -L /home/twaik/.jdks/openjdk-18.0.2.1/lib/server
#QMAKE_LIBS += -ljvm
QMAKE_LIBS += -lffi

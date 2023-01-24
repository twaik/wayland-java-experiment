My attempt to implement java wayland server implementation.
In current implementation launching server leads to rapid consumption of RAM and swap, most likely because I forgot to write wl_buffer.release implementation and use it in renderer...
There is Java part with gradle build system and native part with Qt buildsystem. Qt was best option to debug native part because it has pretty nice GDB/LLDB gui and it can connect to existing process.
Project discontinued.

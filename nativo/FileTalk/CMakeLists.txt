# Especifica la versión mínima de CMake
cmake_minimum_required(VERSION 3.0)

# Define el nombre del proyecto
project(filetalk)

# Establece el estándar de C++ que usarás, por ejemplo C++14
set(CMAKE_CXX_STANDARD 14)

# Especifica los directorios donde se encuentran los encabezados
include_directories(${PROJECT_SOURCE_DIR}/src)

# Incluir subdirectorios con código fuente de forma recursiva (si tienes más carpetas)
file(GLOB_RECURSE SOURCES
    src/models/socket/*.cpp
    src/models/server/*.cpp

    main.cpp
)

# Crear el ejecutable con los archivos fuente
add_executable(filetalk ${SOURCES})

# Especifica la instalación del ejecutable
install(TARGETS filetalk RUNTIME DESTINATION bin)

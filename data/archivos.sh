#!/bin/bash

# Definir las rutas de los directorios
rutaOriginal="/home/cris/Descargas"
rutaCopia="/home/cris/copias/Descargas"

# Función para contar la cantidad de archivos en un directorio
contar_archivos() {
    local directorio="$1"
    # Contar solo archivos (no directorios)
    find "$directorio" -type f | wc -l
}

# Función para calcular el tamaño total de un directorio
tamano_total() {
    local directorio="$1"
    # Calcular el tamaño total de los archivos en el directorio
    du -sh "$directorio" | awk '{print $1}'
}

# Verificar si los directorios existen
if [[ ! -d "$rutaOriginal" ]]; then
    echo "El directorio original ($rutaOriginal) no existe."
    exit 1
fi

if [[ ! -d "$rutaCopia" ]]; then
    echo "El directorio de copia ($rutaCopia) no existe."
    exit 1
fi

# Contar los archivos y obtener el tamaño de los directorios
echo "Estadísticas del directorio original:"
archivosOriginal=$(contar_archivos "$rutaOriginal")
tamanoOriginal=$(tamano_total "$rutaOriginal")

echo "Cantidad de archivos: $archivosOriginal"
echo "Tamaño total: $tamanoOriginal"

echo ""

echo "Estadísticas del directorio de copia:"
archivosCopia=$(contar_archivos "$rutaCopia")
tamanoCopia=$(tamano_total "$rutaCopia")

echo "Cantidad de archivos: $archivosCopia"
echo "Tamaño total: $tamanoCopia"

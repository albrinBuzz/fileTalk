# Reemplaza "NombreDelPaquete" con el nombre deseado para el paquete
Name: FileTalk
# Reemplaza "1.0" con la versión del paquete
Version: 1.0
# Reemplaza "1" con la revisión del paquete
Release: 1
License: Apache-2.0 license
Summary: Automated file organizer by extension for efficient folder management and sort
URL: https://github.com/albrinBuzz/Jsorter
Source0:        FileTalk.tar.gz
Source1:        FileTalk.jar
Source2:        FileTalk.desktop
Source3:        FileTalk.png
Requires:       java-17
# Descripción detallada del paquete
%description
This software tool streamlines folder management by automatically organizing files based on their extensions. It simplifies file organization by creating subfolders categorized by file types, enhancing overall folder structure and accessibility.

# Comando para construir el paquete
%prep
# No es necesario hacer nada en esta sección si solo estás empaquetando un archivo .jar



# Comando para compilar o preparar el paquete (si es necesario)
%build
# No es necesario hacer nada en esta sección si solo estás empaquetando un archivo .jar

# Comando para instalar el paquete
%install
# Crear directorios de instalación
install -d %{buildroot}/opt/FileTalk
install -d %{buildroot}/usr/share/applications
#copiar la imagen al directorio /share/icons
mkdir -p %{buildroot}/usr/share/icons/hicolor/48x48/apps/
cp -r %{_sourcedir}/FileTalk.png %{buildroot}/usr/share/icons/hicolor/48x48/apps/

cp %{SOURCE1} %{buildroot}/opt/FileTalk/

mkdir -p %{buildroot}/usr/bin
install -m 755 -d %{buildroot}/usr/bin/
ln -s /opt/FileTalk/FileTalk.jar %{buildroot}/usr/bin/FileTalk



# Instalar archivos
install -m 644 %{SOURCE1} %{buildroot}/opt/FileTalk/
install -m 644 %{SOURCE2} %{buildroot}/usr/share/applications/


# Establecer permisos
# Cambiar los permisos del directorio FileTalk a 755
chmod -R 755 %{buildroot}/opt/FileTalk
# Cambiar los permisos del archivo FileTalk.jar a 644
chmod 644 %{buildroot}/opt/FileTalk/FileTalk.jar
# Cambiar los permisos del archivo FileTalk.desktop a 644
chmod 655 %{buildroot}/usr/share/applications/FileTalk.desktop
# Cambiar los permisos del directorio de iconos a 755
chmod 755 %{buildroot}/usr/share/icons/hicolor/48x48/apps/
# Cambiar los permisos del archivo de icono a 644
chmod 644 %{buildroot}/usr/share/icons/hicolor/48x48/apps/FileTalk.png

%post
# Abrir el puerto 8080 si firewalld está disponible
if command -v firewall-cmd > /dev/null; then
    firewall-cmd --zone=public --add-port=8080/tcp --permanent
    firewall-cmd --reload
fi


# Comando para limpiar antes de construir el paquete
%clean
rm -rf %{buildroot}

# Comando para especificar los archivos incluidos en el paquete
%files
/opt/FileTalk/FileTalk.jar
/usr/share/applications/FileTalk.desktop
/usr/share/icons/hicolor/48x48/apps/FileTalk.png
/usr/bin/FileTalk

%changelog
* Tue Apr 13 2024 cr <cris550@gmail.com> - 1.0-1
- Initial RPM release



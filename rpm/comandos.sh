tar -czvf rpm/rpmbuild/SOURCES/FileTalk.tar.gz -C /home/cris/java/javafx/proyectos/FileTalk .
cp -r rpm/rpmbuild ~/
rpmbuild -ba ~/rpmbuild/SPECS/setup.spec
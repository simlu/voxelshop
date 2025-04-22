#First check previous version of java
java -version

#Remove all versions of java
sudo apt-get purge --auto-remove openjdk*

#Check again, this check should fail
java -version

#Install the correct version for Voxelshop
sudo apt install openjdk-8-jdk openjdk-8-jre

#And launch program
java -jar voxelshop-start.jar

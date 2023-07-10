FROM openjdk:11
EXPOSE 8080
ADD target/songs-storage-0-0-2.jar songs-storage-0-0-2.jar
ENTRYPOINT ["java", "-jar", "songs-storage-0-0-2.jar", "songs-storage-0-0-2.jar"]
FROM java:8  
ADD adsb-iot-flight-service-0.1.jar app.jar  
RUN bash -c 'touch /app.jar'  
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 8080
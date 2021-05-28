FROM openjdk:11.0.11-jdk
# Lets not run as root
RUN groupadd -r swuser -g 433 && \
    useradd -u 431 -r -g swuser -s /sbin/nologin -c "Docker image user" swuser && \
    mkdir -p /home/swuser && \
    chown -R swuser:swuser /home/swuser
USER swuser
# Add the
ADD generator/target/universal/bml-generator-generator-*.zip /home/swuser
RUN cd /home/swuser && unzip bml-generator-generator-*.zip && rm bml-generator-generator-*.zip && \
    cd bml-generator-generator-*/bin

CMD ["/home/swuser/bml-generator-generator-0.1.0-SNAPSHOT/bin/bml-generator-generator","-Dhttp.port=8080"]



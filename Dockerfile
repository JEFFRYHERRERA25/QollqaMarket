FROM tomcat:10-jdk21
RUN sed -i 's/redirectPort="8443"/redirectPort="8080"/g' /usr/local/tomcat/conf/server.xml
COPY dist/QollqaMarket.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
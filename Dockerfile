FROM tomcat:10-jdk21
COPY dist/QollqaMarket.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
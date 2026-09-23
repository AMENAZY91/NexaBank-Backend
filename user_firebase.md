mvn spring-boot:run

Abre una nueva ventana de PowerShell y ejecuta:

$env:JAVA_HOME = "C:\Users\restr\tools\jdk-17.0.20.1+1"
$env:Path += ";C:\Users\restr\tools\jdk-17.0.20.1+1\bin;C:\Users\restr\tools\apache-maven-3.9.6\bin"
cd C:\Users\restr\OneDrive\Desktop\Backend\nexabank\nexabank-api
mvn spring-boot:run
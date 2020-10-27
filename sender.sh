
#!/bin/bash
while true; do
	java -jar target/emailSender-0.0.1-SNAPSHOT.jar 1
	java -jar target/emailSender-0.0.1-SNAPSHOT.jar 2
	java -jar target/emailSender-0.0.1-SNAPSHOT.jar 3
	java -jar target/emailSender-0.0.1-SNAPSHOT.jar 4
	java -jar target/emailSender-0.0.1-SNAPSHOT.jar 5
	read -t 30000 -n 1 -s -r -p "Press any key to continue"
done

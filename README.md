# Wireless wheel

Application for control the robot via TCP/IP connection and accelerometer sensor. Program sends specific message to the server and waiting for response (in loop). Message format has been specified in class TcpClient, method generateMessage. Value from accelerometer sensor was scaled to [-255, 255]. Information about one axis are saved on 4 bytes.

### Presentation of application
![alt text](https://raw.githubusercontent.com/mk307009/wirelesswheel/master/screenshot/main.png "Main activity")
![alt text](https://raw.githubusercontent.com/mk307009/wirelesswheel/master/screenshot/menu.png "Menu")

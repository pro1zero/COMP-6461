# Follow these steps to test httpc on a Docker container. 

Visit: https://labs.play-with-docker.com/ </br>
Press 'Start' and 'Add new instance' (You'll need to login into Docker first.) </br>

Paste these two commands in terminal: </br> 
```docker pull pro1zero/httpccontainer:latest``` </br>
```docker run -i -t pro1zero/httpccontainer:latest``` </br>

Now, you should see the program running with a welcome message. Use one of the commands listed below at a time to test the httpc. </br>

httpc help </br>
httpc help get </br>
httpc help post </br>
httpc get 'http://httpbin.org/get?course=networking&assignment=1' </br>
httpc get -v 'http://httpbin.org/get?course=networking&assignment=1' </br>
httpc post -h Content-Type:application/json --d '{"Assignment": 1}' http://httpbin.org/post </br>

Infact you can remove the url given in the above commands and put your url to find the GET/POST responses. </br>

This project was made in a team of 2(me and Neelofer Shama)

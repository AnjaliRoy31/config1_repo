node {
       checkout scm;
       def url =readProperties file: 'properties_file.properties'
       stage('checkout') { 
                    echo "${url}"
                    def Var1= url.GIT_URL
                    echo "Var1=${Var1}"
                    git "${Var1}"
                }
        stage('Build') {
          sh "mvn clean install" 
            }
        stage('SonarQube analysis') { 
         sh 'mvn sonar:sonar -Dsonar.host.url=http://my77009dns.EastUS2.cloudapp.azure.com:9000/sonar' 
           }
	   
	   stage('Docker Push'){
			//dir ("Playground")
			//{
				//sh  '''if [ "$(docker ps -a |grep tomcat | wc -l)" -ne 0 ]; then docker rm -f $(docker ps -a |grep tomcat | awk '{print $1}') ; fi'''
				//sh  '''if [ "$(docker images | wc -l)" -ne 0 ]; then docker rmi -f $(docker images | awk '{print $3}') ; fi'''
				sh  "docker build -t bank:latest ."
				sh    "docker images"
				sh    "docker login -u 'anjiroy' -p 'Abhi@0331' "
				sh    "docker tag bank:latest anjiroy/bank:latest"
				sh    "docker push anjiroy/bank:latest"
				sh    "docker images"
				//sh "docker run -d -p8084:8080 anjiroy/bank:latest"
			
		   //}
		}   
	   
        stage ('deploying artifact'){
          sh 'cd target';
          sh 'pwd';
          sh 'ls -a';
	  def server =Artifactory.newServer url: "http://ec2-18-224-202-57.us-east-2.compute.amazonaws.com:8081/artifactory/",username: "admin",password: "password";
	  def uploadSpec="""{
	       "files":[
		  {
		   "pattern":"target/*.war",
		   "target":"repo1"
		  }
		]
	  }"""
	 server.upload(uploadSpec)

	}
	stage("Deploy"){
		sh "docker pull anjiroy/bank:latest"
		sh "docker run -d -p8084:8080 anjiroy/bank:latest"
	}
		
	stage('confirmation for build')

	{

	input message:'Do you want to send it to production?',ok:'YES'

	}


}
